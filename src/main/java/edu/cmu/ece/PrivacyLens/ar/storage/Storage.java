/*
 * Copyright (c) 2011, SWITCH
 * Copyright (c) 2011, Carnegie Mellon University
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

package edu.cmu.ece.PrivacyLens.ar.storage;

import java.util.List;

import edu.cmu.ece.PrivacyLens.ar.AttributeReleaseChoice;
import edu.cmu.ece.PrivacyLens.ar.LoginEvent;
import edu.cmu.ece.PrivacyLens.ar.LoginEventDetail;
import edu.cmu.ece.PrivacyLens.ar.ReminderInterval;

/** Storage interface for user consent. */
public interface Storage {

    /**
     * Creates a login event with associated detail
     * 
     * @param loginEvent The login event
     * @param loginEventDetail The login event detail
     */
    public void createLoginEvent(LoginEvent loginEvent, LoginEventDetail loginEventDetail);

    /**
     * Retrieves login event based on a login event id
     * 
     * @param loginEventId The login event
     */
    public LoginEvent readLoginEvent(String loginEventId);

    /**
     * Retrieves login event detail based on a login event
     * 
     * @param loginEvent The login event
     */
    public LoginEventDetail readLoginEventDetail(LoginEvent loginEvent);

    /**
     * Deletes a login event with associated detail Should it return something other than void? int/bool?
     * 
     * @param loginEvent The login event
     */
    public void deleteLoginEvent(LoginEvent loginEvent);

    /**
     * Retrieve login events based on constraints
     * 
     * @param userId The user id
     * @param relyingPartyId The relying party id
     * @param limit Limit of how many records to retrieve
     */
    public List<LoginEvent> listLoginEvents(String userId, String relyingPartyId, int limit);

    /**
     * Retrieve service provider list based on constraints
     * 
     * @param userId The user id
     * @param limit Limit of how many records to retrieve
     */
    public List<String> listRelyingParties(String userId, int limit);

    // end login events

    /**
     * Creates an attribute release consent for a specific user and relying party.
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     * @param attributeReleaseChoice The attribute release consent.
     */
    public void createAttributeReleaseChoice(final String userId, final String relyingPartyId,
            final AttributeReleaseChoice attributeReleaseChoice);

    /**
     * Reads the attribute release consents from the storage for a specific user and relying party.
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     * @return Returns a collection of attribute release consents, might be empty but never null.
     */
    public List<AttributeReleaseChoice> readAttributeReleaseChoices(final String userId, final String relyingPartyId);

    /**
     * Updates the attribute release consent for a specific user and relying party.
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     * @param attributeReleaseConsent The attribute release consent.
     */
    public void updateAttributeReleaseChoice(final String userId, String relyingPartyId,
            final AttributeReleaseChoice attributeReleaseChoice);

    /**
     * Deletes the attribute release consents from the storage for a specific user and relying party.
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     */
    public void deleteAttributeReleaseChoices(final String userId, final String relyingPartyId);

    // should provide for deleting single consents

    /**
     * Checks if the storage contains attribute release consent for a specific user, relying party and attribute.
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     * @param attributeId The attribute id.
     * @return Returns true if the storage contains attribute release choice, false otherwise.
     */
    public boolean containsAttributeReleaseChoice(final String userId, final String relyingPartyId,
            final String attributeId);

    /**
     * Create a record of {user, sp} -> show interface
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     * @param forceShow Flag whether to show interface
     */
    public void createForceShowInterface(final String userId, final String relyingPartyId, boolean forceShow);

    /**
     * Checks if user should be presented with the interface
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     * @param attributeId The attribute id.
     * @return Should the user be presented with the interface
     */
    public boolean readForceShowInterface(final String userId, final String relyingPartyId);

    /**
     * Update whether user should be presented with the interface
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     * @param forceShow Flag whether to show interface
     */
    public void updateForceShowInterface(final String userId, final String relyingPartyId, final boolean forceShow);

    /**
     * Delete whether user should be presented with the interface
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     */
    //public boolean deleteForceShowInterface(final String userId, final String relyingPartyId);

    /**
     * Create a record of {user, sp} -> show interface
     * 
     * @param reminderInterval TODO
     */
    public void createReminderInterval(ReminderInterval reminderInterval);

    /**
     * Checks if user should be presented with the interface
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     * @param attributeId The attribute id.
     * @return The Reminder Interval object
     */
    public ReminderInterval readReminderInterval(final String userId, final String relyingPartyId);

    /**
     * Update whether user should be presented with the interface
     * @param reminderInterval TODO
     */
    public void updateReminderInterval(ReminderInterval reminderInterval);

    /**
     * Delete whether user should be presented with the interface
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     */
    //public boolean deleteReminderInterval(ReminderInterval reminderInterval);

}
