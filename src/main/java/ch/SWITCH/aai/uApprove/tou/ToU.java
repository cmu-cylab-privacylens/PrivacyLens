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

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import ch.SWITCH.aai.uApprove.UApproveException;
import ch.SWITCH.aai.uApprove.Util;

/** Represents the terms of use. */
public class ToU {

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
            content = Util.readResource(resource);
            logger.trace("Initializing ToU from {}.", resource.getDescription());
        } catch (final IOException e) {
            throw new UApproveException("Error while reading ToU resource " + resource.getDescription(), e);
        }
    }

    /**
     * Initializes the Terms of Use.
     */
    public void initialize() {
        Validate.notEmpty(version, "ToU version is not set.");
        Validate.notEmpty(content, "ToU text is not set.");
        logger.debug("ToU version {} [{}] initialized.", getVersion(), Util.hash(getContent()));
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

}
