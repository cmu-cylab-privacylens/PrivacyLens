/*
 * Copyright (c) 2011, SWITCH
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

package ch.SWITCH.aai.uApprove.ar.storage;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.SWITCH.aai.uApprove.ar.AttributeReleaseChoice;
import ch.SWITCH.aai.uApprove.ar.LoginEvent;
import ch.SWITCH.aai.uApprove.ar.LoginEventDetail;
import ch.SWITCH.aai.uApprove.ar.ReminderInterval;

/**
 * No operation implementation of the attribute release consent storage interface. I.e., this implementation will not
 * persist any data nor get any data.
 */
public class NOPStorage implements Storage {

    /** Class logger. */
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(NOPStorage.class);

    /** {@inheritDoc} */
    public void createAttributeReleaseChoice(final String userId, final String relyingPartyId,
            final AttributeReleaseChoice attributeReleaseConsent) {
        return;
    }

    /** {@inheritDoc} */
    public List<AttributeReleaseChoice> readAttributeReleaseChoices(final String userId, final String relyingPartyId) {
        return Collections.emptyList();
    }

    /** {@inheritDoc} */
    public void updateAttributeReleaseChoice(final String userId, final String relyingPartyId,
            final AttributeReleaseChoice attributeReleaseConsent) {
        return;
    }

    /** {@inheritDoc} */
    public void deleteAttributeReleaseChoices(final String userId, final String relyingPartyId) {
        return;
    }

    /** {@inheritDoc} */
    public boolean containsAttributeReleaseChoice(final String userId, final String relyingPartyId,
            final String attributeId) {
        return false;
    }

    /** {@inheritDoc} */
    public void createLoginEvent(final LoginEvent loginEvent, final LoginEventDetail loginEventDetail) {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public LoginEvent readLoginEvent(final String loginEventId) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    public LoginEventDetail readLoginEventDetail(final LoginEvent loginEvent) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    public void deleteLoginEvent(final LoginEvent loginEvent) {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public List<LoginEvent> listLoginEvents(final String userId, final String relyingPartyId, final int limit) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    public List<String> listRelyingParties(final String userId, final int limit) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    public void
            createForceShowInterface(final String userId, final String relyingPartyId, final boolean forceShow) {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public boolean readForceShowInterface(final String userId, final String relyingPartyId) {
        // TODO Auto-generated method stub
        return false;
    }

    /** {@inheritDoc} */
    public void updateForceShowInterface(final String userId, final String relyingPartyId, final boolean forceShow) {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public void createReminderInterval(ReminderInterval reminderInterval) {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public ReminderInterval readReminderInterval(final String userId, final String relyingPartyId) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    public void updateReminderInterval(ReminderInterval reminderInterval) {
        // TODO Auto-generated method stub

    }

}
