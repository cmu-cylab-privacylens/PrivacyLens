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
import edu.cmu.ece.PrivacyLens.Oracle;
import edu.cmu.ece.PrivacyLens.StrouckiHTMLUtils;
import edu.cmu.ece.PrivacyLens.StrouckiUtils;
import edu.cmu.ece.PrivacyLens.ToggleBean;
import edu.cmu.ece.PrivacyLens.Util;
import edu.cmu.ece.PrivacyLens.config.General;

/**
 * Causes the controller to go to entry state
 */
public class AdminEntryAction implements Action {
    private final ServletContext servletContext;

    private final AttributeReleaseModule attributeReleaseModule;

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(AdminEntryAction.class);

    private final String emailAdminBoilerText = StrouckiHTMLUtils.getEmailAdminBoilerText(General.getInstance()
            .getAdminMail());

    private String relyingPartyId;

    private Oracle oracle;

    private String requestContextPath;

    /** {@inheritDoc} */
    public AdminEntryAction() {
        logger.trace("AdminEntryAction init");
        servletContext = Util.servletContext;

        final WebApplicationContext appContext =
                WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        attributeReleaseModule =
                (AttributeReleaseModule) appContext.getBean("PrivacyLens.attributeReleaseModule",
                        AttributeReleaseModule.class);
        logger.trace("AdminEntryAction init end arm: {}", attributeReleaseModule);

    }

    private List<Map> processLoginEvents(final List<LoginEvent> loginEventsList) {
        final List<Map> foobar = new ArrayList<Map>();
        for (final LoginEvent loginEvent : loginEventsList) {
            loginEvent.getEventDetailHash();
            final DateTime loginEventDate = loginEvent.getDate();
            final DateTime now = new DateTime();
            final long relativeTime = now.getMillis() - loginEventDate.getMillis();
            final String dateTimeString = StrouckiUtils.millisToDuration(relativeTime);
            final String serviceString = loginEvent.getServiceName();
            final String loginEventId = loginEvent.getEventDetailHash();
            final Map<String, Object> map = new HashMap<String, Object>();
            map.put("dateTimeString", dateTimeString);
            map.put("service", serviceString);
            map.put("loginEvent", loginEvent);
            map.put("loginEventId", loginEventId);
            foobar.add(map);
        }
        return foobar;
    }

