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
