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

    private final boolean enabled;

    private final boolean allowGeneralConsent;

    private List<String> enabledRelyingParties;

    private List<String> attributeBlacklist;

    private List<String> attributeOrdering;

    private Storage storage;

    public void setRelyingParties(final List<String> relyingParties) {
        this.enabledRelyingParties = relyingParties;
    }

    /**
     * @param attributeBlacklist The attributeBlacklist to set.
     */
    public void setAttributeBlacklist(final List<String> attributeBlacklist) {
        this.attributeBlacklist = attributeBlacklist;
    }

    /**
     * @param attributeOrdering The attributeOrdering to set.
     */
    public void setAttributeOrdering(final List<String> attributeOrdering) {
        this.attributeOrdering = attributeOrdering;
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
        attributeBlacklist = Collections.emptyList();
        attributeOrdering = Collections.emptyList();
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
            final List<Attribute> releasedAttributes) {

        final List<Attribute> attributes =
                AttributeReleaseHelper.removeBlacklistedAttributes(releasedAttributes, attributeBlacklist);

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
