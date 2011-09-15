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

import java.util.List;

import ch.SWITCH.aai.uApprove.ar.AttributeReleaseConsent;

/** Storage interface for user consent. */
public interface Storage {

    /**
     * Reads the attribute release consents from the storage for a specific user and relying party.
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     * @return Returns a collection of attribute release consents, might be empty but never null.
     */
    List<AttributeReleaseConsent> readAttributeReleaseConsents(final String userId, final String relyingPartyId);

    /**
     * Deletes the attribute release consents from the storage for a specific user and relying party.
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     */
    void deleteAttributeReleaseConsents(final String userId, final String relyingPartyId);

    /**
     * Checks if the storage contains attribute release consent for a specific user, relying party and attribute.
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     * @param attributeId The attribute id.
     * @return Returns true if the storage contains attribute release consent, false otherwise.
     */
    boolean containsAttributeReleaseConsent(final String userId, final String relyingPartyId, final String attributeId);

    /**
     * Updates the attribute release consent for a specific user and relying party.
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     * @param attributeReleaseConsent The attribute release consent.
     */
    void updateAttributeReleaseConsent(final String userId, String relyingPartyId,
            final AttributeReleaseConsent attributeReleaseConsent);

    /**
     * Creates an attribute release consent for a specific user and relying party.
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     * @param attributeReleaseConsent The attribute release consent.
     */
    void createAttributeReleaseConsent(final String userId, final String relyingPartyId,
            final AttributeReleaseConsent attributeReleaseConsent);

}