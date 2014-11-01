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
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import edu.cmu.ece.PrivacyLens.Action;
import edu.cmu.ece.PrivacyLens.HTMLUtils;
import edu.cmu.ece.PrivacyLens.IdPHelper;
import edu.cmu.ece.PrivacyLens.Oracle;
import edu.cmu.ece.PrivacyLens.ToggleBean;
import edu.cmu.ece.PrivacyLens.Util;
import edu.cmu.ece.PrivacyLens.config.General;

/**
 * Causes the controller to go to entry state
 */
public class AdminServiceLoginAction implements Action {
    private final ServletContext servletContext;

    private final AttributeReleaseModule attributeReleaseModule;

    private String requestContextPath;

    private String relyingPartyId;

    private Oracle oracle;

    private final String emailAdminBoilerText = HTMLUtils.getEmailAdminBoilerText(General.getInstance().getAdminMail());

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(AdminServiceLoginAction.class);

    /** {@inheritDoc} */
    public AdminServiceLoginAction() {
        logger.trace("AdminEntryAction init");
        servletContext = Util.servletContext;

        final WebApplicationContext appContext =
                WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        attributeReleaseModule =
                (AttributeReleaseModule) appContext.getBean("PrivacyLens.attributeReleaseModule",
                        AttributeReleaseModule.class);
        logger.trace("AdminEntryAction init end arm: {}", attributeReleaseModule);

    }

    public List<ToggleBean> generateToggleFromAttributes(final List<Attribute> attributes,
            final Map<String, Boolean> settingsMap) {
        final Map<String, Boolean> attrMap = Oracle.getInstance().getAttributeRequired(relyingPartyId);
        final Map<String, String> attrReason = Oracle.getInstance().getAttributeReason(relyingPartyId);
        final Map<String, String> attrPrivacy = Oracle.getInstance().getAttributePrivacy(relyingPartyId);

        logger.debug("attribute size {}", attributes.size());
        logger.debug("attribute map size {}", attrMap.size());

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

            // don't present values of machine readable attributes
            if (!attribute.isMachineReadable()) {
                stringBuilder.append("Your " + attribute.getDescription() + " is " + '"'
                        + Util.listToString(attribute.getValues()) + "\". ");
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
            // don't present values of machine readable attributes
            if (!attribute.isMachineReadable()) {
                stringBuilder.append(" (<b>");
                stringBuilder.append(Util.listToString(attribute.getValues()));
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
        logger.trace("AdminServiceLoginAction execute sc: {}", servletContext);

        if (requestContextPath == null) {
            requestContextPath = request.getContextPath();
        }

        final boolean backButton = (request.getParameter("back") != null);

        if (backButton) {
            return "entry";
        }

        final String sectionParameter = request.getParameter("section");
        final boolean loginEventSection = (sectionParameter.equals("loginEvent"));

        //final boolean helpButton = (request.getParameter("help") != null);

        if (false) { // only one section should be defined
            // error
            return "entry";
        }

        oracle = Oracle.getInstance();
        if (loginEventSection) {
            final String choice = request.getParameter("choice");
            // do stuff with choice
            final LoginEvent loginEvent = attributeReleaseModule.readLoginEvent(choice);

            if (loginEvent == null) {
                // error
                return "entry";
            }

            final LoginEventDetail loginEventDetail = attributeReleaseModule.readLoginEventDetail(loginEvent);

            // the whole event is given to the jsp since additional text uses
            // some of the fields
            request.getSession().setAttribute("loginEvent", loginEvent);
            request.getSession().setAttribute("loginEventDetail", loginEventDetail);

            final StringBuffer sentInfoBuffer = new StringBuffer();
            for (final Attribute attribute : loginEventDetail.getAttributes()) {
                sentInfoBuffer.append(attribute.getDescription());
                // don't present values of machine readable attributes
                if (!attribute.isMachineReadable()) {
                    sentInfoBuffer.append(": \"" + Util.listToString(attribute.getValues()) + "\"");
                }

                sentInfoBuffer.append("<br/>");
            }
            final String sentInfo = sentInfoBuffer.toString();
            request.getSession().setAttribute("sentInfo", sentInfo);

            relyingPartyId = loginEvent.getServiceUrl();
            final String principalName = loginEvent.getUserId();

            logger.debug("rpid {}", relyingPartyId);

            final List<Attribute> attributes = IdPHelper.getAttributes(servletContext, request);
            final Map<String, Boolean> consentByAttribute =
                    attributeReleaseModule.getAttributeConsent(principalName, relyingPartyId, attributes);

            final List<ToggleBean> beanList = generateToggleFromAttributes(attributes, consentByAttribute);
            request.getSession().setAttribute("attributeBeans", beanList);
            logger.debug("beanList size {}", beanList.size());
            request.getSession().setAttribute("relyingParty", relyingPartyId);
            final boolean forceShow = attributeReleaseModule.isForceShowInterface(principalName, relyingPartyId);
            final ReminderInterval reminderInterval =
                    attributeReleaseModule.getReminderInterval(principalName, relyingPartyId);
            request.getSession().setAttribute("reminderInterval", reminderInterval.getRemindAfter());
            request.getSession().setAttribute("forceShow", forceShow);
            return "loginEvent";

        }

        return "entry";
    }

}
