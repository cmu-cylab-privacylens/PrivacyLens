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

package ch.SWITCH.aai.uApprove.ar;

import org.joda.time.DateTime;

/** Represents an attribute release. */
public class AttributeRelease {

    /** The attribute id. */
    private final String attributeId;

    /** An hash over all attribute values. */
    private final String valuesHash;

    /** A timestamp when consent for this attribute release was given. */
    private final DateTime consentDate;

    /**
     * Constructs a @see AttributeRelease.
     * 
     * @param attributeId The id of the attribute.
     * @param valuesHash The hashed values.
     * @param consentDate The timestamp for this @see AttributeRelease.
     */
    public AttributeRelease(final String attributeId, final String valuesHash, final DateTime consentDate) {
        this.attributeId = attributeId;
        this.valuesHash = valuesHash;
        this.consentDate = consentDate;
    }

    /**
     * Constructs a @see AttributeRelease.
     * 
     * @param attribute The @see Attribute.
     * @param consentDate The timestamp for this @see AttributeRelease.
     */
    public AttributeRelease(final Attribute attribute, final DateTime consentDate) {
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

    /**
     * Checks if an @see AttributeRelease contains a given @see Attribute.
     * 
     * @param attribute The @see Attribute to check for.
     * @return Returns true if this @see AttributeRelease contains the given attribute.
     */
    public boolean contains(final Attribute attribute) {
        return attributeId.equals(attribute.getId())
                && valuesHash.equals(AttributeReleaseHelper.hashValues(attribute.getValues()));
    }
}
