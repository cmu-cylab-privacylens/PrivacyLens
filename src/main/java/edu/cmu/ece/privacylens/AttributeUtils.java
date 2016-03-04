/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2013-2016 Carnegie Mellon University
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

package edu.cmu.ece.privacylens;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * utilities to pull in attribute descriptions etc if none are found.
 */

public final class AttributeUtils {
    /** Class logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AttributeUtils.class);

    private static final Map<String, String> nameMap;

    private static final Map<String, String> descriptionMap;

    private AttributeUtils() {
    }


    static {
        final Map<String, String> n = new HashMap<String, String>();
        final Map<String, String> d = new HashMap<String, String>();

        /*
        mapAdd(n, d, "eduPersonPrincipalName", "user name", "Andrew ID");
        mapAdd(n, d, "eduPersonPrimaryAffiliation", "affiliation", "CMU affiliation");
        mapAdd(n, d, "eduPersonEntitlement", "credentials", "credentials to access CMU services");
        mapAdd(n, d, "cn", "name", "full name");
        mapAdd(n, d, "sn", "surname", "Surname");
        */

        //i.put("eduPersonPrincipalName", )

        nameMap = n;
        descriptionMap = d;
    }

    public static void setAttribute(final String id, final String desc) {
        mapAdd(nameMap, descriptionMap, id, desc, desc);
    }

    private static void mapAdd(final Map<String, String> n, final Map<String, String> d, final String id,
            final String name, final String description) {
        n.put(id, name);
        d.put(id, description);
    }

    /**
     * @param id The attribute id
     * @return A short descriptive name
     */
    public static String getName(final String id) {
        String name = nameMap.get(id);
        if (name == null) {
            name = "(" + id + ")";
        }
        return name;
    }

    /**
     * @param id The attribute id
     * @return A longer description of the attribute
     */
    public static String getDescription(final String id) {
        String description = descriptionMap.get(id);
        if (description == null) {
            description = "(" + id + ")";
        }
        return description;
    }

}
