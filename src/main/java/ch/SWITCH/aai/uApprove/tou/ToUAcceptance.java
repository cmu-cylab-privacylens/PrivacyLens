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

package ch.SWITCH.aai.uApprove.tou;

import org.joda.time.DateTime;

import edu.cmu.ece.PrivacyLens.Util;

/** Represents a terms of use acceptance. */

public class ToUAcceptance {

    /** The terms of use version. */
    private final String version;

    /** The terms of use fingerpint. */
    private final String fingerprint;

    /** The terms of use acceptance date. */
    private final DateTime acceptanceDate;

    /**
     * Constructs a terms of use acceptance using version, fingerprint and an acceptance date.
     * 
     * @param version The version.
     * @param fingerprint The fingerprint.
     * @param acceptanceDate The acceptance date.
     */
    public ToUAcceptance(final String version, final String fingerprint, final DateTime acceptanceDate) {
        this.version = version;
        this.fingerprint = fingerprint;
        this.acceptanceDate = acceptanceDate;
    }

    /**
     * Constructs a terms of use acceptance using a ToU and an acceptance date.
     * 
     * @param tou The {@see Tou}.
     * @param acceptanceDate The acceptance date.
     */
    public ToUAcceptance(final ToU tou, final DateTime acceptanceDate) {
        this(tou.getVersion(), Util.hash(tou.getContent()), acceptanceDate);
    }

    /**
     * Gets the version.
     * 
     * @return Returns the version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the fingerprint.
     * 
     * @return Returns the fingerprint.
     */
    public String getFingerprint() {
        return fingerprint;
    }

    /**
     * Gets the acceptance date.
     * 
     * @return Returns the acceptance date.
     */
    public DateTime getAcceptanceDate() {
        return acceptanceDate;
    }

}
