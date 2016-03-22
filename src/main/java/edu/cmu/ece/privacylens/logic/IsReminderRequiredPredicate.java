/*
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

package edu.cmu.ece.privacylens.logic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import edu.cmu.ece.privacylens.Reminder;
import edu.cmu.ece.privacylens.consent.flow.ConsentFlowDescriptor;
import edu.cmu.ece.privacylens.context.ConsentContext;
import edu.cmu.ece.privacylens.context.ReminderContext;
import net.shibboleth.idp.consent.logic.impl.FlowDescriptorLookupFunction;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * Predicate that returns whether a reminder is required
 */
public class IsReminderRequiredPredicate implements
        Predicate<ProfileRequestContext> {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory
            .getLogger(IsReminderRequiredPredicate.class);

    /** Consent context lookup strategy. */
    @Nonnull
    private Function<ProfileRequestContext, ConsentContext> consentContextLookupStrategy;

    /** Consent flow descriptor lookup strategy. */
    @Nonnull
    private Function<ProfileRequestContext, ConsentFlowDescriptor> consentFlowDescriptorLookupStrategy;

    /** Constructor. */
    public IsReminderRequiredPredicate() {
        consentContextLookupStrategy =
                new ChildContextLookup<>(ConsentContext.class);
        consentFlowDescriptorLookupStrategy =
                new FlowDescriptorLookupFunction<>(ConsentFlowDescriptor.class);
    }

    /**
     * Set the consent context lookup strategy.
     *
     * @param strategy consent context lookup strategy
     */
    public
            void
            setConsentContextLookupStrategy(
                    @Nonnull final Function<ProfileRequestContext, ConsentContext> strategy) {
        consentContextLookupStrategy =
                Constraint.isNotNull(strategy,
                        "Consent context lookup strategy cannot be null");
    }

    /**
     * Set the consent flow descriptor lookup strategy.
     *
     * @param strategy consent flow descriptor lookup strategy
     */
    public
            void
            setConsentFlowDescriptorLookupStrategy(
                    @Nonnull final Function<ProfileRequestContext, ConsentFlowDescriptor> strategy) {
        consentFlowDescriptorLookupStrategy =
                Constraint
                        .isNotNull(strategy,
                                "Consent flow descriptor lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public boolean apply(@Nullable final ProfileRequestContext input) {
        if (input == null) {
            log.debug("Reminder is not required, no profile request context");
            return false;
        }

        final ConsentContext consentContext = consentContextLookupStrategy.apply(input);

        if (consentContext == null) {
            log.debug("Reminder is not required, no consent context");
            return false;
        }
        final ReminderContext reminderContext =
                consentContext.getReminderContext();

        if (reminderContext == null) {
            log.debug("Reminder is not required, no reminder context");
            return false;
        }

        // CHANGEME implement
        // privacy lens
        // if reminder interval hits, show reminder
        // else not

        final Reminder currentReminder = reminderContext.getCurrentReminder();
        final Reminder targetReminder = reminderContext.getTargetReminder();
        int currentVisits = currentReminder.getVisits();
        final int targetVisits = targetReminder.getVisits();

        // record new visit
        currentVisits++;
        currentReminder.setVisits(currentVisits);

        // CHANGEME mod the visits?
        if (currentVisits >= targetVisits) {
            log.debug(
                    "Reminder is required. Target visits: {}, current visits: {}",
                    targetVisits, currentVisits);
            return true;
        }

        log.debug(
                "Reminder is not required. Target visits: {}, current visits: {}",
                targetVisits, currentVisits);
        return false;
    }

}
