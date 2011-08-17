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

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import ch.SWITCH.aai.uApprove.Util;
import ch.SWITCH.aai.uApprove.UApproveException;

/** Represents the terms of use. */
@ThreadSafe
public class ToU {

    // TODO: Needs probably an ID too (persisted, compared). For disjunction between different ToUs of relying parties.

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(ToU.class);

    /** The version of the terms of use. */
    private final String version;

    /** The text of the terms of use. */
    private final String text;

    /**
     * Constructs terms of use.
     * 
     * @param touVersion The version of the terms of use.
     * @param resource The resource from where the terms of use text is loaded.
     * @throws UApproveException In case of an initialization error.
     */
    public ToU(final String touVersion, final Resource resource) throws UApproveException {
        Assert.hasText(touVersion, touVersion + " is an invalid ToU version");
        version = touVersion;

        Assert.notNull(resource, "ToU resource is not set.");
        try {
            text = Util.readResource(resource);
            logger.info("ToU version {} initialized from file {}", version, resource);
        } catch (final IOException exception) {
            throw new UApproveException("Error while reading ToU resource " + resource.getDescription(), exception);
        }
    }

    /**
     * Gets the text of the terms of use.
     * 
     * @return Returns the text.
     */
    public final String getText() {
        return text;
    }

    /**
     * Gets the version of the terms of use.
     * 
     * @return Returns the version.
     */
    public final String getVersion() {
        return version;
    }
}
