/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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