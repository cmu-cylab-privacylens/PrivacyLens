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

import edu.cmu.ece.privacylens.IdPHelper;
import edu.cmu.ece.privacylens.Oracle;
import edu.cmu.ece.privacylens.ar.Attribute;
import edu.cmu.ece.privacylens.ar.AttributeReleaseModule;
import edu.cmu.ece.privacylens.ar.ReminderInterval;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * Process the login event view
 *
 */

public final class ProcessLoginEventView extends AbstractAttributeReleaseAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(ProcessLoginEventView.class);


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

        final String principalName =
                IdPHelper.getPrincipalName(profileRequestContext);

        final String sectionParameter = request.getParameter("section");
        final boolean loginEventSection =
                (sectionParameter.equals("loginEvent"));
        final boolean serviceSection = (sectionParameter.equals("service"));

        final boolean helpButton = (request.getParameter("help") != null);

        final AttributeReleaseModule attributeReleaseModule =
                IdPHelper.attributeReleaseModule;

        @Nonnull final String relyingPartyId =
                (String) flowScope.get("relyingParty");

        final boolean backButton = (request.getParameter("back") != null);
        final boolean saveButton = (request.getParameter("save") != null);

        if (backButton) {
            final EventContext event = new EventContext();
            event.setEvent("proceed");
            profileRequestContext.addSubcontext(event);
            return;
        }

        if (saveButton) {
            final String forceShowInterface =
                    (request.getParameter("forceShowInterface"));
            final boolean forceShow = (forceShowInterface.equals("yes"));

            attributeReleaseModule.setForceShowInterface(principalName,
                    relyingPartyId, forceShow);

            final List<Attribute> attributes =
                    (List<Attribute>) flowScope.get("attributes");

            Map<String, Boolean> consentByAttribute =
                    (Map<String, Boolean>) flowScope.get("consentByAttribute");
            final DateTime timestamp = (DateTime) flowScope.get("timestamp");

            if (consentByAttribute == null) {
                log.error("consentByAttribute is null");
                consentByAttribute = new HashMap<String, Boolean>();
            }
            final Enumeration e = request.getParameterNames();
            while (e.hasMoreElements()) {
                final String param = (String) e.nextElement();
                if (param.startsWith("input-")) {
                    log.debug("Processing parameter {}", param);
                    final String attributeName = (param.split("^input-"))[1];
                    consentByAttribute.put(attributeName,
                            request.getParameter(param).equals("1") ? true
                                    : false);
                }
            }
            log.debug("Configure consent for user {} to {}.", principalName,
                    relyingPartyId);

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
            // XXX check correctness
            attributeReleaseModule.denyAttributeRelease(principalName,
                    relyingPartyId, deniedAttributes);
            attributeReleaseModule.consentAttributeRelease(principalName,
                    relyingPartyId, consentedAttributes);

            int remindAfter = 1;
            final String reminderIntervalString =
                    (request.getParameter("reminderInterval"));

            try {
                remindAfter = Integer.parseInt(reminderIntervalString);
            } catch (final NumberFormatException x) {
            }
            final ReminderInterval reminderInterval =
                    new ReminderInterval(principalName, relyingPartyId,
                            remindAfter, 0);
            attributeReleaseModule.updateReminderInterval(reminderInterval);

            final EventContext event = new EventContext();
            event.setEvent("proceed");
            profileRequestContext.addSubcontext(event);
            return;
        }

        log.warn("ProcessLoginEvent fallthrough");

        final EventContext event = new EventContext();
        event.setEvent("proceed");
        profileRequestContext.addSubcontext(event);
        return;
    }
}