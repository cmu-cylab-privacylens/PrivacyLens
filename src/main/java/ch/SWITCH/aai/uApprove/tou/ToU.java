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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import ch.SWITCH.aai.uApprove.Util;

/** Represents the terms of use. */
public class ToU {

    // TODO: Needs probably an ID too (persisted, compared). For disjunction between different ToUs of relying parties.

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(ToU.class);

    /** The version of the terms of use. */
    private String version;

    /** The content of the terms of use. */
    private String content;

    /**
     * Sets the terms of use version.
     * 
     * @param version The version of the terms of use to set.
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    /**
     * Sets the content of use text.
     * 
     * @param resource The resource from where the ToU content is loaded.
     */
    public void setResource(final Resource resource) {
        try {
            logger.debug("Initialized ToU from {}.", resource.getDescription());
            content = Util.readResource(resource);
        } catch (final IOException exception) {
            logger.error("Error while reading ToU resource {}.", resource.getDescription(), exception);
        }
    }

    /**
     * Gets the version of the terms of use.
     * 
     * @return Returns the version.
     */
    public final String getVersion() {
        return version;
    }

    /**
     * Gets the content of the terms of use.
     * 
     * @return Returns the content.
     */
    public final String getContent() {
        return content;
    }

    public void initialize() {
        Assert.hasText(version, version + " is an invalid ToU version.");
        Assert.hasText(content, "ToU Text is not set.");
        logger.debug("ToU {} initialized [{}].", version, Util.fingerprint(content));
    }

}
