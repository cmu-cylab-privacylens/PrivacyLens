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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final String explanationCoda = HTMLUtils
        .getEmailAdminBoilerText(General.getInstance().getAdminMail());

    private String requestContextPath;

    private String relyingPartyId;

    private Oracle oracle;

    private List<ToggleBean> generateToggleFromAttributes(
        final List<Attribute> attributes,
        final Map<String, Boolean> settingsMap, final Oracle oracle,
        final String relyingPartyId, final String requestContextPath) {
        final Map<String, Boolean> attrIsRequiredMap =
            oracle.getAttributeRequired(relyingPartyId);

        final Map<Attribute, ToggleBean> attributeBeans =
            InterfaceUtil.generateToggleFromAttributes(attributes, settingsMap,
                oracle, relyingPartyId, requestContextPath);

        // attribute group handling

        final Map<String, Map> attrGroups =
            oracle.getAttributeGroupRequested(relyingPartyId);

        final Map<String, List<Attribute>> beanToGroup =
            new HashMap<String, List<Attribute>>();

        final Map<String, String> attrGroupMap =
            oracle.getAttributeGroup(relyingPartyId);

        // if this attribute is part of a group, add it
        for (final Attribute attribute : attributeBeans.keySet()) {
            final String attributeId = attribute.getId();
            final String group = attrGroupMap.get(attributeId);
            if (group != null) {
                if (!beanToGroup.containsKey(group)) {
                    beanToGroup.put(group, new ArrayList<Attribute>());
                }
                beanToGroup.get(group).add(attribute);
            }
        }

        for (final String groupId : beanToGroup.keySet()) {

            final String attrReason =
                (String) attrGroups.get(groupId).get("reason");
            final String attrPrivacy =
                (String) attrGroups.get(groupId).get("privpolicy");
            final String description =
                (String) attrGroups.get(groupId).get("description");

            final boolean required =
                Boolean.valueOf((String) attrGroups.get(groupId)
                    .get("required"));
            final String parameter = groupId;
            final String serviceName = oracle.getServiceName();

            // for each group, pick the member attributes out of the map
            // created before
            final List<Attribute> groupAttrList = beanToGroup.get(groupId);

            final ToggleBean bean = new ToggleBean();
            boolean value =
                InterfaceUtil.groupBeanToValue(bean, groupAttrList,
                    attributeBeans);

            value = required || value;

            // similarity start
            InterfaceUtil.setValueAndImages(bean, requestContextPath, value,
                required);

            // set up privacy and request reason text
            final StringBuilder explanationStringBuilder = new StringBuilder();
            InterfaceUtil.addPrivacyAndReasonText(explanationStringBuilder, attrReason,
                attrPrivacy);
            // similarity end

            final List<String> subValues = new ArrayList<String>();

            // this is annoying. we have attribute definitions from the oracle
            // but we have to look through the actual attribute values.

            for (final Attribute attribute : groupAttrList) {
                final String attributeId = attribute.getId();

                // skip attribute if not requested
                if (!attrIsRequiredMap.containsKey(attributeId)) {
                    continue;
                }

                if (!attribute.isMachineReadable()) {
                    try {
                        final String attrValue =
                            Util.listToString(attribute.getValues());
                        subValues.add(attrValue);
                    } catch (final IndexOutOfBoundsException x) {
                        subValues.add("[blank]");
                    }
                } else {
                    //subValues.add("[machine readable]");
                }

            }

            String valueText = null;
            if (subValues.size() != 0) {
                valueText = Util.listToString(subValues);
            }

            if (valueText != null) {
                explanationStringBuilder.append("Your " + description
                    + " includes the items (");
                explanationStringBuilder.append(valueText);
                explanationStringBuilder.append("). ");
            }

            // set up consequence text
            InterfaceUtil.addConsequenceText(explanationStringBuilder, serviceName, value,
 description, explanationCoda);

            final String explanation = explanationStringBuilder.toString();
            bean.setExplanation(explanation);

            bean.setParameter(parameter);

            // set up first presentation
            InterfaceUtil.setBeanText(bean, description, valueText, required);

            if (!bean.validate()) {
                logger.error("{} did not validate", parameter);
            }

            // XXXstroucki should we do a super attribute which can be sorted?
            final Attribute fakeAttribute = new Attribute(groupId, null);
            attributeBeans.put(fakeAttribute, bean);
        }

        // now sort this based on preference in config
        final List<ToggleBean> out = InterfaceUtil.sortBeans(attributeBeans);

        return out;

    }

    /** {@inheritDoc} */
    public String execute(final HttpServletRequest request,
        final HttpServletResponse response) throws Exception {
        logger.debug("execute");

        final ServletContext servletContext = Util.servletContext;
        // this timestamp will describe this transaction
        final DateTime timestamp = new DateTime();
        oracle = Oracle.getInstance();
        final AttributeReleaseModule attributeReleaseModule =
            IdPHelper.attributeReleaseModule;

        // assemble list of attributes
        final List<Attribute> attributes =
            IdPHelper.getAttributes(servletContext, request);

        // personalize the application. Use displayName if available, otherwise leave
        // it with the login principal name.
        final String principalName =
            IdPHelper.getPrincipalName(servletContext, request);
        oracle.setUserName(principalName);
        for (final Attribute attribute : attributes) {
            if (attribute.getId().equals("displayName")) {
                Oracle.getInstance().setUserName(attribute.getValues().get(0));
            }
        }

        relyingPartyId = IdPHelper.getRelyingPartyId(servletContext, request);
        oracle.setRelyingPartyId(relyingPartyId);
        requestContextPath = request.getContextPath();

        // XXX needed?
        request.getSession().setAttribute("attributes", attributes);

        logger.debug("principal: {} rpid: {}", principalName, relyingPartyId);

        // XXX obtain these from user preferences!
        final Map<String, Boolean> consentByAttribute =
            attributeReleaseModule.getAttributeConsent(principalName,
                relyingPartyId, attributes);

        final List<ToggleBean> attributeBeans =
            generateToggleFromAttributes(attributes, consentByAttribute,
                oracle, relyingPartyId, requestContextPath);

        request.getSession().setAttribute("attributeBeans", attributeBeans);

        request.getSession().setAttribute("timestamp", timestamp);

        try {
            request.getSession().setAttribute("userName",
                Oracle.getInstance().getUserName());
            request.getSession().setAttribute("idpName",
                General.getInstance().getIdpName());

            if (attributeReleaseModule.isForceShowInterface(principalName,
                relyingPartyId)) {
                logger.debug("Interface forced");
                return "entry";
            }
            // does the user want to be bothered
            final ReminderInterval reminderInterval =
                attributeReleaseModule.getReminderInterval(principalName,
                    relyingPartyId);
            logger.debug("Entering reminder interval for {} is {}",
                principalName, reminderInterval);
            final int currentCount = reminderInterval.getCurrentCount() + 1;
            final int remindAfter = reminderInterval.getRemindAfter();
            final int modulo = currentCount % remindAfter;
            reminderInterval.setCurrentCount(modulo);
            attributeReleaseModule.updateReminderInterval(reminderInterval);
            if (modulo == 0) {
                final String attributeList =
                    getReminderAttributes(attributes, consentByAttribute);
                request.getSession().setAttribute("attributeList",
                    attributeList);
                logger.debug("Reminder due");
                return "reminder";
            } else {
                logger.debug("No reminder due");
                // make entry in login database, with all consented attributes
                final List<Attribute> consentedAttributes =
                    new ArrayList<Attribute>();
                for (final Attribute attribute : attributes) {
                    if (consentByAttribute.get(attribute.getId())) {
                        consentedAttributes.add(attribute);
                    }
                }
                final Oracle oracle = Oracle.getInstance();
                attributeReleaseModule.addLogin(principalName,
                    oracle.getServiceName(), relyingPartyId, timestamp,
                    consentedAttributes);
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
     * Obtains a string list of attributes, and possibly their values for
     * insertion into reminder view
     *
     * @return html block to insert into view
     */
    private String getReminderAttributes(final List<Attribute> attributes,
        final Map<String, Boolean> settingsMap) {
        final List<String> list = new ArrayList<String>();

        for (final Attribute attr : attributes) {
            final StringBuilder sb = new StringBuilder();
            final boolean include = settingsMap.get(attr.getId());
            if (!include) {
                continue;
            }
            sb.append(attr.getDescription());
            // don't present values of machine readable attributes
            if (!attr.isMachineReadable()) {
                sb.append(" (<strong>");
                sb.append(Util.listToString(attr.getValues()));
                sb.append("</strong>)");
            }
            list.add(sb.toString());
        }
        final String out = Util.listToString(list);
        return out;
    }
}
