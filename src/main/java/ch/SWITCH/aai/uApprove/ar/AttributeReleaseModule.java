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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import ch.SWITCH.aai.uApprove.IdPHelper;
import ch.SWITCH.aai.uApprove.Util;
import ch.SWITCH.aai.uApprove.ar.storage.Storage;

// XXX revisit attribute choices

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
        IdPHelper.attributeReleaseModule = this;
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

        if (storage.containsAttributeReleaseChoice(principalName, WILDCARD, WILDCARD)) {
            logger.debug("User {} has given general consent.", principalName);
            return false;
        }

        final List<AttributeReleaseChoice> attributeReleaseChoices =
                storage.readAttributeReleaseChoices(principalName, relyingPartyId);
        if (AttributeReleaseHelper.approvedAttributes(attributes, attributeReleaseChoices, compareAttributeValues)) {
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
    public void clearChoice(final String principalName, final String relyingPartyId) {
        // XXXstroucki do something about this to either be general or specific
        logger.info("Clear user consent for {}.", principalName);
        storage.deleteAttributeReleaseChoices(principalName, WILDCARD);
        storage.deleteAttributeReleaseChoices(principalName, relyingPartyId);

        if (auditLogEnabled) {
            Util.auditLog("ar.clearConsent", principalName, relyingPartyId, Arrays.asList(new String[] {}));
        }
    }

    /**
     * Gets list of consented attributes XXX test this
     */
    public Map<String, Boolean> getAttributeConsent(final String principalName, final String relyingPartyId,
            final List<Attribute> attributes) {
        final Map<String, Boolean> out = new HashMap<String, Boolean>();
        for (final Attribute attribute : attributes) {
            final boolean choice =
                    storage.containsAttributeReleaseChoice(principalName, relyingPartyId, attribute.getId());
            out.put(attribute.getId(), choice);
        }
        return out;

    }

    /**
     * Removes attribute release consent.
     * 
     * @param principalName The principal name.
     * @param relyingPartyId The relying party id.
     * @param attributes The attributes.
     */
    public void denyAttributeRelease(final String principalName, final String relyingPartyId,
            final List<Attribute> attributes) {
        logger.info("Remove user consent for {} attributes from {} to {}.", new Object[] {attributes.size(),
                principalName, relyingPartyId,});
        // remove all attrib consents for now, but should be done by attribute.
        clearChoice(principalName, relyingPartyId);
        /*
        final DateTime choiceDate = new DateTime();
        for (final Attribute attribute : attributes) {
            final AttributeReleaseConsent attributeReleaseConsent = new AttributeReleaseConsent(attribute, choiceDate);
            if (storage.containsAttributeReleaseConsent(principalName, relyingPartyId, attribute.getId())) {
                logger.debug("Delete existing Attribute Release consent for user {}, relying party {} and attribute {}.",
                        new Object[] {principalName, relyingPartyId, attribute.getId()});
                storage.deleteAttributeReleaseConsent(principalName, relyingPartyId, attributeReleaseConsent);
            } else {
                logger.debug("Deny Attribute Release consent for user {}, relying party {} and attribute {}.",
                        new Object[] {principalName, relyingPartyId, attribute.getId()});
            }
        }

        if (auditLogEnabled) {
            final List<String> attributeIds = new ArrayList<String>();
            for (final Attribute attribute : attributes) {
                attributeIds.add(attribute.getId());
            }
            Util.auditLog("ar.consent", principalName, relyingPartyId, attributeIds);
        }
        */
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
        final DateTime choiceDate = new DateTime();
        for (final Attribute attribute : attributes) {
            final AttributeReleaseChoice attributeReleaseConsent =
                    new AttributeReleaseChoice(attribute, choiceDate, true);
            if (storage.containsAttributeReleaseChoice(principalName, relyingPartyId, attribute.getId())) {
                logger.debug("Update Attribute Release consent for user {}, relying party {} and attribute {}.",
                        new Object[] {principalName, relyingPartyId, attribute.getId()});
                storage.updateAttributeReleaseChoice(principalName, relyingPartyId, attributeReleaseConsent);
            } else {
                logger.debug("Create Attribute Release consent for user {}, relying party {} and attribute {}.",
                        new Object[] {principalName, relyingPartyId, attribute.getId()});
                storage.createAttributeReleaseChoice(principalName, relyingPartyId, attributeReleaseConsent);
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
        final AttributeReleaseChoice attributeRelease =
                new AttributeReleaseChoice(WILDCARD, WILDCARD, new DateTime(), true);
        storage.createAttributeReleaseChoice(principalName, WILDCARD, attributeRelease);

        if (auditLogEnabled) {
            Util.auditLog("ar.generalConsent", principalName, StringUtils.EMPTY, Arrays.asList(new String[] {}));
        }
    }

    /**
     * Makes entry in login table
     * 
     * @param principalName
     * @param relyingPartyId
     * @param timestamp
     * @param attributes
     */
    public void addLogin(final String principalName, final String serviceName, final String serviceUrl,
            final DateTime timestamp, final List<Attribute> attributes) {
        final LoginEvent loginEvent = new LoginEvent(principalName, serviceName, serviceUrl, timestamp);
        final LoginEventDetail loginEventDetail = new LoginEventDetail(loginEvent.getEventDetailHash(), attributes);
        storage.createLoginEvent(loginEvent, loginEventDetail);
        if (auditLogEnabled) {
            Util.auditLog("ar.loginTableEntry", principalName, serviceName,
                    Arrays.asList(new String[] {serviceUrl, timestamp.toString()}));
        }
    }

    public LoginEvent readLoginEvent(final String loginEventId) {
        return storage.readLoginEvent(loginEventId);
    }

    public LoginEventDetail readLoginEventDetail(final LoginEvent loginEvent) {
        return storage.readLoginEventDetail(loginEvent);
    }

    public List<LoginEvent> listLoginEvents(final String userId, final String relyingPartyId, final int limit) {
        return storage.listLoginEvents(userId, relyingPartyId, limit);
    }

    public List<String> listRelyingParties(final String userId, final int limit) {
        return storage.listRelyingParties(userId, limit);
    }

    public boolean isForceShowInterface(final String userId, final String relyingPartyId) {
        return storage.readForceShowInterface(userId, relyingPartyId);
    }

    public void setForceShowInterface(final String userId, final String relyingPartyId, final boolean forceShow) {
        logger.debug("setForceShowInterface userid: {} rpid: {}", userId, relyingPartyId);
        logger.debug("setForceShowInterface yesno: {}", (forceShow ? "yes" : "no"));
        try {
            storage.createForceShowInterface(userId, relyingPartyId, forceShow);
        } catch (final DataAccessException x) {
            storage.updateForceShowInterface(userId, relyingPartyId, forceShow);
        }
    }

    public ReminderInterval getReminderInterval(final String userId, final String relyingPartyId) {
        ReminderInterval reminderInterval = null;
        try {
            reminderInterval = storage.readReminderInterval(userId, relyingPartyId);
        } catch (final DataAccessException x) {
        }

        if (reminderInterval == null) {
            reminderInterval = new ReminderInterval(userId, relyingPartyId, 1, 0);
            storage.createReminderInterval(reminderInterval);
        }
        return reminderInterval;
    }

    public void updateReminderInterval(final ReminderInterval reminderInterval) {
        logger.debug("setForceShowInterface userid: {} rpid: {}", reminderInterval.getUserId(),
                reminderInterval.getRelyingPartyId());
        logger.debug("setForceShowInterface remindAfter: {} current: {}", reminderInterval.getRemindAfter(),
                reminderInterval.getCurrentCount());
        try {
            storage.updateReminderInterval(reminderInterval);
        } catch (final DataAccessException x) {
            storage.createReminderInterval(reminderInterval);
        }
    }
}
