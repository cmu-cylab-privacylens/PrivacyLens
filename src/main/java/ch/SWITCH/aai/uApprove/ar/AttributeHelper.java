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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import ch.SWITCH.aai.uApprove.Util;

/**
 *
 */
public class AttributeHelper {

    private List<String> blacklist;

    private List<String> ordering;

    /**
     * Constructor.
     * 
     */
    public AttributeHelper() {
        blacklist = Collections.emptyList();
        ordering = Collections.emptyList();
    }

    /**
     * @param blacklist The blacklist to set.
     */
    public void setBlacklist(final String blacklist) {
        this.blacklist = Util.stringToList(blacklist);
    }

    /**
     * @param ordering The ordering to set.
     */
    public void setOrdering(final String ordering) {
        this.ordering = Util.stringToList(ordering);;
    }

    public void removeBlacklistedAttributes(final List<Attribute> attributes) {
        final Iterator<Attribute> iterator = attributes.iterator();
        while (iterator.hasNext()) {
            if (blacklist.contains(iterator.next().getId())) {
                iterator.remove();
            }
        }
    }

    public void sortAttributes(final List<Attribute> attributes) {
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
}
