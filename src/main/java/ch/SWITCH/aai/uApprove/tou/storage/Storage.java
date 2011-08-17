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

package ch.SWITCH.aai.uApprove.tou.storage;

import ch.SWITCH.aai.uApprove.tou.ToUAcceptance;

/** Storage interface for terms of use. */
public interface Storage {

    /**
     * Creates a terms of use acceptance.
     * 
     * @param userId The user id.
     * @param touAcceptance terms of use acceptance.
     */
    public void createToUAcceptance(final String userId, final ToUAcceptance touAcceptance);

    /**
     * Updates a terms of use acceptance.
     * 
     * @param userId The user id.
     * @param touAcceptance terms of use acceptance.
     */
    public void updateToUAcceptance(final String userId, final ToUAcceptance touAcceptance);

    /**
     * Reads a terms of use acceptance.
     * 
     * @param userId The user id.
     * @param version Terms of use version.
     * @return Returns a terms of use acceptance, might be empty but never null.
     */
    public ToUAcceptance readToUAcceptance(final String userId, final String version);

    /**
     * Checks if the storage contains a terms of use acceptance for a specific user.
     * 
     * @param userId The user id.
     * @param version Terms of use version.
     * @return Returns true if the storage contains the terms of use acceptance, false otherwise.
     */
    public boolean containsToUAcceptance(final String userId, final String version);
}