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

package ch.SWITCH.aai.uApprove.ar;

import org.joda.time.DateTime;

/** Represents an attribute release consent. */
public class AttributeReleaseConsent {

    /** The attribute id. */
    private final String attributeId;

    /** An hash over all attribute values. */
    private final String valuesHash;

    /** A timestamp when consent for this attribute release was given. */
    private final DateTime consentDate;

    /**
     * Constructs a @see AttributeReleaseConsent.
     * 
     * @param attributeId The id of the attribute.
     * @param valuesHash The hashed values.
     * @param consentDate The timestamp for this @see AttributeRelease.
     */
    public AttributeReleaseConsent(final String attributeId, final String valuesHash, final DateTime consentDate) {
        this.attributeId = attributeId;
        this.valuesHash = valuesHash;
        this.consentDate = consentDate;
    }

    /**
     * Constructs a @see AttributeReleaseConsent.
     * 
     * @param attribute The @see Attribute.
     * @param consentDate The timestamp for this @see AttributeRelease.
     */
    public AttributeReleaseConsent(final Attribute attribute, final DateTime consentDate) {
        this.attributeId = attribute.getId();
        this.valuesHash = AttributeReleaseHelper.hashValues(attribute.getValues());
        this.consentDate = consentDate;
    }

    /**
     * Gets the attribute id.
     * 
     * @return Returns the id.
     */
    public String getAttributeId() {
        return attributeId;
    }

    /**
     * Gets the hash value of the attribute values.
     * 
     * @return Returns the values hash.
     */
    public String getValuesHash() {
        return valuesHash;
    }

    /**
     * Gets the timestamp when consent for this attribute release was given.
     * 
     * @return Returns the date.
     */
    public DateTime getDate() {
        return consentDate;
    }

}
