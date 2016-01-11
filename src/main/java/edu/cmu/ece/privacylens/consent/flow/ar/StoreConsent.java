/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2016 Carnegie Mellon University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the names of the copyright holders nor the
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

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import edu.cmu.ece.privacylens.Consent;
import edu.cmu.ece.privacylens.IdPHelper;
import edu.cmu.ece.privacylens.ar.Attribute;
import edu.cmu.ece.privacylens.ar.AttributeReleaseModule;
import edu.cmu.ece.privacylens.ar.ReminderInterval;
import edu.cmu.ece.privacylens.consent.flow.AbstractConsentAction;
import edu.cmu.ece.privacylens.context.ConsentContext;
import edu.cmu.ece.privacylens.context.ReminderContext;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * Store consent data in database
 * CHANGEME implement this.
 *
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @post See above.
 */
public class StoreConsent extends AbstractConsentAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(StoreConsent.class);

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
    }

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        final AttributeReleaseModule arm = IdPHelper.attributeReleaseModule;
        final String relyingPartyId =
                IdPHelper.getRelyingPartyId(profileRequestContext);
        final String principalName =
                IdPHelper.getPrincipalName(profileRequestContext);

        final RequestContext requestContext =
                RequestContextHolder.getRequestContext();

        final MutableAttributeMap<Object> flowScope =
                requestContext.getFlowScope();

        final List<Attribute> attributes =
                (List<Attribute>) flowScope.get("attributes");

        final ConsentContext consentContext = getConsentContext();

        final Map<String, Consent> consents =
                consentContext.getCurrentConsents();
        final ReminderContext reminderContext =
                consentContext.getReminderContext();

        try {
            final int remindAfter =
                    reminderContext.getTargetReminder().getVisits();
            int currentCount = reminderContext.getCurrentReminder().getVisits();

            currentCount = currentCount % remindAfter;

            final ReminderInterval reminderInterval =
                    new ReminderInterval(principalName, relyingPartyId,
                            remindAfter, currentCount);
            arm.updateReminderInterval(reminderInterval);
            log.debug("Stored reminder interval for principal: {} rpid: {}",
                    principalName, relyingPartyId);

            final List<Attribute> consentedAttributes =
                    new ArrayList<Attribute>();
            for (final Attribute attribute : attributes) {
                final String attributeId = attribute.getId();
                final Consent consent = consents.get(attributeId);
                if (consent == null || !consent.isApproved()) {
                    continue;
                }
                consentedAttributes.add(attribute);
            }
            arm.consentAttributeRelease(principalName, relyingPartyId,
                    consentedAttributes);
            log.debug("Stored consents for principal: {} rpid: {}",
                    principalName, relyingPartyId);

        } catch (final Throwable e) {
            log.error("{} Unable to write consent to storage", getLogPrefix(),
                    e);
        }

        log.debug("StoreConsent finished");
    }

}
