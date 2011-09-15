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