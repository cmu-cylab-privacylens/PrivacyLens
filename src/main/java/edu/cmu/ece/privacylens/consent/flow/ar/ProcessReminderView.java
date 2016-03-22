/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2015-2016, Carnegie Mellon University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of SWITCH nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SWITCH BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.cmu.ece.privacylens.consent.flow.ar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.opensaml.profile.context.EventContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import edu.cmu.ece.privacylens.Consent;
import edu.cmu.ece.privacylens.IdPHelper;
import edu.cmu.ece.privacylens.Oracle;
import edu.cmu.ece.privacylens.ar.Attribute;
import edu.cmu.ece.privacylens.ar.AttributeReleaseModule;
import edu.cmu.ece.privacylens.context.ConsentContext;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * Get toggle beans
 *
 */

public final class ProcessReminderView extends AbstractAttributeReleaseAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(ProcessReminderView.class);


    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(
            @Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        final RequestContext requestContext =
                RequestContextHolder.getRequestContext();

        final MutableAttributeMap<Object> flowScope =
                requestContext.getFlowScope();

        final HttpServletRequest request =
                (HttpServletRequest) requestContext.getExternalContext()
                        .getNativeRequest();

        final Oracle oracle = Oracle.getInstance();

        final boolean yesButton = (request.getParameter("yes") != null);
        // final boolean moreOptionsButton = (request.getParameter("moreOptions") != null);
        final boolean noButton = (request.getParameter("no") != null);


        final String relyingPartyId =
                IdPHelper.getRelyingPartyId(profileRequestContext);
        final String principalName =
                IdPHelper.getPrincipalName(profileRequestContext);

        // this timestamp will describe this transaction
        final DateTime timestamp = new DateTime();

        // why?
        //final List<Attribute> attributes = IdPHelper.getAttributes(servletContext, request);

        final List<Attribute> attributes =
                (List<Attribute>) flowScope.get("attributes");

        if (noButton) {
            final EventContext event = new EventContext();
            event.setEvent("no");
            profileRequestContext.addSubcontext(event);
            return;
        }

        if (yesButton) {
            // XXX obtain these from user preferences!
            // CHANGEME implement storage

            // copy previous consents to current consents
            final ConsentContext consentContext = getConsentContext();
            final Map<String, Consent> previousConsents =
                    consentContext.getPreviousConsents();
            consentContext.getCurrentConsents().putAll(previousConsents);

            // change this way?
            final AttributeReleaseModule attributeReleaseModule =
                    IdPHelper.getAttributeReleaseModule();
            final Map<String, Consent> consentByAttribute =
                    attributeReleaseModule.getAttributeConsent(principalName,
                            relyingPartyId, attributes);

            // make entry in login database, with all consented attributes
            final List<Attribute> consentedAttributes =
                    new ArrayList<Attribute>();
            for (final Attribute attribute : attributes) {
                final Consent consent =
                        consentByAttribute.get(attribute.getId());
                if (consent.isApproved()) {
                    consentedAttributes.add(attribute);
                }
            }

            attributeReleaseModule.addLogin(principalName,
                    oracle.getServiceName(relyingPartyId), relyingPartyId,
                    timestamp,
                    consentedAttributes);

            final EventContext event = new EventContext();
            event.setEvent("yes");
            profileRequestContext.addSubcontext(event);
            return;
        }

        // fallthrough
        log.warn("Fell through");
        final EventContext event = new EventContext();
        event.setEvent("entry");
        profileRequestContext.addSubcontext(event);

        log.debug("XXXstroucki Parameters: {}", request.getParameterMap());
    }
}