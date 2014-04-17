/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2013 Carnegie Mellon University
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

package edu.cmu.ece.PrivacyLens;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

/**
 *
 */
public class Oracle {

    private static Oracle theInstance;

    private String relyingPartyId = "unset";

    private final Logger logger = LoggerFactory.getLogger(Oracle.class);

    private String userName = "unset";

    private boolean configured = false;

    private List<Map> spList;

    private List<Map> attrList;

    private List<Map> groupList;

    /**
     * Constructor, make singleton
     */

    private Oracle() {
        // singleton
    }

    /**
     * return handle to Oracle
     */
    public static Oracle getInstance() {
        if (theInstance == null) {
            theInstance = new Oracle();
        }
        return theInstance;
    }

    public void setConfig(final String configPath) {
        // really a lot of this stuff should be readable from metadata.
        Resource configResource;
        try {
            configResource = new UrlResource(configPath);
        } catch (final MalformedURLException x) {
            logger.error("Malformed URL exception: {}", x);
            return;
        }
        final Gson gson = new Gson();
        final List<Map> splist = new ArrayList<Map>();
        final List<Map> attrlist = new ArrayList<Map>();
        final List<Map> grouplist = new ArrayList<Map>();

        try {
            final InputStream in = configResource.getInputStream();
            final JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            final Map<String, Object> elements = new HashMap<String, Object>();
            reader.beginObject();
            while (reader.hasNext()) {
                final String name = reader.nextName();

                if (name.equals("SPs")) {
                    reader.beginArray();
                    final Map<String, Object> spmap = new HashMap<String, Object>();
                    while (reader.hasNext()) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            final String name1 = reader.nextName();
                            logger.debug("Reading {}", name1);
                            if (name1.equals("name") || name1.equals("id")) {
                                spmap.put(name1, reader.nextString());
                            }
                            if (name1.equals("attrGroups")) {
                                final List<Map> grouplist1 = new ArrayList<Map>();
                                reader.beginArray();
                                while (reader.hasNext()) {
                                    final Map<String, String> attrmap = new HashMap<String, String>();
                                    reader.beginObject();
                                    while (reader.hasNext()) {
                                        attrmap.put(reader.nextName(), reader.nextString());
                                    }
                                    reader.endObject();
                                    grouplist1.add(attrmap);
                                }
                                reader.endArray();
                                spmap.put("attrGroups", grouplist1);
                            }
                            if (name1.equals("attrs")) {
                                final List<Map> attrlist1 = new ArrayList<Map>();
                                reader.beginArray();
                                while (reader.hasNext()) {
                                    final Map<String, String> attrmap = new HashMap<String, String>();
                                    reader.beginObject();
                                    while (reader.hasNext()) {
                                        attrmap.put(reader.nextName(), reader.nextString());
                                    }
                                    reader.endObject();
                                    attrlist1.add(attrmap);
                                }
                                reader.endArray();
                                spmap.put("attrs", attrlist1);
                            }
                        }
                        reader.endObject();
                        splist.add(spmap);
                    }
                    reader.endArray();
                }

                // these should be handled in config?
                if (name.equals("attrs")) {
                    reader.beginArray();
                    while (reader.hasNext()) {

                        final Map<String, String> attrmap = new HashMap<String, String>();
                        reader.beginObject();
                        while (reader.hasNext()) {
                            attrmap.put(reader.nextName(), reader.nextString());
                        }
                        reader.endObject();
                        attrlist.add(attrmap);
                    }
                    reader.endArray();
                }

            }
            reader.endObject();
            reader.close();
            in.close();

            // should now have attrlist, grouplist and splist after this

            this.spList = splist;
            this.attrList = attrlist;
            this.groupList = grouplist;

            // should be in config?
            for (final Map<String, String> attr : attrlist) {
                AttributeUtils.setAttribute(attr.get("id"), attr.get("desc"));
            }

            this.configured = true;
        } catch (final Exception x) {
            logger.error("Exception parsing config: {}", x);
        }
    }

    public void initialize() {
        Validate.isTrue(configured, "Oracle is not configured.");
        logger.trace("Oracle initialized.");

    }

    /**
     * @return human readable service provider name
     */
    public String getServiceName() {
        // take this as argument instead?
        final String url = this.relyingPartyId;

        // map this from rp name
        for (final Map<String, Object> sp : spList) {
            if (sp.get("id").equals(url)) {
                return (String) sp.get("name");
            }

        }
        logger.warn("No service provider known by id {}", url);
        return "UNKNOWN";
    }

    public Map<String, Map> getAttributeGroupRequested(final String spId) {
        final Map<String, Map> out = new HashMap();
        for (final Map<String, Object> sp : spList) {
            if (sp.get("id").equals(spId)) {
                final List<Map> attrGroupList = (List<Map>) sp.get("attrGroups");
                for (final Map<String, String> attrmap : attrGroupList) {
                    out.put(attrmap.get("id"), attrmap);
                }
            }
        }
        return out;
    }

    public Map<String, Map> getAttributeRequested(final String spId) {
        final Map<String, Map> out = new HashMap();
        for (final Map<String, Object> sp : spList) {
            if (sp.get("id").equals(spId)) {
                final List<Map> attrList = (List<Map>) sp.get("attrs");
                for (final Map<String, String> attrmap : attrList) {
                    out.put(attrmap.get("id"), attrmap);
                }
            }
        }
        return out;
    }

    public Map<String, Boolean> getAttributeRequired(final String spId) {
        final Map<String, Boolean> out = new HashMap();
        for (final Map<String, Object> sp : spList) {
            if (sp.get("id").equals(spId)) {
                final List<Map> attrList = (List<Map>) sp.get("attrs");
                for (final Map<String, String> attrmap : attrList) {
                    String required = attrmap.get("required");
                    if (required == null) {
                        required = "false";
                    }
                    if (!(required.equals("true") || required.equals("false"))) {
                        logger.error("Interpreting {} as false", required);
                    }
                    out.put(attrmap.get("id"), required.equals("true"));
                }
            }
        }
        return out;
    }

    public Map<String, String> getAttributePrivacy(final String spId) {
        final Map<String, String> out = new HashMap();
        for (final Map<String, Object> sp : spList) {
            if (sp.get("id").equals(spId)) {
                final List<Map> attrList = (List<Map>) sp.get("attrs");
                for (final Map<String, String> attrmap : attrList) {
                    out.put(attrmap.get("id"), attrmap.get("privpolicy"));
                }
            }
        }
        return out;
    }

    public Map<String, String> getAttributeReason(final String spId) {
        final Map<String, String> out = new HashMap();
        for (final Map<String, Object> sp : spList) {
            if (sp.get("id").equals(spId)) {
                final List<Map> attrList = (List<Map>) sp.get("attrs");
                for (final Map<String, String> attrmap : attrList) {
                    out.put(attrmap.get("id"), attrmap.get("reason"));
                }
            }
        }
        return out;
    }

    /**
     * @return
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param relyingPartyId
     */
    public void setRelyingPartyId(final String relyingPartyId) {
        logger.debug("setRelyingPartyId: {}", relyingPartyId);
        this.relyingPartyId = relyingPartyId;
    }

    /**
     * @param userName
     */
    public void setUserName(final String userName) {
        logger.debug("setUserName: {}", userName);
        this.userName = userName;
    }

}
