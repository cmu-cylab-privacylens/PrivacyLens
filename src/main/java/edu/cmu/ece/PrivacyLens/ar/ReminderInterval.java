/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2013 Carnegie Mellon University
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

package edu.cmu.ece.PrivacyLens.ar;

/** Represents an reminder interval object. */
public class ReminderInterval {

    /** The current turn number. */
    private int currentCount;

    /** The chosen turn to remind after. */
    private int remindAfter;

    /** The service provider for whom this is valid. */
    private final String relyingPartyId;

    /** The user id for whom this is valid. */
    private final String userId;

    /**
     * Constructs a @see ReminderInterval.
     * 
     * @param userId The id of the user.
     * @param relyingPartyId The service provider.
     * @param remindAfter The chosen reminder interval.
     */
    public ReminderInterval(final String userId, final String relyingPartyId, final int remindAfter,
            final int currentCount) {
        this.relyingPartyId = relyingPartyId;
        this.userId = userId;
        if (remindAfter == 0) {
            throw new InternalError("Not accepting 0 for remindAfter");
        }
        this.remindAfter = remindAfter;
        this.currentCount = currentCount;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "ReminderInterval [currentCount=" + currentCount + ", remindAfter=" + remindAfter + ", relyingPartyId="
                + relyingPartyId + ", userId=" + userId + "]";
    }

    /**
     * @return Returns the relyingPartyId.
     */
    public String getRelyingPartyId() {
        return relyingPartyId;
    }

    /**
     * @return Returns the userId.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @return Returns the currentCount.
     */
    public int getCurrentCount() {
        return currentCount;
    }

    /**
     * @param currentCount The currentCount to set.
     */
    public void setCurrentCount(final int currentCount) {
        this.currentCount = currentCount;
    }

    /**
     * @return Returns the remindAfter.
     */
    public int getRemindAfter() {
        return remindAfter;
    }

    /**
     * @param remindAfter The remindAfter to set.
     */
    public void setRemindAfter(final int remindAfter) {
        if (remindAfter == 0) {
            throw new InternalError("Not accepting 0 for remindAfter");
        }
        this.remindAfter = remindAfter;
    }

}