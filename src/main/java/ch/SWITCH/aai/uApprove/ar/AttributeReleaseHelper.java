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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.SWITCH.aai.uApprove.Util;

/**
 * Attribute Release Helper.
 */
public final class AttributeReleaseHelper {

    private static final Logger logger = LoggerFactory.getLogger(AttributeReleaseHelper.class);

    /** Default constructor for utility classes is private. */
    private AttributeReleaseHelper() {
    }

    /**
     * Creates a collection of @see AttributeReleaseConsent.
     * 
     * @param attributes The attributes from where the attribute releases should created.
     * @param date The consent date for the attributes.
     * @return Returns a collection of attribute release consents
     */
    public static Collection<AttributeReleaseChoice> createAttributeReleaseChoices(
            final Collection<Attribute> attributes, final DateTime date) {
        final Collection<AttributeReleaseChoice> attributeReleaseChoices = new HashSet<AttributeReleaseChoice>();
        for (final Attribute attribute : attributes) {
            attributeReleaseChoices.add(new AttributeReleaseChoice(attribute, date, true));
        }
        return attributeReleaseChoices;
    }

    /**
     * Hashes the values of a given @see Attribute.
     * 
     * @param values The values
     * @return Returns the hash of all attribute values.
     */
    public static String hashValues(final Collection<String> values) {
        final List<String> valueList = new ArrayList<String>(values);
        Collections.sort(valueList);

        final StringBuilder stringBuilder = new StringBuilder();
        for (final String value : valueList) {
            stringBuilder.append(value).append(";");
        }
        return Util.hash(stringBuilder.toString());
    }

    /**
     * Checks whether the attribute release consent contains the attribute. Optionally compares the attribute values.
     * 
     * @param attribute The attribute.
     * @param attributeReleaseConsent The attribute release consent.
     * @param compareAttributeValues Indicates whether to compare the attribute values.
     * @return Returns true if the attribute release contains the attribute.
     */
    public static boolean approvedAttribute(final Attribute attribute,
            final AttributeReleaseChoice attributeReleaseConsent, final boolean compareAttributeValues) {
        if (StringUtils.equals(attributeReleaseConsent.getAttributeId(), attribute.getId())) {
            if (compareAttributeValues) {
                return StringUtils.equals(attributeReleaseConsent.getValuesHash(), hashValues(attribute.getValues()));
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Calls for all attributes and attribute release consents the approvedAttribute method.
     * 
     * @param attributes The attributes
     * @param attributeReleaseConsents The attribute release consents.
     * @param compareAttributeValues Indicates whether to compare the attribute values.
     * @return Returns true if all attributes are contained in of the attribute releases.
     */
    public static boolean approvedAttributes(final List<Attribute> attributes,
            final List<AttributeReleaseChoice> attributeReleaseConsents, final boolean compareAttributeValues) {
        for (final Attribute attribute : attributes) {
            boolean approved = false;
            for (final AttributeReleaseChoice attributeRelease : attributeReleaseConsents) {
                if (approvedAttribute(attribute, attributeRelease, compareAttributeValues)) {
                    approved = true;
                    break;
                }
            }
            if (!approved) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tries to resolve the FDQN from the relying party id.
     * 
     * @param entityId The relying party id.
     * @return Returns the sp.example.org component out of https://sp.example.org/shibboleth or the sp.example.org
     *         component out of urn:mace:federation.org:sp.example.org or the entityId.
     */
    public static String resolveFqdn(final String entityId) {

        final Pattern urlPattern = Pattern.compile("^https://(.+)/shibboleth$");
        final Matcher matcher = urlPattern.matcher(entityId);
        if (matcher.find()) {
            return matcher.group(1);
        }

        if (entityId.matches("^urn:mace:.+")) {
            final String[] temp = entityId.split(":");
            return temp[temp.length - 1];
        }

        return entityId;
    }
}
