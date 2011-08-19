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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 */
public class AttributeList {

    private List<String> blacklist;

    Comparator<Attribute> comperator;

    public AttributeList() {
        blacklist = Collections.emptyList();
    }

    /**
     * 
     */
    public void setOrdering(final String list) {
        final List<String> ordering = Arrays.asList(list.split("\\s+"));

        comperator = new Comparator<Attribute>() {
            public int compare(final Attribute attribute1, final Attribute attribute2) {
                int last = ordering.size();

                int rank1 = ordering.indexOf(attribute1.getId());
                int rank2 = ordering.indexOf(attribute2.getId());

                if (rank2 < 0) {
                    rank2 = last++;
                }

                if (rank1 < 0) {
                    rank1 = last++;
                }
                return rank1 - rank2;
            }
        };
    }

    /**
     * 
     */
    public void setBlacklist(final String list) {
        blacklist = Arrays.asList(list.split("\\s+"));
    }

    /**
     * Removes blacklisted attributes from the attributes which will be released.
     * 
     * @param blacklist A collection of attribute ids which are blacklisted
     * @param allAttributes A collection of attributes which will be released.
     * @return Returns a new collection of attributes without the blacklisted ones.
     */
    public List<Attribute> removeBlacklisted(final List<Attribute> allAttributes) {
        final List<Attribute> attributes = new ArrayList<Attribute>();
        for (final Attribute attribute : allAttributes) {
            if (!blacklist.contains(attribute.getId())) {
                attributes.add(attribute);
            }
        }
        return attributes;
    }

    public void sort(final List<Attribute> attributes) {
        if (comperator != null) {
            Collections.sort(attributes, comperator);
        }
    }
}