    public List<ToggleBean> generateToggleFromAttributes(final List<Attribute> attributes,
            final Map<String, Boolean> settingsMap) {
        final Map<String, Boolean> attrMap = Oracle.getInstance().getAttributeRequired(relyingPartyId);
        final Map<String, String> attrReason = Oracle.getInstance().getAttributeReason(relyingPartyId);
        final Map<String, String> attrPrivacy = Oracle.getInstance().getAttributePrivacy(relyingPartyId);

        final List<ToggleBean> attributeBeans = new ArrayList<ToggleBean>();
        for (final Attribute attribute : attributes) {
            final String attributeId = attribute.getId();
            if (!attrMap.containsKey(attributeId)) {
                continue;
            }

            final ToggleBean bean = new ToggleBean();

            final boolean required = attrMap.get(attributeId);

            if (required) {
                bean.setImmutable(true);
                bean.setImageTrue(requestContextPath + "/PrivacyLens/force_sending.png");
            } else {
                bean.setImmutable(false);
                bean.setImageFalse(requestContextPath + "/PrivacyLens/not_sending.png");
                bean.setImageTrue(requestContextPath + "/PrivacyLens/sending.png");
            }

            final boolean value = settingsMap.get(attributeId);
            if (required) {
                bean.setValue(true);
            } else {
                bean.setValue(value);
            }

            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<p>" + attrReason.get(attributeId) + "</p><p>" + attrPrivacy.get(attributeId)
                    + "</p>");
            stringBuilder.append("<p>");

            // eduPersonEntitlement is meaningless to the end user
            if (!attributeId.equals("eduPersonEntitlement")) {
                stringBuilder.append("Your " + attribute.getDescription() + " is " + '"' + attribute.getValues().get(0)
                        + "\". ");
            }
            stringBuilder.append("If you continue to " + oracle.getServiceName() + ", your "
                    + attribute.getDescription() + " will ");
            stringBuilder.append(value ? "" : "not");
            stringBuilder.append(" be sent to it. Use the toggle switch to change this setting.");
            stringBuilder.append("</p>");
            stringBuilder.append(emailAdminBoilerText);

            final String explanation = stringBuilder.toString();
            bean.setExplanation(explanation);
            bean.setExplanationIcon(requestContextPath + "/PrivacyLens/info.png");
            bean.setTextDiv("attributeReleaseAttribute");
            bean.setImageDiv("attributeReleaseControl");
            bean.setParameter(attributeId);

            stringBuilder.setLength(0);
            stringBuilder.append(attribute.getDescription());
            // eduPersonEntitlement is meaningless to the end user
            if (!attributeId.equals("eduPersonEntitlement")) {
                stringBuilder.append(" (<b>");
                stringBuilder.append(attribute.getValues().get(0));
                stringBuilder.append("</b>)");
            }
            if (required) {
                stringBuilder.append('*');
            }

            final String text = stringBuilder.toString();
            bean.setText(text);
            if (!bean.validate()) {
                logger.error("AdminServiceLoginAction {} did not validate", attribute);
            }
            attributeBeans.add(bean);

        }
        return attributeBeans;
    }

    /** {@inheritDoc} */
    public String execute(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        //final ServletContext servletContext = Util.servletContext;
        logger.trace("AdminEntryAction execute sc: {}", servletContext);

        if (requestContextPath == null) {
            requestContextPath = request.getContextPath();
        }

        oracle = Oracle.getInstance();

        final String principalName = IdPHelper.getPrincipalName(servletContext, request);

        final int limitLoginEvents = 6;

        final String sectionParameter = request.getParameter("section");
        final boolean loginEventSection = (sectionParameter.equals("loginEvent"));
        final boolean serviceSection = (sectionParameter.equals("service"));

        final boolean helpButton = (request.getParameter("help") != null);

        if (loginEventSection && serviceSection) { // only one section should be defined
            // error
            return "entry";
        }

        if (loginEventSection) {
            final String choice = request.getParameter("choice");
            // do stuff with choice
            final LoginEvent loginEvent = attributeReleaseModule.readLoginEvent(choice);

            if (loginEvent == null) {
                // error
                return "entry";
            }

            final LoginEventDetail loginEventDetail = attributeReleaseModule.readLoginEventDetail(loginEvent);
            request.getSession().setAttribute("loginEvent", loginEvent);
            request.getSession().setAttribute("loginEventDetail", loginEventDetail);
            relyingPartyId = loginEvent.getServiceUrl();

            final List<Attribute> attributes = IdPHelper.getAttributes(servletContext, request);
            final Map<String, Boolean> consentByAttribute =
                    attributeReleaseModule.getAttributeConsent(principalName, relyingPartyId, attributes);

            final List<ToggleBean> beanList = generateToggleFromAttributes(attributes, consentByAttribute);
            request.getSession().setAttribute("attributeBeans", beanList);
            request.getSession().setAttribute("relyingParty", relyingPartyId);
            final boolean forceShow = attributeReleaseModule.isForceShowInterface(principalName, relyingPartyId);
            final ReminderInterval reminderInterval =
                    attributeReleaseModule.getReminderInterval(principalName, relyingPartyId);
            request.getSession().setAttribute("reminderInterval", reminderInterval.getRemindAfter());
            request.getSession().setAttribute("forceShow", forceShow);
            return "loginEvent";

        }

        if (serviceSection) {
            final String choice = request.getParameter("choice");
            // do stuff with choice
            if (choice == null) {
                // error
                return "entry";
            }

            final String service = choice;

            final List<LoginEvent> serviceLoginEvents =
                    attributeReleaseModule.listLoginEvents(principalName, service, limitLoginEvents);

            final List<Map> serviceLoginList = processLoginEvents(serviceLoginEvents);
            request.getSession().setAttribute("lastLoginEvents", serviceLoginList);
            if (serviceLoginList.size() == limitLoginEvents) {
                request.getSession().setAttribute("loginEventListFull", true);
            }

            request.getSession().setAttribute("userName", Oracle.getInstance().getUserName());
            request.getSession().setAttribute("relyingParty", service);
            request.getSession().setAttribute("idpName", General.getInstance().getIdpName());

            //final List<Attribute> attributes = IdPHelper.getAttributes(servletContext, request);
            //request.getSession().setAttribute("attributes", attributes);

            return "serviceLogin";

        }

        if (helpButton) {
            return "entry";
        }

        return "entry";
    }

}
