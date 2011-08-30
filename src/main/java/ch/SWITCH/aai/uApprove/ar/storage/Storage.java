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

import ch.SWITCH.aai.uApprove.ar.AttributeRelease;

/** Storage interface for user consent. */
public interface Storage {

    /**
     * Reads the attribute releases from the storage for a specific user and relying party.
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     * @return Returns a collection of attribute releases, might be empty but never null.
     */
    List<AttributeRelease> readAttributeReleases(final String userId, final String relyingPartyId);

    /**
     * Deletes the attribute releases from the storage for a specific user and relying party.
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     */
    void deleteAttributeReleases(final String userId, final String relyingPartyId);

    /**
     * Checks if the storage contains attribute releases for a specific user andrelying party
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     * @return Returns true if the storage contains attribute releases, false otherwise.
     */
    boolean containsAttributeReleases(final String userId, final String relyingPartyId);

    /**
     * Updates the attribute releases for a specific user and relying party.
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     * @param attributeRelease The attribute release.
     */
    void updateAttributeRelease(final String userId, String relyingPartyId, final AttributeRelease attributeRelease);

    /**
     * Creates an attribute releases for a specific user and relying party.
     * 
     * @param userId The user id.
     * @param relyingPartyId The relying party id.
     * @param attributeRelease The attribute release.
     */
    void createAttributeRelease(final String userId, final String relyingPartyId,
            final AttributeRelease attributeRelease);

}