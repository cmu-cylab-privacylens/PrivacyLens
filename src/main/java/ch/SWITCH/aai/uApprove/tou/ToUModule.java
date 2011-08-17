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

import ch.SWITCH.aai.uApprove.tou.storage.Storage;

/**
 *
 */
public class ToUModule {

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(ToUModule.class);

    private final boolean enabled;

    private final ToU tou;

    private final Storage storage;

    /**
     * Constructor.
     * 
     * @param enabled
     * @param tou
     * @param storage
     */
    public ToUModule(final boolean enabled, final ToU tou, final Storage storage) {
        super();
        this.enabled = enabled;
        this.tou = tou;
        this.storage = storage;
    }

    /**
     * @return Returns the tou.
     */
    public ToU getTou() {
        return tou;
    }

    /**
     * @param principalName
     * @param relyingPartyId
     * @return
     */
    public boolean isToUAccepted(final String principalName) {
        if (!enabled) {
            logger.debug("Terms of use are disabled.");
            return true;
        }
        logger.debug("Using ToU {}.", tou);

        final ToUAcceptance touAcceptance;
        if (storage.containsToUAcceptance(principalName, tou.getVersion())) {
            touAcceptance = storage.readToUAcceptance(principalName, tou.getVersion());
        } else {
            touAcceptance = ToUAcceptance.emptyToUAcceptance();
        }

        if (touAcceptance.contains(tou)) {
            logger.info("User {} has already accepted ToU {}", principalName, tou);
            return true;
        }

        logger.info("User {} needs to accept ToU", principalName, tou);
        return false;
    }

    public void acceptToU(final String principalName) {
        final DateTime acceptanceDate = new DateTime();
        logger.info("User {} has accepted ToU version {}", principalName, tou.getVersion());
        if (storage.containsToUAcceptance(principalName, tou.getVersion())) {
            logger.debug("Update ToU acceptance for ToU version {}.", tou.getVersion());
            storage.updateToUAcceptance(principalName, ToUAcceptance.createToUAcceptance(tou, acceptanceDate));
        } else {
            logger.debug("Create ToU acceptance for ToU version {}.", tou.getVersion());
            storage.createToUAcceptance(principalName, ToUAcceptance.createToUAcceptance(tou, acceptanceDate));
        }
    }

}
