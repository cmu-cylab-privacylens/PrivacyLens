/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2013-2015 Carnegie Mellon University
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

package edu.cmu.ece.PrivacyLens.ar;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import edu.cmu.ece.PrivacyLens.Action;
import edu.cmu.ece.PrivacyLens.Util;

/**
 * Causes the controller to go to entry state
 */
public class AdminServiceLoginAction implements Action {
    private final ServletContext servletContext;

    private final AttributeReleaseModule attributeReleaseModule;

    /** Class logger. */
    private final Logger logger = LoggerFactory
        .getLogger(AdminServiceLoginAction.class);

    /** {@inheritDoc} */
    public AdminServiceLoginAction() {
        logger.trace("AdminEntryAction init");
        servletContext = Util.servletContext;

        final WebApplicationContext appContext =
            WebApplicationContextUtils
                .getRequiredWebApplicationContext(servletContext);
        attributeReleaseModule =
            (AttributeReleaseModule) appContext.getBean(
                "PrivacyLens.attributeReleaseModule",
                AttributeReleaseModule.class);
        logger.trace("AdminEntryAction init end arm: {}",
            attributeReleaseModule);

    }

    private void prepareEntry(final HttpServletRequest request) {
        final AdminEntryPrepare.Databag databag =
            new AdminEntryPrepare.Databag(attributeReleaseModule, request);
        AdminEntryPrepare.prepare(databag);
    }

    /** {@inheritDoc} */
    public String execute(final HttpServletRequest request,
        final HttpServletResponse response) throws Exception {
        logger.trace("AdminServiceLoginAction execute sc: {}", servletContext);

        final boolean backButton = (request.getParameter("back") != null);

        final String sectionParameter = request.getParameter("section");
        final boolean loginEventSection =
            (sectionParameter.equals("loginEvent"));

        if (backButton) {
            prepareEntry(request);
            return "entry";
        }

        //final boolean helpButton = (request.getParameter("help") != null);

        if (false) { // only one section should be defined
            // error
            prepareEntry(request);
            return "entry";
        }

        if (loginEventSection) {
            final String choice = request.getParameter("choice");
            // do stuff with choice
            final LoginEvent loginEvent =
                attributeReleaseModule.readLoginEvent(choice);

            if (loginEvent == null) {
                // error
                prepareEntry(request);
                return "entry";
            }

            final String principalName = loginEvent.getUserId();
            final AdminLoginEventPrepare.Databag databag =
                new AdminLoginEventPrepare.Databag(attributeReleaseModule,
                    request, principalName, loginEvent);
            AdminLoginEventPrepare.prepare(databag);

            return "loginEvent";

        }

        logger.warn("AdminServiceLoginAction fallthrough");

        prepareEntry(request);
        return "entry";
    }

}
