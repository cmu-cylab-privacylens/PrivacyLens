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
import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.SWITCH.aai.uApprove.ar.storage.Storage;

/**
 * The Attribute Release Module.
 */
public class AttributeReleaseModule {

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(AttributeReleaseModule.class);

    /** Module enabled. */
    private boolean enabled;

    /** General Consent allowed. */
    private boolean allowGeneralConsent;

    /** List of enabled relying parties. */
    private List<String> enabledRelyingParties;

    /** Compare attribute values. */
    private boolean compareAttributeValues;

    /** Storage. */
    private Storage storage;

    /** Default constructor. */
    public AttributeReleaseModule() {
        enabled = false;
        allowGeneralConsent = false;
        enabledRelyingParties = Collections.emptyList();
        compareAttributeValues = true;
    }

    /**
     * Sets whether the module is enabled.
     * 
     * @param enabled The enabled to set.
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets if the module is enabled.
     * 
     * @return Returns the enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether general consent is allowed.
     * 
     * @param allowGeneralConsent The allowGeneralConsent to set.
     */
    public void setAllowGeneralConsent(final boolean allowGeneralConsent) {
        this.allowGeneralConsent = allowGeneralConsent;
    }

    /**
     * Sets whether attribute values are compared.
     * 
     * @param compareAttributeValues The compareAttributeValues to set.
     */
    public void setCompareAttributeValues(final boolean compareAttributeValues) {
        this.compareAttributeValues = compareAttributeValues;
    }

    /**
     * Gets if general consent is allowed.
     * 
     * @return Returns the allowGeneralConsent.
     */
    public boolean isAllowGeneralConsent() {
        return allowGeneralConsent;
    }

    /**
     * Sets the relying parties.
     * 
     * @param relyingParties The relyingParties to set.
     */
    public void setRelyingParties(final List<String> relyingParties) {
        this.enabledRelyingParties = relyingParties;
    }

    /**
     * Sets the storage.
     * 
     * @param storage The storage to set.
     */
    public void setStorage(final Storage storage) {
        this.storage = storage;
    }

    /**
     * Initializes the module.
     */
    public void initialize() {
        if (enabled) {
            Validate.notNull(storage, "Storage is not set.");
            logger.debug(
                    "Attribute Release Module initialzed with {} general consent. Attribute values are {}compared.",
                    isAllowGeneralConsent() ? "enabled" : "disabled", compareAttributeValues ? "" : "not ");
        } else {
            logger.debug("Attribute Release Module is not enabled.");
        }
    }

    /**
     * Determine if consent is required.
     * 
     * @param principalName The principal name.
     * @param relyingPartyId The relying party id.
     * @param attributes The attributes.
     * @return Returns true if consent is required.
     */
    public boolean requiresConsent(final String principalName, final String relyingPartyId,
            final List<Attribute> attributes) {

        if (!enabledRelyingParties.contains(relyingPartyId)) {
            logger.debug("Skip relying party {}.", relyingPartyId);
            return false;
        }

        if (storage.containsAttributeReleases(principalName, "*")) {
            logger.debug("User {} has given gerneral consent.");
            return false;
        }

        final List<AttributeRelease> attributeReleases = storage.readAttributeReleases(principalName, relyingPartyId);
        if (AttributeReleaseHelper.approvedAttributes(attributes, attributeReleases, compareAttributeValues)) {
            logger.debug("User {} has already approved attributes {} for relying party {}.", new Object[] {
                    principalName, relyingPartyId, attributes,});
            return false;
        }

        logger.debug("Consent is required from user {} for attributes {} releasing to relying party {}.", new Object[] {
                principalName, relyingPartyId, attributes,});
        return true;
    }

    /**
     * Clear consent.
     * 
     * @param principalName The principal Name.
     * @param relyingPartyId The relying party id.
     */
    public void clearConsent(final String principalName, final String relyingPartyId) {
        logger.info("Clear user consent for {}.", principalName);
        storage.deleteAttributeReleases(principalName, "*");
        storage.deleteAttributeReleases(principalName, relyingPartyId);
    }

    /**
     * Creates attribute release consent.
     * 
     * @param principalName The principal name.
     * @param relyingPartyId The relying party id.
     * @param attributes The attributes.
     */
    public void
            createConsent(final String principalName, final String relyingPartyId, final List<Attribute> attributes) {
        logger.info("Create user consent for {} attributes from {} to {}.", new Object[] {attributes.size(),
                principalName, relyingPartyId,});
        final DateTime consenDate = new DateTime();
        for (final Attribute attribute : attributes) {
            final AttributeRelease attributeRelease = new AttributeRelease(attribute, consenDate);
            storage.createAttributeRelease(principalName, relyingPartyId, attributeRelease);
        }
    }

    /**
     * Creates general attribute release consent.
     * 
     * @param principalName The principal name.
     */
    public void createConsent(final String principalName) {
        logger.info("Create general consent for {}.", principalName);
        final AttributeRelease attributeRelease = new AttributeRelease("*", StringUtils.EMPTY, new DateTime());
        storage.createAttributeRelease(principalName, "*", attributeRelease);
    }
}
