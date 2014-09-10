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
import org.springframework.util.StringUtils;

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
public class SourceAction implements Action {
    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(SourceAction.class);

    private final String emailAdminBoilerText = HTMLUtils.getEmailAdminBoilerText(General.getInstance().getAdminMail());

    private String requestContextPath;

    private String relyingPartyId;

    private Oracle oracle;

    private List<ToggleBean> generateToggleFromAttributes(final List<Attribute> attributes,
            final Map<String, Boolean> settingsMap) {
        final List<ToggleBean> out = new ArrayList<ToggleBean>();

        final Map<String, Map> attrMap = oracle.getAttributeRequested(relyingPartyId);
        final Map<String, Map> attrGroups = oracle.getAttributeGroupRequested(relyingPartyId);

        final Map<String, List> beanToGroup = new HashMap<String, List>();

        final Map<String, ToggleBean> attributeBeans = new HashMap<String, ToggleBean>();
        for (final Attribute attribute : attributes) {
            final String attributeId = attribute.getId();
            if (!attrMap.containsKey(attributeId)) {
                continue;
            }

            final String attrReason = (String) attrMap.get(attributeId).get("reason");
            final String attrPrivacy = (String) attrMap.get(attributeId).get("privpolicy");
            final String group = (String) attrMap.get(attributeId).get("group");

            final ToggleBean bean = new ToggleBean();

            final boolean required = attrMap.get(attributeId).get("required") != null;

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
            stringBuilder.append("<p>" + attrReason + "</p><p>" + attrPrivacy + "</p>");
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
                logger.error("{} did not validate", attribute);
            }

            if (group != null) {
                if (!beanToGroup.containsKey(group)) {
                    beanToGroup.put(group, new ArrayList<String>());
                }
                beanToGroup.get(group).add(attributeId);
            }
            attributeBeans.put(attributeId, bean);

        }

        final Map<String, ToggleBean> groupHandle;
        for (final String groupId : beanToGroup.keySet()) {

            final String attrReason = (String) attrGroups.get(groupId).get("reason");
            final String attrPrivacy = (String) attrGroups.get(groupId).get("privpolicy");

            final ToggleBean bean = new ToggleBean();

            final boolean required = attrGroups.get(groupId).get("required") != null;
            final String description = (String) attrGroups.get(groupId).get("description");

            final List<String> attrList = beanToGroup.get(groupId);
            boolean value = true;
            for (final String attr : attrList) {
                final ToggleBean memberBean = attributeBeans.get(attr);
                attributeBeans.remove(attr);
                // XXX mixed values?
                value &= memberBean.isValue();
                bean.addMember(memberBean);
            }
            bean.setValue(value);

            if (required) {
                bean.setImmutable(true);
                bean.setImageTrue(requestContextPath + "/PrivacyLens/force_sending.png");
            } else {
                bean.setImmutable(false);
                bean.setImageFalse(requestContextPath + "/PrivacyLens/not_sending.png");
                bean.setImageTrue(requestContextPath + "/PrivacyLens/sending.png");
            }

            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<p>" + attrReason + "</p><p>" + attrPrivacy + "</p>");
            stringBuilder.append("<p>");

            // eduPersonEntitlement is meaningless to the end user
            /*
            if (!attributeId.equals("eduPersonEntitlement")) {
                stringBuilder.append("Your " + attribute.getDescription() + " is " + '"' + attribute.getValues().get(0)
                        + "\". ");
            }
             */
            final List<String> subValues = new ArrayList<String>();

            // this is annoying. we have attribute definitions from the oracle
            // but we have to look through the actual attribute values.

            for (final String attrName : attrList) {
                for (final Attribute attr : attributes) {
                    if (attr.getId().equals(attrName)) {
                        try {
                            final String attrValue = attr.getValues().get(0);
                            subValues.add(attr.getValues().get(0));
                        } catch (final IndexOutOfBoundsException x) {
                            subValues.add("[blank]");
                        }
                    }
                }
            }

            final String subValuesText = StringUtils.collectionToDelimitedString(subValues, ", ");

            stringBuilder.append("Your " + description + " includes the items (");
            stringBuilder.append(subValuesText);
            stringBuilder.append("). ");

            stringBuilder.append("If you continue to " + oracle.getServiceName() + ", your " + description + " will ");
            stringBuilder.append(value ? "" : "not");
            stringBuilder.append(" be sent to it. Use the toggle switch to change this setting.");
            stringBuilder.append("</p>");
            stringBuilder.append(emailAdminBoilerText);

            final String explanation = stringBuilder.toString();
            bean.setExplanation(explanation);
            bean.setExplanationIcon(requestContextPath + "/PrivacyLens/info.png");
            bean.setTextDiv("attributeReleaseAttribute");
            bean.setImageDiv("attributeReleaseControl");
            bean.setParameter(groupId);

            stringBuilder.setLength(0);
            stringBuilder.append(description);
            // eduPersonEntitlement is meaningless to the end user
            /*
            if (!attributeId.equals("eduPersonEntitlement")) {
                stringBuilder.append(" (<b>");
                stringBuilder.append(attribute.getValues().get(0));
                stringBuilder.append("</b>)");
            }
             */

            stringBuilder.append(" (<b>");
            stringBuilder.append(subValuesText);
            stringBuilder.append("</b>)");

            if (required) {
                stringBuilder.append('*');
            }

            final String text = stringBuilder.toString();
            bean.setText(text);

            if (!bean.validate()) {
                logger.error("{} did not validate", groupId);
            }

            out.add(bean);
        }

