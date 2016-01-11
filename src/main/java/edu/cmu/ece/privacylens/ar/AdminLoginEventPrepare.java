/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2015 Carnegie Mellon University
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
package edu.cmu.ece.privacylens.ar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;

import edu.cmu.ece.privacylens.Consent;
import edu.cmu.ece.privacylens.InterfaceUtil;
import edu.cmu.ece.privacylens.Oracle;
import edu.cmu.ece.privacylens.ToggleBean;
import edu.cmu.ece.privacylens.Util;

/**
 * Prepares the view for showing logins by service
 */
public class AdminLoginEventPrepare {

    public static class Databag {
        private final AttributeReleaseModule attributeReleaseModule;

        private final String principalName;

        private final LoginEvent loginEvent;

        private final RequestContext requestContext;

        public Databag(final AttributeReleaseModule attributeReleaseModule,
                final RequestContext requestContext,
                final String principalName,
 final LoginEvent loginEvent) {
            this.attributeReleaseModule = attributeReleaseModule;
            this.principalName = principalName;
            this.loginEvent = loginEvent;
            this.requestContext = requestContext;
        }
    }

    public static void prepare(final Databag databag) {
        final AttributeReleaseModule attributeReleaseModule =
            databag.attributeReleaseModule;
        final LoginEvent loginEvent = databag.loginEvent;
        final RequestContext requestContext = databag.requestContext;
        final String principalName = databag.principalName;
        final ServletContext servletContext = Util.getServletContext();

        final Oracle oracle = Oracle.getInstance();

        final HttpServletRequest request =
                (HttpServletRequest) requestContext.getExternalContext()
                        .getNativeRequest();
        final MutableAttributeMap<Object> flowScope =
                requestContext.getFlowScope();
        final String requestContextPath = request.getContextPath();

        final LoginEventDetail loginEventDetail =
            attributeReleaseModule.readLoginEventDetail(loginEvent);
        // the whole event is given to the jsp since additional text uses
        // some of the fields
        flowScope.put("loginEvent", loginEvent);
        flowScope.put("loginEventDetail",
            loginEventDetail);

        final StringBuffer sentInfoBuffer = new StringBuffer();
        for (final Attribute attribute : loginEventDetail.getAttributes()) {
            sentInfoBuffer.append(attribute.getDescription());
            // don't present values of machine readable attributes
            if (!attribute.isMachineReadable()) {
                sentInfoBuffer.append(": \""
                    + Util.listToString(attribute.getValues()) + "\"");
            }

            sentInfoBuffer.append("<br/>");
        }
        final String sentInfo = sentInfoBuffer.toString();
        flowScope.put("sentInfo", sentInfo);

        final String relyingPartyId = loginEvent.getServiceUrl();

        //final SAMLHelper samlHelper =
        //    appContext.getBean("PrivacyLens.samlHelper",
        //    SAMLHelper.class);

        /*
        final ViewHelper viewHelper =
            (ViewHelper) appContext.getBean("PrivacyLens.viewHelper",
                ViewHelper.class);
                */

        final List<Attribute> attributes =
                (List<Attribute>) flowScope.get("attributes");
        /*
        samlHelper.resolveAttributes(principalName, relyingPartyId,
        viewHelper.selectLocale(request),
        IdPHelper.getSession(request));
        */
        //IdPHelper.getAttributes(servletContext, request);
        final Map<String, Consent> consentByAttribute =
            attributeReleaseModule.getAttributeConsent(principalName,
                relyingPartyId, attributes);

        final Map<String, Boolean> settingsMap = new HashMap<String, Boolean>();
        for (final String attribute : consentByAttribute.keySet()) {
            final Consent consent = consentByAttribute.get(attribute);
            settingsMap.put(attribute, consent.isApproved());
        }

        final List<ToggleBean> beanList =
            InterfaceUtil.generateToggleListFromAttributes(attributes,
                                settingsMap, oracle, relyingPartyId,
                requestContextPath);
        flowScope.put("attributeBeans", beanList);
        //flowScope.put("attributes", attributes);
        flowScope.put("relyingParty", relyingPartyId);
        final boolean forceShow =
            attributeReleaseModule.isForceShowInterface(principalName,
                relyingPartyId);
        final ReminderInterval reminderInterval =
            attributeReleaseModule.getReminderInterval(principalName,
                relyingPartyId);
        flowScope.put("reminderInterval",
            reminderInterval.getRemindAfter());
        flowScope.put("forceShow", forceShow);
    }
}
