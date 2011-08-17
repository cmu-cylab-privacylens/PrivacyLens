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

package ch.SWITCH.aai.uApprove.tou;

import net.jcip.annotations.ThreadSafe;

import org.joda.time.DateTime;

import ch.SWITCH.aai.uApprove.Util;

/** Represents a terms of use acceptance. */
@ThreadSafe
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
     * Constructs a terms of use acceptance using a terms of use and an acceptance date.
     * 
     * @param tou The {@see Tou}.
     * @param acceptanceDate The acceptance date.
     */
    private ToUAcceptance(final ToU tou, final DateTime acceptanceDate) {
        this.version = tou.getVersion();
        this.fingerprint = Util.fingerprint(tou.getContent());
        this.acceptanceDate = acceptanceDate;
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

    /**
     * Creates a terms of use acceptance.
     * 
     * @param tou The {@see Tou}.
     * @param acceptanceDate The acceptance date.
     * @return Returns a terms of use acceptance.
     */
    public static ToUAcceptance createToUAcceptance(final ToU tou, final DateTime acceptanceDate) {
        return new ToUAcceptance(tou, acceptanceDate);
    }

    /**
     * Creates an empty terms of use acceptance.
     * 
     * @return Returns an empty terms of use acceptance.
     */
    public static ToUAcceptance emptyToUAcceptance() {
        return new ToUAcceptance("", "", null);
    }

    /**
     * Checks if this terms of acceptance contains a specific terms of use.
     * 
     * @param tou The {@see Tou}.
     * @return Returns true if version and fingerprint equals, false otherwise.
     */
    public boolean contains(final ToU tou) {
        return version.equals(tou.getVersion()) && fingerprint.equals(Util.fingerprint(tou.getContent()));
    }
}
