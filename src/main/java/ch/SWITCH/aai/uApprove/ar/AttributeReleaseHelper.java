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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import ch.SWITCH.aai.uApprove.Util;

/**
 * Attribute Release Helper.
 */
public final class AttributeReleaseHelper {

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
    public static Collection<AttributeReleaseConsent> createAttributeReleaseConsents(
            final Collection<Attribute> attributes, final DateTime date) {
        final Collection<AttributeReleaseConsent> attributeReleaseConsents = new HashSet<AttributeReleaseConsent>();
        for (final Attribute attribute : attributes) {
            attributeReleaseConsents.add(new AttributeReleaseConsent(attribute, date));
        }
        return attributeReleaseConsents;
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
            final AttributeReleaseConsent attributeReleaseConsent, final boolean compareAttributeValues) {
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
            final List<AttributeReleaseConsent> attributeReleaseConsents, final boolean compareAttributeValues) {
        for (final Attribute attribute : attributes) {
            boolean approved = false;
            for (final AttributeReleaseConsent attributeRelease : attributeReleaseConsents) {
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
