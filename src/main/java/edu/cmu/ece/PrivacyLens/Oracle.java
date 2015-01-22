/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2013-2015 Carnegie Mellon University
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

/**
 * This class holds a lot of unchanging data, perhaps some of it should be
 * cached.
 */
public class Oracle {

    private static Oracle theInstance;

    private String relyingPartyId = "unset";

    private final Logger logger = LoggerFactory.getLogger(Oracle.class);

    // Interpret ServiceProviderData URLs as regular expressions
    private static boolean regexpMatch = true;

    private String userName = "unset";

    private boolean configured = false;

    private OracleData data;

    private class OracleData {
        private ServiceProviderData[] SPs;

        private LocalizedAttributes[] attrs;

        private OracleData() {
        }

        public OracleData(final ServiceProviderData[] SPs,
            final LocalizedAttributes[] attrs) {
            this.SPs = SPs;
            this.attrs = attrs;
        }
    }

    private class LocalizedAttributes {
        private String desc;

        private String id;

        private LocalizedAttributes() {
        }

        public LocalizedAttributes(final String id, final String desc) {
            this.id = id;
            this.desc = desc;
        }

        public Map<String, String> toMap() {
            final Map<String, String> out = new HashMap<String, String>();
            out.put("id", id);
            out.put("desc", desc);
            return out;
        }
    }

    private class ServiceProviderData {
        private AttributeData[] attrs;

        private AttributeGroupData[] attrGroups;

        private String name;

        private String id;

        private String logo;

        private transient boolean hasAttrGroups;

        private transient Pattern matchPattern;

        private ServiceProviderData() {
        }

        public ServiceProviderData(final String id, final String name,
            final AttributeData[] attrs) {
            this.id = id;
            this.name = name;
            this.attrs = attrs;
            this.hasAttrGroups = false;
        }

        public ServiceProviderData(final String id, final String name,
            final AttributeData[] attrs, final AttributeGroupData[] attrGroups) {
            this.id = id;
            this.name = name;
            this.attrs = attrs;
            this.attrGroups = attrGroups;
            this.hasAttrGroups = true;
        }

        public boolean match(final String target) {
            // XXXstroucki oracle data is currently unsorted. that makes
            // it difficult to structure regexps from specific to general.
            boolean out;

            if (regexpMatch) {
                if (matchPattern == null) {
                    matchPattern = Pattern.compile(id);
                    // handle default id
                    if (id.equals("DEFAULT")) {
                        matchPattern = Pattern.compile("");
                    }
                }
                final Matcher matcher = matchPattern.matcher(target);
                if (matcher.find()) {
                    out = true;
                } else {
                    out = false;
                }

            } else {
                out = (id.equals(target));
            }

            return out;
        }

    }

    private class AttributeData {
        private String reason;

        private String privpolicy;

        private String group;

        private String id;

        private boolean required;

        private transient boolean hasGroup;

        private AttributeData() {
        }

        public AttributeData(final String id, final String reason,
            final String privpolicy, final String required) {
            this.id = id;
            this.reason = reason;
            this.privpolicy = privpolicy;
            this.required = required.equals("true");
            hasGroup = false;
        }

        public AttributeData(final String id, final String reason,
            final String privpolicy, final String required, final String group) {
            this.id = id;
            this.reason = reason;
            this.privpolicy = privpolicy;
            this.required = required.equals("true");
            this.group = group;
            hasGroup = true;
        }

        public Map<String, String> toMap() {
            final Map<String, String> out = new HashMap<String, String>();
            out.put("reason", reason);
            out.put("group", group);
            out.put("privpolicy", privpolicy);
            out.put("required", Boolean.toString(required));
            out.put("id", id);
            return out;
        }
    }

    private class AttributeGroupData {
        private String reason;

        private String description;

        private String privpolicy;

        private String id;

        private boolean required;

        private AttributeGroupData() {
        }

        public AttributeGroupData(final String id, final String description,
            final String reason, final String privpolicy) {
            this.id = id;
            this.description = description;
            this.reason = reason;
            this.privpolicy = privpolicy;
        }

