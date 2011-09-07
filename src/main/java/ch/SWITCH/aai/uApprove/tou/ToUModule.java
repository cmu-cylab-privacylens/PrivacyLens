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

package ch.SWITCH.aai.uApprove.tou;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.SWITCH.aai.uApprove.tou.storage.Storage;

/**
 *
 */
public class ToUModule {

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(ToUModule.class);

    private boolean enabled;

    private ToU tou;

    private boolean compareContent;

    private List<String> relyingParties;

    private Storage storage;

    /** Default constructor. */
    public ToUModule() {
        enabled = false;
        compareContent = true;
    }

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
     * @param tou The tou to set.
     */
    public void setTou(final ToU tou) {
        this.tou = tou;
    }

    /**
     * @param compareContent The compareContent to set.
     */
    public void setCompareContent(final boolean compareContent) {
        this.compareContent = compareContent;
    }

    /**
     * @param relyingParties The relyingParties to set.
     */
    public void setRelyingParties(final List<String> relyingParties) {
        this.relyingParties = relyingParties;
    }

    /**
     * @param storage The storage to set.
     */
    public void setStorage(final Storage storage) {
        this.storage = storage;
    }

    /**
     * @return Returns the tou.
     */
    public ToU getTou() {
        return tou;
    }

    public void initialize() {
        if (enabled) {
            Validate.notNull(tou, "ToU is not set.");
            Validate.notNull(storage, "Storage is not set.");
            logger.debug("ToU Module initialized with ToU version {}. Content is {}compared.", getTou().getVersion(),
                    compareContent ? "" : "not ");
        } else {
            logger.debug("ToU Module is not enabled.");
        }
    }

    /**
     * @param principalName
     * @param relyingPartyId
     * @return
     */
    public boolean requiresToUAcceptance(final String principalName, final String relyingPartyId) {

        if (!relyingParties.contains(relyingPartyId)) {
            logger.debug("Skip relying party {}.", relyingPartyId);
            return false;
        }

        final ToUAcceptance touAcceptance;
        if (storage.containsToUAcceptance(principalName, tou.getVersion())) {
            touAcceptance = storage.readToUAcceptance(principalName, tou.getVersion());
        } else {
            touAcceptance = ToUAcceptance.emptyToUAcceptance();
        }

        if (ToUHelper.acceptedToU(tou, touAcceptance, compareContent)) {
            logger.info("User {} has already accepted ToU {}.", principalName, tou.getVersion());
            return false;
        }

        logger.info("User {} needs to accept ToU {}.", principalName, tou.getVersion());
        return true;
    }

    public void acceptToU(final String principalName) {
        logger.info("User {} has accepted ToU version {}", principalName, tou.getVersion());

        final ToUAcceptance touAcceptance = new ToUAcceptance(tou, new DateTime());
        if (storage.containsToUAcceptance(principalName, tou.getVersion())) {
            logger.debug("Update ToU acceptance for ToU version {}.", tou.getVersion());
            storage.updateToUAcceptance(principalName, touAcceptance);
        } else {
            logger.debug("Create ToU acceptance for ToU version {}.", tou.getVersion());
            storage.createToUAcceptance(principalName, touAcceptance);
        }
    }
}
