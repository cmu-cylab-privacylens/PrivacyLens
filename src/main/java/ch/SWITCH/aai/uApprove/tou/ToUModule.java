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

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import ch.SWITCH.aai.uApprove.tou.storage.Storage;

/**
 *
 */
public class ToUModule {

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(ToUModule.class);

    private boolean enabled;

    private ToU tou;

    private Storage storage;

    /** Default constructor. */
    public ToUModule() {
        enabled = false;
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
        Assert.notNull(tou, "ToU is not set.");
        Assert.notNull(storage, "Storage is not set.");
    }

    /**
     * @param principalName
     * @param relyingPartyId
     * @return
     */
    public boolean isToUAccepted(final String principalName) {
        final ToUAcceptance touAcceptance;
        if (storage.containsToUAcceptance(principalName, tou.getVersion())) {
            touAcceptance = storage.readToUAcceptance(principalName, tou.getVersion());
        } else {
            touAcceptance = ToUAcceptance.emptyToUAcceptance();
        }

        if (touAcceptance.contains(tou)) {
            logger.info("User {} has already accepted ToU {}.", principalName, tou.getVersion());
            return true;
        }

        logger.info("User {} needs to accept ToU {}.", principalName, tou.getVersion());
        return false;
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
