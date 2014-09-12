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
 * This class holds a lot of unchanging data, perhaps some of it should be cached.
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

    private class OracleData {
        private ServiceProviderData[] SPs;

        private OracleData() {
        }

        public OracleData(final ServiceProviderData[] SPs) {
            this.SPs = SPs;
        }
    }

    private class ServiceProviderData {
        private AttributeData[] attrs;

        private AttributeGroupData[] attrGroups;

        private String name;

        private String id;

        private transient boolean hasAttrGroups;

        private ServiceProviderData() {
        }

        public ServiceProviderData(final String id, final String name, final AttributeData[] attrs) {
            this.id = id;
            this.name = name;
            this.attrs = attrs;
            this.hasAttrGroups = false;
        }

        public ServiceProviderData(final String id, final String name, final AttributeData[] attrs,
                final AttributeGroupData[] attrGroups) {
            this.id = id;
            this.name = name;
            this.attrs = attrs;
            this.attrGroups = attrGroups;
            this.hasAttrGroups = true;
        }

    }

    private class AttributeData {
        private String reason;

        private String privpolicy;

        private String group;

        private String id;

        private transient boolean hasGroup;

        private AttributeData() {
        }

        public AttributeData(final String id, final String reason, final String privpolicy) {
            this.id = id;
            this.reason = reason;
            this.privpolicy = privpolicy;
            hasGroup = false;
        }

        public AttributeData(final String id, final String reason, final String privpolicy, final String group) {
            this.id = id;
            this.reason = reason;
            this.privpolicy = privpolicy;
            this.group = group;
            hasGroup = true;
        }
    }

    private class AttributeGroupData {
        private String reason;

        private String description;

        private String privpolicy;

        private String id;

        private AttributeGroupData() {
        }

        public AttributeGroupData(final String id, final String description, final String reason,
                final String privpolicy) {
            this.id = id;
            this.description = description;
            this.reason = reason;
            this.privpolicy = privpolicy;
        }
    }

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
            in.mark(Integer.MAX_VALUE);
            final JsonReader readerTest = new JsonReader(new InputStreamReader(in, "UTF-8"));
            final OracleData x = gson.fromJson(readerTest, OracleData.class);
            //readerTest.close();
            logger.debug("OracleData JSON: {}", gson.toJson(x));
            in.reset();
            final JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

            final Map<String, Object> elements = new HashMap<String, Object>();
            reader.beginObject();
            while (reader.hasNext()) {
                final String name = reader.nextName();

                if (name.equals("SPs")) {
                    reader.beginArray();
                    Map<String, Object> spmap;
                    while (reader.hasNext()) {
                        spmap = new HashMap<String, Object>();
                        reader.beginObject();
                        while (reader.hasNext()) {
                            final String name1 = reader.nextName();
                            logger.debug("Reading {}", name1);
                            if (name1.equals("name") || name1.equals("id")) {
                                final String value = reader.nextString();
                                spmap.put(name1, value);
                                logger.debug("SP map {} -> {}.", name1, value);
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
        logger.warn("No service provider known by id {}.", url);
        return "UNKNOWN";
    }

    /**
     * @param spId Service Provider ID
     * @param in candidate Group ID
     * @return whether the candidate ID identifies a group.
     */
    public boolean isAttrGroup(final String spId, final String in) {
        final Map<String, Map> map = getAttributeGroupRequested(spId);
        if (map.get(in) != null) {
            return true;
        }
        return false;
    }

    /**
     * @param spId Service Provider ID
     * @param groupId Attribute Group ID
     * @return list of attribute ids
     */
    public List<String> getAttributeGroupMembers(final String spId, final String groupId) {
        // isAttrGroup?
        final List<String> out = new ArrayList();
        if (!isAttrGroup(spId, groupId)) {
            return out;
        }

        Map<String, Object> spData = null;
        for (final Map<String, Object> sp : spList) {
            logger.trace("sp id: {}", sp.get("id"));
            if (sp.get("id").equals(spId)) {
                spData = sp;
                break;
            }
        }

        if (spData == null) {
            return out;
        }

        final List<Map> attrList = (List<Map>) spData.get("attrs");
        for (final Map<String, String> attrMap : attrList) {
            final String thisGroupId = attrMap.get("group");
            logger.trace("checking id: {}", attrMap.get("id"));
            if (thisGroupId == null || !thisGroupId.equals(groupId)) {
                continue;
            }
            logger.trace("attr id: {} is member of: {}", attrMap.get("id"), groupId);
            out.add(attrMap.get("id"));
        }

        return out;
    }

    /**
     * @param spId Service Provider ID
     * @return map of group id -> (map of k -> v))
     */
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

    /**
     * @param spId Service Provider ID
     * @return map of attribute id -> (map of k -> v))
     */
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

    /**
     * @param spId Service Provider ID
     * @return map of attribute id -> whether attribute is required
     */
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
                    final boolean isRequired = required.equals("true");
                    out.put(attrmap.get("id"), isRequired);
                }
            }
        }
        return out;
    }

    /**
     * @param spId Service Provider ID
     * @return map of attribute id -> privacy policy
     */
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

    /**
     * @param spId Service Provider ID
     * @return map of attribute id -> request reason
     */
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
     * @return IdP user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param relyingPartyId Service Provider ID
     */
    public void setRelyingPartyId(final String relyingPartyId) {
        logger.debug("setRelyingPartyId: {}", relyingPartyId);
        this.relyingPartyId = relyingPartyId;
    }

    /**
     * @param userName IdP user name
     */
    public void setUserName(final String userName) {
        logger.debug("setUserName: {}", userName);
        this.userName = userName;
    }

}
