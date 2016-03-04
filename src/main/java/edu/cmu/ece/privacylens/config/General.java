/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2013-2016 Carnegie Mellon University
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

package edu.cmu.ece.privacylens.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class General {

    private static final General theInstance = new General();

    private final Logger logger = LoggerFactory.getLogger(General.class);

    //private boolean configured = false;

    private String idpName;

    private String adminUrl;

    private String adminMail;

    private String credits;

    public String getOrganizationName() {
        return getIdpName();
    }

    /**
     * @return Returns the idpName.
     */
    public String getIdpName() {
        return idpName;
    }

    /**
     * @param idpName The idpName to set.
     */
    public void setIdpName(final String idpName) {
        this.idpName = idpName;
    }

    /**
     * @return Returns the adminUrl.
     */
    public String getAdminUrl() {
        return adminUrl;
    }

    /**
     * @param adminUrl The adminUrl to set.
     */
    public void setAdminUrl(final String adminUrl) {
        logger.debug("setAdminUrl url: {}", adminUrl);
        this.adminUrl = adminUrl;
    }

    /**
     * @return Returns the adminMail.
     */
    public String getAdminMail() {
        return adminMail;
    }

    /**
     * @param adminMail The adminMail to set.
     */
    public void setAdminMail(final String adminMail) {
        this.adminMail = adminMail;
    }

    /**
     * @return Returns the credits.
     */
    public String getCredits() {
        return credits;
    }

    /**
     * @param credits The credits to set.
     */
    public void setCredits(final String credits) {
        this.credits = credits;
    }

    /**
     * Constructor, make singleton
     */

    private General() {
        // singleton
    }

    /**
     * return handle to General
     *
     * @return the instance
     */
    public static General getInstance() {
        return theInstance;
    }

    public void initialize() {
        //Validate.isTrue(configured, "config.General is not configured.");
        logger.trace("config.General initialized.");
    }

}
