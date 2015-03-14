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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import edu.cmu.ece.PrivacyLens.Action;
import edu.cmu.ece.PrivacyLens.IdPHelper;
import edu.cmu.ece.PrivacyLens.Util;

/**
 * Causes the controller to go to entry state
 */
public class AdminLoginEventAction implements Action {
    private final ServletContext servletContext;

    private final AttributeReleaseModule attributeReleaseModule;

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(AdminLoginEventAction.class);

    /** {@inheritDoc} */
    public AdminLoginEventAction() {
        logger.trace("AdminLoginEventAction init");
        servletContext = Util.servletContext;
        final WebApplicationContext appContext =
                WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);

        attributeReleaseModule =
                (AttributeReleaseModule) appContext.getBean("PrivacyLens.attributeReleaseModule",
                        AttributeReleaseModule.class);
        logger.trace("AdminLoginEventAction init end arm: {}", attributeReleaseModule);
    }

    private void prepareEntry(final HttpServletRequest request) {
        final AdminEntryPrepare.Databag databag =
            new AdminEntryPrepare.Databag(attributeReleaseModule, request);
        AdminEntryPrepare.prepare(databag);
    }

    /** {@inheritDoc} */
    public String execute(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        //final ServletContext servletContext = Util.servletContext;
        logger.trace("AdminLoginEventAction execute sc: {} arm: {}", servletContext, attributeReleaseModule);
        final String principalName =
            IdPHelper.getPrincipalName(servletContext, request);
        final String relyingPartyId = (String) request.getSession().getAttribute("relyingParty");

        final boolean backButton = (request.getParameter("back") != null);
        final boolean saveButton = (request.getParameter("save") != null);

        if (backButton) {
            prepareEntry(request);
            return "entry";
        }

        if (saveButton) {
            final String forceShowInterface = (request.getParameter("forceShowInterface"));
            final boolean forceShow = (forceShowInterface.equals("yes"));

            attributeReleaseModule.setForceShowInterface(principalName,
                relyingPartyId, forceShow);

            final List<Attribute> attributes = (List<Attribute>) request.getSession().getAttribute("attributes");

            Map<String, Boolean> consentByAttribute =
                    (Map<String, Boolean>) request.getSession().getAttribute("consentByAttribute");
            final DateTime timestamp = (DateTime) request.getSession().getAttribute("timestamp");

            if (consentByAttribute == null) {
                logger.error("consentByAttribute is null");
                consentByAttribute = new HashMap<String, Boolean>();
            }
            final Enumeration e = request.getParameterNames();
            while (e.hasMoreElements()) {
                final String param = (String) e.nextElement();
                if (param.startsWith("input-")) {
                    logger.debug("Processing parameter {}", param);
                    final String attributeName = (param.split("^input-"))[1];
                    consentByAttribute.put(attributeName, request.getParameter(param).equals("1") ? true : false);
                }
            }
            logger.debug("Create consent for user {} to {}.", principalName,
                relyingPartyId);

            final List<Attribute> deniedAttributes = new ArrayList<Attribute>();
            final List<Attribute> consentedAttributes = new ArrayList<Attribute>();

            for (final Attribute attribute : attributes) {
                final Boolean consented = consentByAttribute.get(attribute.getId());
                if (consented != null && !consented) {
                    logger.trace("Did not consent for {}", attribute.getId());
                    deniedAttributes.add(attribute);
                } else {
                    logger.trace("Did consent for {}", attribute.getId());
                    consentedAttributes.add(attribute);
                }
            }
            // XXX check correctness
            attributeReleaseModule.denyAttributeRelease(principalName,
                relyingPartyId, deniedAttributes);
            attributeReleaseModule.consentAttributeRelease(principalName,
                relyingPartyId, consentedAttributes);

            int remindAfter = 1;
            final String reminderIntervalString = (request.getParameter("reminderInterval"));

            try {
                remindAfter = Integer.parseInt(reminderIntervalString);
            } catch (final NumberFormatException x) {
            }
            final ReminderInterval reminderInterval =
                new ReminderInterval(principalName, relyingPartyId,
                    remindAfter, 0);
            attributeReleaseModule.updateReminderInterval(reminderInterval);

            prepareEntry(request);
            return "entry";
        }

        logger.warn("AdminLoginEventAction fallthrough");

        prepareEntry(request);
        return "entry";
    }
}
