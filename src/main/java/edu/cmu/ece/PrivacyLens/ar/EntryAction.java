/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2013 Carnegie Mellon University
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.ece.PrivacyLens.Action;
import edu.cmu.ece.PrivacyLens.IdPHelper;
import edu.cmu.ece.PrivacyLens.Oracle;
import edu.cmu.ece.PrivacyLens.Util;

/**
 * Act on the user's choices from the Entry view
 */
public class EntryAction implements Action {

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(EntryAction.class);

    /** The servlet context. */
    private final ServletContext servletContext;

    /** The Attribute Release module. */
    private final AttributeReleaseModule attributeReleaseModule;

    /** constructor */
    public EntryAction() {
        // is there a better way?
        servletContext = Util.servletContext;
        attributeReleaseModule = IdPHelper.attributeReleaseModule;
    }

    /** {@inheritDoc} */
    public String execute(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final boolean yesButton = (request.getParameter("yes") != null);
        // final boolean moreOptionsButton = (request.getParameter("moreOptions") != null);
        final boolean noButton = (request.getParameter("no") != null);
        final boolean historyButton = (request.getParameter("history") != null);
        final boolean explainButton = (request.getParameter("explain") != null);

        final String principalName = IdPHelper.getPrincipalName(servletContext, request);
        final String relyingPartyId = IdPHelper.getRelyingPartyId(servletContext, request);
        final List<Attribute> attributes = IdPHelper.getAttributes(servletContext, request);

        final Oracle oracle = Oracle.getInstance();
        final Map<String, Map> attrGroups = oracle.getAttributeGroupRequested(relyingPartyId);

        final boolean generalConsent = attributeReleaseModule.isAllowGeneralConsent();

        // if (allowAlwaysButton && generalConsent) {
        // // record the consent so we don't bug the user next time
        // logger.debug("Create general consent for {}", principalName);
        // attributeReleaseModule.consentGeneralAttributeRelease(principalName);
        // return "sink";
        // }

        final Map<String, Boolean> consentByAttribute = new HashMap<String, Boolean>();
        final DateTime timestamp = (DateTime) request.getSession().getAttribute("timestamp");

        final Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            final String param = (String) e.nextElement();
            if (param.startsWith("input-")) {
                logger.debug("Processing parameter {}", param);
                final String inputName = (param.split("^input-"))[1];
                final boolean setting = request.getParameter(param).equals("1") ? true : false;
                if (oracle.isAttrGroup(relyingPartyId, inputName)) {
                    final List<String> groupMembers = oracle.getAttributeGroupMembers(relyingPartyId, inputName);
                    for (final String group : groupMembers) {
                        logger.debug("Group member {} setting {}", group, setting);
                        consentByAttribute.put(group, setting);
                    }
                } else {
                    consentByAttribute.put(inputName, setting);
                }
            }
        }

        if (yesButton) {
            logger.debug("Create consent for user {} to {}.", principalName, relyingPartyId);
            final List<Attribute> filteredAttributes = new ArrayList();
            final Iterator<Attribute> i = attributes.iterator();
            while (i.hasNext()) {
                final Attribute attr = i.next();
                final Boolean consented = consentByAttribute.get(attr.getId());
                if (consented != null && !consented) {
                    logger.trace("Did not consent for {}", attr.getId());
                    filteredAttributes.add(attr);
                    i.remove();
                }
            }

            // XXX fix the consent db
            attributeReleaseModule.denyAttributeRelease(principalName, relyingPartyId, filteredAttributes);
            attributeReleaseModule.consentAttributeRelease(principalName, relyingPartyId, attributes);

            // make entry in login database
            attributeReleaseModule.addLogin(principalName, oracle.getServiceName(), relyingPartyId, timestamp,
                    attributes);
            return "sink";
        }

        if (noButton) {
            return "admin";
        }

        if (historyButton) {
            return "admin";
        }

        if (explainButton) {
            return "explanation";
        }

        // fallthrough
        logger.warn("Fell through");
        return "entry";
    }
}
