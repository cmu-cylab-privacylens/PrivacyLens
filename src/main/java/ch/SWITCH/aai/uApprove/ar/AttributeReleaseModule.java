/*
 * Copyright (c) 2013 SWITCH
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.SWITCH.aai.uApprove.Util;
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

    /** Audit log enabled. */
    private boolean auditLogEnabled;

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
        auditLogEnabled = false;
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
     * Sets if the ToU audit log is enabled.
     * 
     * @param auditLogEnabled The auditLogEnabled to set.
     */
    public void setAuditLogEnabled(final boolean auditLogEnabled) {
        this.auditLogEnabled = auditLogEnabled;
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
            logger.trace("Attribute Release initialized with storage type {}.", storage.getClass().getSimpleName());
            logger.debug(
                    "Attribute Release Module initialzed with {} general consent. Attribute values are{}compared.",
                    isAllowGeneralConsent() ? "enabled" : "disabled", compareAttributeValues ? " " : " not ");
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
            logger.debug("User {} has given gerneral consent.", principalName);
            return false;
        }

        final List<AttributeReleaseConsent> attributeReleaseConsents =
                storage.readAttributeReleaseConsents(principalName, relyingPartyId);
        if (AttributeReleaseHelper.approvedAttributes(attributes, attributeReleaseConsents, compareAttributeValues)) {
            logger.debug("User {} has already approved attributes {} for relying party {}.", new Object[] {
                    principalName, attributes, relyingPartyId});
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

        if (auditLogEnabled) {
            Util.auditLog("ar.clearConsent", principalName, relyingPartyId, Arrays.asList(new String[] {}));
        }
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

        if (auditLogEnabled) {
            final List<String> attributeIds = new ArrayList<String>();
            for (final Attribute attribute : attributes) {
                attributeIds.add(attribute.getId());
            }
            Util.auditLog("ar.consent", principalName, relyingPartyId, attributeIds);
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

        if (auditLogEnabled) {
            Util.auditLog("ar.generalConsent", principalName, StringUtils.EMPTY, Arrays.asList(new String[] {}));
        }
    }
}
