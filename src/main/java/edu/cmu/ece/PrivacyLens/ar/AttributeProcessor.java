/*
 * Copyright (c) 2011, SWITCH
 * Copyright (c) 2013, Carnegie Mellon University
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

package edu.cmu.ece.PrivacyLens.ar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.ece.PrivacyLens.Util;

/**
 * Attribute Processor.
 */
public class AttributeProcessor {

    private class EntitlementDescriptions {
        private final Logger logger = LoggerFactory.getLogger(EntitlementDescriptions.class);

        private final Map<String, String> map = new HashMap<String, String>();

        private EntitlementDescriptions(final String data) {
            // data format should be q("original","new" "original2","new2" ...)
            // using perl terminology
            final List<String> tuples = new ArrayList<String>();
            // itemPattern: extract tuples into "first" and "rest" lists -
            // it would be better if the properties could already have been
            // segmented
            final Pattern itemPattern = Pattern.compile("^(\".+?\",\".+?\")\\s*(.*)$");
            boolean stop = false;
            String nextData = data;
            while (!stop) {
                final Matcher matcher = itemPattern.matcher(nextData);
                if (!matcher.find()) {
                    throw new InternalError("Entitlement description format is incorrect");
                }
                final String first = matcher.group(1);
                final String rest = matcher.group(2);
                tuples.add(first);
                nextData = rest;
                if (nextData.length() == 0) {
                    stop = true;
                }
            }
            // tuplePattern: extract the two strings
            final Pattern tuplePattern = Pattern.compile("^\"(.+)\",\"(.+)\"$");
            for (final String tuple : tuples) {
                final Matcher matcher = tuplePattern.matcher(tuple);
                if (!matcher.find()) {
                    throw new InternalError("Entitlement description format is incorrect");
                }
                final String key = matcher.group(1);
                final String value = matcher.group(2);

                map.put(key, value);
            }

        }

        /**
         * @param key the string to find a description for
         * @return the configured description
         */
        private String get(final String key) {
            final String out = map.get(key);
            if (out == null) {
                // log?
            }
            return out;
        }

    }

    /** Class logger. */
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(AttributeProcessor.class);

    /** The attribute blacklist. */
    private List<String> blacklist;

    /** The ordering of the attributes. */
    private List<String> order;

    /**
     * List of attributes that are machine readable
     */
    private List<String> machinereadable;

    /**
     * Structure allowing mapping from technical entitlement strings to more user friendly strings
     */
    private EntitlementDescriptions entitlementDescriptions;

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
     * @param machinereadable The list of machine readable attributes to set.
     */
    public void setMachinereadable(final String machinereadable) {
        this.machinereadable = Util.stringToList(machinereadable);
    }

    /**
     * @param entitlementdescription The list of comma separated quoted string pairs to decode entitlements.
     */
    public void setEntitlementdescription(final String entitlementdescription) {
        this.entitlementDescriptions = new EntitlementDescriptions(entitlementdescription);
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

    public void markMachineReadableAttributes(final List<Attribute> attributes) {
        final Iterator<Attribute> iterator = attributes.iterator();
        while (iterator.hasNext()) {
            final Attribute attribute = iterator.next();
            if (machinereadable.contains(attribute.getId())) {
                attribute.setMachineReadable(true);
            }
        }

    }

    public void processEntitlementDescriptions(final Attribute attribute) {
        // XXXstroucki check that the attribute is entitlements
        if (entitlementDescriptions == null) {
            logger.error("entitlementDescriptions is null, can't process");
            return;
        }
        final ListIterator<String> iterator = attribute.getValues().listIterator();
        while (iterator.hasNext()) {
            final String x = iterator.next();
            final String newValue = entitlementDescriptions.get(x);
            if (newValue != null) {
                iterator.set(newValue);
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
                final int last = order.size();
                int rank1 = order.indexOf(attribute1.getId());
                int rank2 = order.indexOf(attribute2.getId());

                // put attributes without defined ordering at the end,
                // but keep them equivalent
                if (rank1 < 0) {
                    rank1 = last + 1;
                }
                if (rank2 < 0) {
                    rank2 = last + 1;
                }

                final int result = Integer.signum(rank1 - rank2);

                return result;
            }
        });
        logger.debug("sorted list: {}", attributes);
    }

    /**
     * Removes null/empty/blank values.
     * 
     * @param attribute The attribute.
     */
    public void removeEmptyValues(final Attribute attribute) {
        final Iterator<String> iterator = attribute.getValues().iterator();
        while (iterator.hasNext()) {
            final String x = iterator.next();
            if (StringUtils.isBlank(x)) {
                iterator.remove();
            }
        }
    }

    /**
     * Removes duplicate values.
     * 
     * @param attribute The attribute.
     */
    public void removeDuplicateValues(final Attribute attribute) {
        final Set<String> set = new LinkedHashSet<String>(attribute.getValues());
        attribute.getValues().clear();
        attribute.getValues().addAll(set);
    }

    /**
     * Removes attributes which does not contain any values.
     * 
     * @param attributes List of attributes.
     */
    public void removeEmptyAttributes(final List<Attribute> attributes) {
        final Iterator<Attribute> iterator = attributes.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValues().isEmpty()) {
                iterator.remove();
            }
        }
    }
}
