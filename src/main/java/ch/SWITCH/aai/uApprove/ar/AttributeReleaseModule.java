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

import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.SWITCH.aai.uApprove.ar.storage.Storage;

/**
 * The Attribute Release Module.
 */
public class AttributeReleaseModule {

    /**
     * Wildcard string used for relying party id, attribute id and attribute values hash, when general consent is used.
     */
    private static final String WILDCARD = "*";

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

        if (storage.containsAttributeReleaseConsent(principalName, WILDCARD, WILDCARD)) {
            logger.debug("User {} has given gerneral consent.");
            return false;
        }

        final List<AttributeReleaseConsent> attributeReleaseConsents =
                storage.readAttributeReleaseConsents(principalName, relyingPartyId);
        if (AttributeReleaseHelper.approvedAttributes(attributes, attributeReleaseConsents, compareAttributeValues)) {
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
        storage.deleteAttributeReleaseConsents(principalName, WILDCARD);
        storage.deleteAttributeReleaseConsents(principalName, relyingPartyId);
    }

    /**
     * Creates or updates attribute release consent.
     * 
     * @param principalName The principal name.
     * @param relyingPartyId The relying party id.
     * @param attributes The attributes.
     */
    public void consentAttributeRelease(final String principalName, final String relyingPartyId,
            final List<Attribute> attributes) {
        logger.info("Create user consent for {} attributes from {} to {}.", new Object[] {attributes.size(),
                principalName, relyingPartyId,});
        final DateTime consenDate = new DateTime();
        for (final Attribute attribute : attributes) {
            final AttributeReleaseConsent attributeReleaseConsent = new AttributeReleaseConsent(attribute, consenDate);
            if (storage.containsAttributeReleaseConsent(principalName, relyingPartyId, attribute.getId())) {
                logger.debug("Update Attribute Release consent for user {}, relying party {} and attribute {}.",
                        new Object[] {principalName, relyingPartyId, attribute.getId()});
                storage.updateAttributeReleaseConsent(principalName, relyingPartyId, attributeReleaseConsent);
            } else {
                logger.debug("Create Attribute Release consent for user {}, relying party {} and attribute {}.",
                        new Object[] {principalName, relyingPartyId, attribute.getId()});
                storage.createAttributeReleaseConsent(principalName, relyingPartyId, attributeReleaseConsent);
            }
        }
    }

    /**
     * Creates general attribute release consent.
     * 
     * @param principalName The principal name.
     */
    public void consentGeneralAttributeRelease(final String principalName) {
        logger.info("Create general consent for {}.", principalName);
        final AttributeReleaseConsent attributeRelease =
                new AttributeReleaseConsent(WILDCARD, WILDCARD, new DateTime());
        storage.createAttributeReleaseConsent(principalName, WILDCARD, attributeRelease);
    }
}
