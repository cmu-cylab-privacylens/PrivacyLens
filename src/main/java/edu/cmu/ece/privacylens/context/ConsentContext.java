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

package edu.cmu.ece.privacylens.context;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;
import org.opensaml.messaging.context.BaseContext;

import com.google.common.base.MoreObjects;

import edu.cmu.ece.privacylens.Consent;
import net.shibboleth.utilities.java.support.annotation.constraint.Live;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;

/**
 * Context representing the state of a consent flow.
 *
 * Holds consent previously given as well as obtained from user input.
 */
public class ConsentContext extends BaseContext {

    /** Map of previous consent read from storage and keyed by consent id. */
    @Nonnull @NonnullElements @Live private final Map<String, Consent> previousConsents;

    /** Map of current consent extracted from user input and keyed by consent id. */
    @Nonnull @NonnullElements @Live private final Map<String, Consent> currentConsents;

    /** Timestamp of current consent action */
    @Nonnull
    private final DateTime timestamp;

    /** Data about reminder choices. */
    @Nonnull
    @NonnullElements
    @Live
    private final ReminderContext reminderContext;

    /** Constructor. */
    public ConsentContext() {
        reminderContext = new ReminderContext();
        previousConsents = new LinkedHashMap<>();
        currentConsents = new LinkedHashMap<>();
        timestamp = new DateTime();
    }

    /**
     * Get map of current consent extracted from user input and keyed by consent id.
     *
     * @return map of current consent extracted from user input and keyed by consent id
     */
    @Nonnull @NonnullElements @Live public Map<String, Consent> getCurrentConsents() {
        return currentConsents;
    }

    /**
     * Get map of previous consent read from storage and keyed by consent id.
     *
     * @return map of previous consent read from storage and keyed by consent id
     */
    @Nonnull @NonnullElements @Live public Map<String, Consent> getPreviousConsents() {
        return previousConsents;
    }

    /**
     * Return timestamp
     *
     * @return the timestamp
     */
    @Nonnull
    public DateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Get reminder data.
     *
     * @return a ReminderContext
     */
    @Nonnull
    @NonnullElements
    @Live
    public ReminderContext getReminderContext() {
        return reminderContext;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("reminderContext", reminderContext)
                .add("previousConsents", previousConsents)
                .add("chosenConsents", currentConsents)
                .add("timestamp", timestamp)
                .toString();
    }

}
