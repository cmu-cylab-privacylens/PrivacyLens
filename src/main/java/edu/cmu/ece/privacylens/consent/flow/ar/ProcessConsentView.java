/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2015, Carnegie Mellon University
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import edu.cmu.ece.privacylens.Util;
import edu.cmu.ece.privacylens.ar.Attribute;
import edu.cmu.ece.privacylens.ar.AttributeReleaseModule;
import edu.cmu.ece.privacylens.context.ConsentContext;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * Get toggle beans
 *
 */

public final class ProcessConsentView extends AbstractAttributeReleaseAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(ProcessConsentView.class);


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
        final boolean historyButton = (request.getParameter("history") != null);
        final boolean explainButton = (request.getParameter("explain") != null);

        final String relyingPartyId =
                IdPHelper.getRelyingPartyId(profileRequestContext);
        final String principalName =
                IdPHelper.getPrincipalName(profileRequestContext);

        // why?
        //final List<Attribute> attributes = IdPHelper.getAttributes(servletContext, request);

        final List<Attribute> attributes =
                (List<Attribute>) flowScope.get("attributes");



        final Map<String, Boolean> consentByAttribute = new HashMap<String, Boolean>();
        //final DateTime timestamp = (DateTime) request.getSession().getAttribute("timestamp");

        final Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            final String param = (String) e.nextElement();
            if (param.startsWith("input-")) {
                log.debug("Processing parameter {}", param);
                final String inputName = (param.split("^input-"))[1];
                final boolean setting =
                        "1".equals(request.getParameter(param)) ? true : false;
                if (oracle.isAttrGroup(relyingPartyId, inputName)) {
                    final List<String> groupMembers = oracle.getAttributeGroupMembers(relyingPartyId, inputName);
                    for (final String group : groupMembers) {
                        log.debug("Group member {} setting {}", group, setting);
                        consentByAttribute.put(group, setting);
                    }
                } else {
                    consentByAttribute.put(inputName, setting);
                }
            }
        }

        if (yesButton) {
            log.debug("Create consent for user {} to {}.", principalName, relyingPartyId);

            final Map<String, Consent> consents =
                    new LinkedHashMap<String, Consent>();

            for (final Attribute attribute : attributes) {
                // net/shibboleth/idp/consent/logic/AttributeReleaseConsentFunction.java
                final Consent consent = new Consent();
                consent.setId(attribute.getId());
                // will there be ordering problems?
                consent.setValue(Util.hash(Util.listToString(attribute
                        .getValues())));
                final Boolean consented = consentByAttribute.get(attribute.getId());
                if (consented != null && !consented) {
                    log.trace("Did not consent for {}", attribute.getId());
                    consent.setApproved(false);
                } else {
                    log.trace("Did consent for {}", attribute.getId());
                    consent.setApproved(true);
                }
                consents.put(attribute.getId(), consent);
            }

            final ConsentContext consentContext = getConsentContext();
            consentContext.getCurrentConsents().putAll(consents);

            // do this elsewhere? change how it is done?
            final AttributeReleaseModule attributeReleaseModule =
                    IdPHelper.attributeReleaseModule;
            final List<Attribute> deniedAttributes = new ArrayList<Attribute>();
            final List<Attribute> consentedAttributes =
                    new ArrayList<Attribute>();

            for (final Attribute attribute : attributes) {
                final Boolean consented =
                        consentByAttribute.get(attribute.getId());
                if (consented != null && !consented) {
                    log.trace("Did not consent for {}", attribute.getId());
                    deniedAttributes.add(attribute);
                } else {
                    log.trace("Did consent for {}", attribute.getId());
                    consentedAttributes.add(attribute);
                }
            }

            attributeReleaseModule.denyAttributeRelease(principalName,
                    relyingPartyId, deniedAttributes);
            attributeReleaseModule.consentAttributeRelease(principalName,
                    relyingPartyId, consentedAttributes);
            final DateTime timestamp = consentContext.getTimestamp();
            //make entry in login database, with all consented attributes
            attributeReleaseModule.addLogin(principalName,
                    oracle.getServiceName(relyingPartyId), relyingPartyId,
                    timestamp, consentedAttributes);

            final EventContext event = new EventContext();
            event.setEvent("yes");
            profileRequestContext.addSubcontext(event);
            return;
        }

        if (noButton) {
            final EventContext event = new EventContext();
            event.setEvent("no");
            profileRequestContext.addSubcontext(event);
            return;
        }

        if (historyButton) {
            final EventContext event = new EventContext();
            event.setEvent("admin");
            profileRequestContext.addSubcontext(event);
            return;
        }

        if (explainButton) {
            final EventContext event = new EventContext();
            event.setEvent("explanation");
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