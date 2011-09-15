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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import ch.SWITCH.aai.uApprove.Util;

/**
 * Attribute Processor.
 */
public class AttributeProcessor {

    /** The attribute blacklist. */
    private List<String> blacklist;

    /** The ordering of the attributes. */
    private List<String> order;

    /** Default constructor. */
    public AttributeProcessor() {
        blacklist = Collections.emptyList();
        order = Collections.emptyList();
    }

    /**
     * Sets the blacklist.
     * 
     * @param blacklist The blacklist to set.
     */
    public void setBlacklist(final String blacklist) {
        this.blacklist = Util.stringToList(blacklist);
    }

    /**
     * Sets the ordering.
     * 
     * @param order The order to set.
     */
    public void setOrder(final String order) {
        this.order = Util.stringToList(order);
    }

    /**
     * Removes the blacklisted attributes from the list.
     * 
     * @param attributes The attributes.
     */
    public void removeBlacklistedAttributes(final List<Attribute> attributes) {
        final Iterator<Attribute> iterator = attributes.iterator();
        while (iterator.hasNext()) {
            if (blacklist.contains(iterator.next().getId())) {
                iterator.remove();
            }
        }
    }

    /**
     * Sorts the attributes.
     * 
     * @param attributes The attributes.
     */
    public void sortAttributes(final List<Attribute> attributes) {
        Collections.sort(attributes, new Comparator<Attribute>() {
            public int compare(final Attribute attribute1, final Attribute attribute2) {
                int last = order.size();
                int rank1 = order.indexOf(attribute1.getId());
                int rank2 = order.indexOf(attribute2.getId());

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
