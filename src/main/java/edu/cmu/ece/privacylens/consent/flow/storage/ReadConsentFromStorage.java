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

package edu.cmu.ece.privacylens.consent.flow.storage;

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
import edu.cmu.ece.privacylens.Reminder;
import edu.cmu.ece.privacylens.ar.Attribute;
import edu.cmu.ece.privacylens.ar.AttributeReleaseModule;
import edu.cmu.ece.privacylens.ar.ReminderInterval;
import edu.cmu.ece.privacylens.context.ReminderContext;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;

/**
 * Consent action which reads consent records from storage and adds the serialized consent records to the consent
 * context as previous consents.
 *
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 */
public class ReadConsentFromStorage extends AbstractConsentStorageAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ReadConsentFromStorage.class);

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        // context: intercept/privacy-lens
        // key: stroucki:https://scalepriv.ece.cmu.edu/shibboleth
        final String context = getStorageContext();
        final String key = getStorageKey();

        try {

            final AttributeReleaseModule arm =
                    IdPHelper.getAttributeReleaseModule();
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

            final Map<String, Consent> consents =
                    arm.getAttributeConsent(principalName, relyingPartyId,
                            attributes);
            /*
            final StorageRecord storageRecord = getStorageService().read(context, key);
            log.debug("{} Read storage record '{}' with context '{}' and key '{}'", getLogPrefix(), storageRecord,
                    context, key);

            if (storageRecord == null) {
                log.debug("{} No storage record for context '{}' and key '{}'", getLogPrefix(), context, key);
                return;
            }

            final Map<String, Consent> consents =
                    (Map<String, Consent>) storageRecord.getValue(getStorageSerializer(), context, key);
            */
            getConsentContext().getPreviousConsents().putAll(consents);
            log.debug("Added previous consents from storage");
            final ReminderInterval interval =
                    arm.getReminderInterval(principalName, relyingPartyId);
            final ReminderContext reminderContext =
                    getConsentContext().getReminderContext();
            final Reminder currentReminder =
                    reminderContext.getCurrentReminder();
            final Reminder targetReminder = reminderContext.getTargetReminder();
            currentReminder.setVisits(interval.getCurrentCount());
            targetReminder.setVisits(interval.getRemindAfter());
            log.debug("Added reminder data from storage");
        } catch (final Throwable e) {
            log.error("{} Unable to read consent from storage", getLogPrefix(), e);
        }
    }

}
