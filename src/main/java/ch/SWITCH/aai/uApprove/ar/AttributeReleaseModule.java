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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import ch.SWITCH.aai.uApprove.ar.storage.Storage;

/**
 *
 */
public class AttributeReleaseModule {

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(AttributeReleaseModule.class);

    private boolean enabled;

    private boolean allowGeneralConsent;

    private List<String> enabledRelyingParties;

    private Storage storage;

    /**
     * @param enabled The enabled to set.
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return Returns the enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param allowGeneralConsent The allowGeneralConsent to set.
     */
    public void setAllowGeneralConsent(final boolean allowGeneralConsent) {
        this.allowGeneralConsent = allowGeneralConsent;
    }

    /**
     * @return Returns the allowGeneralConsent.
     */
    public boolean isAllowGeneralConsent() {
        return allowGeneralConsent;
    }

    public void setRelyingParties(final List<String> relyingParties) {
        this.enabledRelyingParties = relyingParties;
    }

    /**
     * @param storage The storage to set.
     */
    public void setStorage(final Storage storage) {
        this.storage = storage;
    }

    public AttributeReleaseModule() {
        enabled = false;
        allowGeneralConsent = false;
        enabledRelyingParties = Collections.emptyList();
    }

    public void initialize() {
        Assert.notNull(storage, "Storage is not set.");
    }

    /**
     * @param principalName
     * @param relyingPartyId
     * @param attributes
     * @return
     */
    public boolean consentRequired(final String principalName, final String relyingPartyId,
            final List<Attribute> attributes) {

        if (storage.containsAttributeReleases(principalName, "*")) {
            logger.debug("User {} has given gerneral consent.");
            return false;
        }

        final List<AttributeRelease> attributeReleases = storage.readAttributeReleases(principalName, relyingPartyId);
        if (AttributeReleaseHelper.approvedAttributes(attributes, attributeReleases)) {
            logger.debug("Consent not required.");
            return false;
        }

        logger.debug("Consent required.");
        return true;
    }

    /**
     * @param principalName
     * @param relyingPartyId
     */
    public void clearConsent(final String principalName, final String relyingPartyId) {
        logger.info("Clear user consent for {}.", principalName);
        storage.deleteAttributeReleases(principalName, "*");
        storage.deleteAttributeReleases(principalName, relyingPartyId);
    }

    /**
     * @param principalName
     * @param relyingPartyId
     */
    public void
            createConsent(final String principalName, final String relyingPartyId, final List<Attribute> attributes) {
        logger.info("Create user consent for {} attributes from {} to {}.", new Object[] {attributes.size(),
                principalName, relyingPartyId});
        final DateTime consenDate = new DateTime();
        for (final Attribute attribute : attributes) {
            final AttributeRelease attributeRelease = new AttributeRelease(attribute, consenDate);
            storage.createAttributeRelease(principalName, relyingPartyId, attributeRelease);
        }
    }

    public void createConsent(final String principalName) {
        logger.info("Create general consent for {}.", principalName);
        final AttributeRelease attributeRelease = new AttributeRelease("*", StringUtils.EMPTY, new DateTime());
        storage.createAttributeRelease(principalName, "*", attributeRelease);
    }
}
