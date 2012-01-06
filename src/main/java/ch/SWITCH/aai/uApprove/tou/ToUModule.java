/*
 * Copyright (c) 2011, SWITCH
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

package ch.SWITCH.aai.uApprove.tou;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.SWITCH.aai.uApprove.Util;
import ch.SWITCH.aai.uApprove.tou.storage.Storage;

/**
 * The Terms of Use Module.
 */
public class ToUModule {

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(ToUModule.class);

    /** Module enabled. */
    private boolean enabled;

    /** Audit log enabled. */
    private boolean auditLogEnabled;

    /** Terms Of Use. */
    private ToU tou;

    /** Indicates whether the content of the ToU is compared. */
    private boolean compareContent;

    /** List of enabled relying parties. */
    private List<String> enabledRelyingParties;

    /** Storage. */
    private Storage storage;

    /** Default constructor. */
    public ToUModule() {
        enabled = false;
        auditLogEnabled = false;
        compareContent = true;
    }

    /**
     * Sets if the ToU module is enabled.
     * 
     * @param enabled The enabled to set.
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets if the ToU module is enabled.
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
     * Sets the ToU.
     * 
     * @param tou The tou to set.
     */
    public void setTou(final ToU tou) {
        this.tou = tou;
    }

    /**
     * Sets if the content should be compared.
     * 
     * @param compareContent The compareContent to set.
     */
    public void setCompareContent(final boolean compareContent) {
        this.compareContent = compareContent;
    }

    /**
     * Sets the relying parties.
     * 
     * @param relyingParties The relying parties.
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
     * Gets the ToU.
     * 
     * @return Returns the tou.
     */
    public ToU getTou() {
        return tou;
    }

    /**
     * Initializes the module.
     */
    public void initialize() {
        if (enabled) {
            Validate.notNull(storage, "Storage is not set.");
            logger.trace("ToU Module initialized with storage type {}.", storage.getClass().getSimpleName());
            Validate.notNull(tou, "ToU is not set.");
            logger.debug("ToU Module initialized with ToU version {}. Content is{}compared.", getTou().getVersion(),
                    compareContent ? " " : " not ");
        } else {
            logger.debug("ToU Module is not enabled.");
        }
    }

    /**
     * Determines if ToU acceptance is required.
     * 
     * @param principalName The principal name.
     * @param relyingPartyId The relying party id.
     * @return Returns true if ToU acceptance is required.
     */
    public boolean requiresToUAcceptance(final String principalName, final String relyingPartyId) {

        if (!enabledRelyingParties.contains(relyingPartyId)) {
            logger.debug("Skip relying party {}.", relyingPartyId);
            return false;
        }

        final ToUAcceptance touAcceptance = storage.readToUAcceptance(principalName, tou.getVersion());
        if (ToUHelper.acceptedToU(tou, touAcceptance, compareContent)) {
            logger.debug("User {} has already accepted ToU {}.", principalName, tou.getVersion());
            return false;
        }

        logger.info("User {} needs to accept ToU {}.", principalName, tou.getVersion());
        return true;
    }

    /**
     * Creates or updates ToU acceptance.
     * 
     * @param principalName The principal name.
     */
    public void acceptToU(final String principalName) {
        logger.info("User {} has accepted ToU version {}", principalName, tou.getVersion());

        final ToUAcceptance touAcceptance = new ToUAcceptance(tou, new DateTime());
        if (storage.containsToUAcceptance(principalName, tou.getVersion())) {
            logger.debug("Update ToU acceptance for user {} and ToU version {}.", principalName, tou.getVersion());
            storage.updateToUAcceptance(principalName, touAcceptance);
        } else {
            logger.debug("Create ToU acceptance for user {} and ToU version {}.", principalName, tou.getVersion());
            storage.createToUAcceptance(principalName, touAcceptance);
        }

        if (auditLogEnabled) {
            Util.auditLog("tou.acceptance", principalName, StringUtils.EMPTY,
                    Arrays.asList(new String[] {touAcceptance.getVersion(), touAcceptance.getFingerprint()}));
        }
    }
}
