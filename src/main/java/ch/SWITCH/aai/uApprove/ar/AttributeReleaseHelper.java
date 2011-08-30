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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.joda.time.DateTime;

import ch.SWITCH.aai.uApprove.Util;

/**
 *
 */
public class AttributeReleaseHelper {

    /**
     * Creates a collection of @see AttributeRelease.
     * 
     * @param attributes The attributes from where the attribute releases should created.
     * @param date The consent date for the attributes.
     * @return Returns a collection of attribute releases
     */
    public static Collection<AttributeRelease> createAttributeReleases(final Collection<Attribute> attributes,
            final DateTime date) {
        final Collection<AttributeRelease> attributeReleases = new HashSet<AttributeRelease>();
        for (final Attribute attribute : attributes) {
            attributeReleases.add(new AttributeRelease(attribute, date));
        }
        return attributeReleases;
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

    public static List<Attribute> removeBlacklistedAttributes(final List<Attribute> attributes,
            final List<String> blacklist) {
        final List<Attribute> notBlacklisted = new ArrayList<Attribute>();
        for (final Attribute attribute : attributes) {
            if (!blacklist.contains(attribute.getId())) {
                notBlacklisted.add(attribute);
            }
        }
        return notBlacklisted;
    }

    public static void sortAttributes(final List<Attribute> attributes, final List<String> ordering) {
        Collections.sort(attributes, new Comparator<Attribute>() {
            public int compare(final Attribute attribute1, final Attribute attribute2) {
                int last = ordering.size();
                int rank1 = ordering.indexOf(attribute1.getId());
                int rank2 = ordering.indexOf(attribute2.getId());

                if (rank2 < 0) {
                    rank2 = last;
                    last++;
                }

                if (rank1 < 0) {
                    rank1 = last;
                }
                return rank1 - rank2;
            }
        });
    }

    public static boolean approvedAttributes(final List<Attribute> attributes,
            final List<AttributeRelease> attributeReleases) {
        for (final Attribute attribute : attributes) {
            boolean approved = false;
            for (final AttributeRelease attributeRelease : attributeReleases) {
                if (attributeRelease.contains(attribute)) {
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
}