        for (final ToggleBean bean : attributeBeans.values()) {
            out.add(bean);
        }

        return out;

    }

    /** {@inheritDoc} */
    public String execute(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        logger.debug("execute");

        final ServletContext servletContext = Util.servletContext;
        // this timestamp will describe this transaction
        final DateTime timestamp = new DateTime();
        oracle = Oracle.getInstance();
        final AttributeReleaseModule attributeReleaseModule = IdPHelper.attributeReleaseModule;

        final String principalName = IdPHelper.getPrincipalName(servletContext, request);
        oracle.setUserName(principalName);

        relyingPartyId = IdPHelper.getRelyingPartyId(servletContext, request);
        oracle.setRelyingPartyId(relyingPartyId);
        requestContextPath = request.getContextPath();

        // assemble list of attributes
        final List<Attribute> attributes = IdPHelper.getAttributes(servletContext, request);
        // XXX needed?
        request.getSession().setAttribute("attributes", attributes);

        logger.debug("principal: {} rpid: {}", principalName, relyingPartyId);

        // XXX obtain these from user preferences!
        final Map<String, Boolean> consentByAttribute =
                attributeReleaseModule.getAttributeConsent(principalName, relyingPartyId, attributes);

        final List<ToggleBean> attributeBeans = generateToggleFromAttributes(attributes, consentByAttribute);
        request.getSession().setAttribute("attributeBeans", attributeBeans);

        request.getSession().setAttribute("timestamp", timestamp);

        for (final Attribute attribute : attributes) {
            if (attribute.getId().equals("cn")) {
                Oracle.getInstance().setUserName(attribute.getValues().get(0));
            }

        }
        try {
            request.getSession().setAttribute("userName", Oracle.getInstance().getUserName());
            request.getSession().setAttribute("idpName", General.getInstance().getIdpName());

            if (attributeReleaseModule.isForceShowInterface(principalName, relyingPartyId)) {
                logger.debug("Interface forced");
                return "entry";
            }
            // does the user want to be bothered
            final ReminderInterval reminderInterval =
                    attributeReleaseModule.getReminderInterval(principalName, relyingPartyId);
            logger.debug("Entering reminder interval for {} is {}", principalName, reminderInterval);
            final int currentCount = reminderInterval.getCurrentCount() + 1;
            final int remindAfter = reminderInterval.getRemindAfter();
            final int modulo = currentCount % remindAfter;
            reminderInterval.setCurrentCount(modulo);
            attributeReleaseModule.updateReminderInterval(reminderInterval);
            if (modulo == 0) {
                final String attributeList = getReminderAttributes(attributes, consentByAttribute);
                request.getSession().setAttribute("attributeList", attributeList);
                logger.debug("Reminder due");
                return "reminder";
            } else {
                logger.debug("No reminder due");
                // make entry in login database
                final Oracle oracle = Oracle.getInstance();
                attributeReleaseModule.addLogin(principalName, oracle.getServiceName(), relyingPartyId, timestamp,
                        attributes);
                return "sink";
            }

        } catch (final Exception x) {
            logger.error("Exception happened: {}", x);
            // XXX throw an unchecked exception for now...
            logger.error("{}", 3 / 0);
        }

        logger.debug("Fell through");
        return "entry";
    }

    /**
     * Obtains a string list of attributes, and possibly their values for insertion into reminder view
     * 
     * @return html block to insert into view
     */
    private String getReminderAttributes(final List<Attribute> attributes, final Map<String, Boolean> settingsMap) {
        final List<String> list = new ArrayList<String>();

        for (final Attribute attr : attributes) {
            final StringBuilder sb = new StringBuilder();
            final boolean include = settingsMap.get(attr.getId());
            if (!include) {
                continue;
            }
            sb.append(attr.getDescription());
            // eduPersonEntitlement is meaningless to end user
            if (!attr.getId().equals("eduPersonEntitlement")) {
                sb.append(" (<strong>");
                // XXX multiple values?
                sb.append(attr.getValues().get(0));
                sb.append("</strong>)");
            }
            list.add(sb.toString());
        }
        final String out = Util.listToString(list);
        return out;
    }
}