        public Map<String, String> toMap() {
            final Map<String, String> out = new HashMap<String, String>();
            out.put("reason", reason);
            out.put("description", description);
            out.put("privpolicy", privpolicy);
            out.put("required", Boolean.toString(required));
            out.put("id", id);
            return out;
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

    public void setConfig(final Resource configResource) {
        // really a lot of this stuff should be readable from metadata.

        final Gson gson = new Gson();

        try {
            final InputStream in = configResource.getInputStream();
            final JsonReader reader =
                new JsonReader(new InputStreamReader(in, "UTF-8"));
            data = gson.fromJson(reader, OracleData.class);
            reader.close();
            //logger.debug("OracleData JSON: {}", gson.toJson(x));

            // Set localized attribute descriptions
            // like eduPersonPrincipalName -> Andrew ID
            // XXXstroucki should be in config?
            for (final LocalizedAttributes attr : data.attrs) {
                AttributeUtils.setAttribute(attr.id, attr.desc);
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
     * @return Returns whether SP matching is done by regular expressions.
     */
    public static boolean isRegexpMatch() {
        return regexpMatch;
    }

    /**
     * @param regexpMatch Whether SP matching should be done with regular
     *            expressions.
     */
    public static void setRegexpMatch(final boolean regexpMatch) {
        Oracle.regexpMatch = regexpMatch;
    }

    /**
     * @return human readable service provider name
     */
    public String getServiceName() {
        // take this as argument instead?
        final String url = this.relyingPartyId;

        // map this from rp name
        for (final ServiceProviderData sp : data.SPs) {
            if (sp.match(url)) {
                return sp.name;
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
    public List<String> getAttributeGroupMembers(final String spId,
        final String groupId) {
        // isAttrGroup?
        final List<String> out = new ArrayList<String>();
        if (!isAttrGroup(spId, groupId)) {
            return out;
        }

        ServiceProviderData spData = null;
        for (final ServiceProviderData sp : data.SPs) {
            logger.trace("sp id: {}", sp.id);
            if (sp.match(spId)) {
                spData = sp;
                break;
            }
        }

        if (spData == null) {
            return out;
        }

        for (final AttributeData attrMap : spData.attrs) {
            final String thisGroupId = attrMap.group;
            logger.trace("checking id: {}", attrMap.id);
            if (thisGroupId == null || !thisGroupId.equals(groupId)) {
                continue;
            }
            logger.trace("attr id: {} is member of: {}", attrMap.id, groupId);
            out.add(attrMap.id);
        }

        return out;
    }

    /**
     * @param spId Service Provider ID
     * @return the ServiceProviderData object for a matching service provider
     *         (can be null)
     */
    private ServiceProviderData getMatchingSP(final String spId) {
        ServiceProviderData matchedSP = null;
        for (final ServiceProviderData sp : data.SPs) {
            if (sp.match(spId)) {
                matchedSP = sp;
                break;
            }
        }

        return matchedSP;
    }

    /**
     * Get the logo file for a service provider
     *
     * @param spId Service Provider ID
     * @return String (could be null) logo file name
     */
    public String getLogo(final String spId) {
        String out = null;
        final ServiceProviderData matchedSP = getMatchingSP(spId);

        if (matchedSP != null) {
            out = matchedSP.logo;
        }
        return out;
    }

    /**
     * @param spId Service Provider ID
     * @return map of group id -> (map of k -> v))
     */
    public Map<String, Map> getAttributeGroupRequested(final String spId) {
        final Map<String, Map> out = new HashMap();
        final ServiceProviderData matchedSP = getMatchingSP(spId);

        if (matchedSP != null) {
            for (final AttributeGroupData attrmap : matchedSP.attrGroups) {
                out.put(attrmap.id, attrmap.toMap());
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
        final ServiceProviderData matchedSP = getMatchingSP(spId);

        if (matchedSP != null) {
            for (final AttributeData attrmap : matchedSP.attrs) {
                out.put(attrmap.id, attrmap.toMap());
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
        final ServiceProviderData matchedSP = getMatchingSP(spId);

        if (matchedSP != null) {
            for (final AttributeData attrmap : matchedSP.attrs) {
                out.put(attrmap.id, attrmap.required);
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
        final ServiceProviderData matchedSP = getMatchingSP(spId);

        if (matchedSP != null) {
            for (final AttributeData attrmap : matchedSP.attrs) {
                out.put(attrmap.id, attrmap.privpolicy);
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
        final ServiceProviderData matchedSP = getMatchingSP(spId);

        if (matchedSP != null) {
            for (final AttributeData attrmap : matchedSP.attrs) {
                out.put(attrmap.id, attrmap.reason);
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
