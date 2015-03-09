/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2014-2015 Carnegie Mellon University
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import edu.cmu.ece.PrivacyLens.HTMLUtils;
import edu.cmu.ece.PrivacyLens.Oracle;
import edu.cmu.ece.PrivacyLens.ToggleBean;
import edu.cmu.ece.PrivacyLens.Util;
import edu.cmu.ece.PrivacyLens.config.General;

/**
 * Utility functions to help with the PrivacyLens interface
 *
 */

public class InterfaceUtil {

    /** Class logger. */
    private static final Logger logger = LoggerFactory
        .getLogger(InterfaceUtil.class);

    private static final String explanationCoda = HTMLUtils
        .getEmailAdminBoilerText(General.getInstance().getAdminMail());

    /**
     * Generate a list of toggle beans
     *
     * @param attributes
     * @param settingsMap
     * @param oracle
     * @param relyingPartyId
     * @param requestContextPath
     * @return
     */
    public static List<ToggleBean> generateToggleListFromAttributes(
        final List<Attribute> attributes,
        final Map<String, Boolean> settingsMap, final Oracle oracle,
        final String relyingPartyId, final String requestContextPath) {
        final Map<Attribute, ToggleBean> attributeBeans =
            generateToggleFromAttributes(attributes, settingsMap, oracle,
                relyingPartyId, requestContextPath);
        return new ArrayList<ToggleBean>(attributeBeans.values());
    }

    /**
     * Generate a map of toggle beans
     *
     * @param attributes
     * @param settingsMap
     * @param oracle
     * @param relyingPartyId
     * @param requestContextPath
     * @return
     */
    public static Map<Attribute, ToggleBean> generateToggleFromAttributes(
        final List<Attribute> attributes,
        final Map<String, Boolean> settingsMap, final Oracle oracle,
        final String relyingPartyId, final String requestContextPath) {
        final Map<String, Boolean> attrIsRequiredMap =
            oracle.getAttributeRequired(relyingPartyId);
        final Map<String, String> attrReasonMap =
            oracle.getAttributeReason(relyingPartyId);
        final Map<String, String> attrPrivacyMap =
            oracle.getAttributePrivacy(relyingPartyId);

        logger.debug("attribute size {}", attributes.size());
        logger.debug("attribute map size {}", attrIsRequiredMap.size());

        final Map<Attribute, ToggleBean> attributeBeans =
            new HashMap<Attribute, ToggleBean>();

        for (final Attribute attribute : attributes) {
            final String attributeId = attribute.getId();

            // skip attribute if not requested
            if (!attrIsRequiredMap.containsKey(attributeId)) {
                continue;
            }

            final String attrReason = attrReasonMap.get(attributeId);
            final String attrPrivacy = attrPrivacyMap.get(attributeId);
            final String description = attribute.getDescription();

            final boolean required = attrIsRequiredMap.get(attributeId);
            final boolean value = required || settingsMap.get(attributeId);
            final String parameter = attributeId;
            final String serviceName = oracle.getServiceName();

            final ToggleBean bean = new ToggleBean();

            setValueAndImages(bean, requestContextPath, value, required);

            // set up privacy and request reason text
            final StringBuilder explanationStringBuilder = new StringBuilder();
            addPrivacyAndReasonText(explanationStringBuilder, attrReason, attrPrivacy);

            String valueText = null;
            // don't present values of machine readable attributes
            if (!attribute.isMachineReadable()) {
                valueText = Util.listToString(attribute.getValues());
            }

            // don't present values of machine readable attributes
            if (valueText != null) {
                explanationStringBuilder.append("Your " + description + " is "
                    + '"' + valueText + "\". ");
            }

            // set up consequence text
            addConsequenceText(explanationStringBuilder, serviceName, value,
                description, explanationCoda);

            final String explanation = explanationStringBuilder.toString();
            bean.setExplanation(explanation);

            bean.setParameter(parameter);

            // set up first presentation
            setBeanText(bean, description, valueText, required);

            if (!bean.validate()) {
                logger.error("{} did not validate", parameter);
            }

            attributeBeans.put(attribute, bean);

        }
        return attributeBeans;
    }

    /**
     * Add consequence text to the stringBuilder
     *
     * @param stringBuilder
     * @param serviceName "CMU's calendar"
     * @param value boolean send or not send
     * @param description "name"
     * @param coda Text to be added at the end
     */
    public static void addConsequenceText(final StringBuilder stringBuilder,
        final String serviceName, final boolean value,
        final String description, final String coda) {
        // TODO Auto-generated method stub
        stringBuilder.append("If you continue to " + serviceName + ", your "
            + description);
        // create a div here to be able to change the text?
        // like some random string that can be passed back?
        stringBuilder.append(" will ");
        stringBuilder.append(value ? "" : "not");
        stringBuilder
            .append(" be sent to it. Use the toggle switch to change this setting.");
        stringBuilder.append("</p>");
        stringBuilder.append(coda);
    }

    /**
     * add reason and privacy policy text to the stringBuilder
     *
     * @param stringBuilder
     * @param attrReason "The site needs the attribute to function"
     * @param attrPrivacy "The site will not divulge the value"
     */
    public static void addPrivacyAndReasonText(
        final StringBuilder stringBuilder, final String attrReason,
        final String attrPrivacy) {
        // TODO Auto-generated method stub
        stringBuilder.append("<p>" + attrReason + "</p><p>" + attrPrivacy
            + "</p>");
        stringBuilder.append("<p>");
    }

    /**
     * set up value, explanationicon, textdiv, imagediv, images and immutable
     * fields for a toggle bean
     *
     * @param bean
     * @param requestContextPath
     * @param value boolean "send/not send"
     * @param required boolean
     */
    public static void setValueAndImages(final ToggleBean bean,
        final String requestContextPath, final boolean value,
        final boolean required) {
        // TODO Auto-generated method stub
        bean.setValue(value);

        bean.setExplanationIcon(requestContextPath + "/PrivacyLens/info.png");
        bean.setTextDiv("attributeReleaseAttribute");
        bean.setImageDiv("attributeReleaseControl");

        if (required) {
            bean.setImmutable(true);
            bean.setImageTrue(requestContextPath
                + "/PrivacyLens/force_sending.png");
        } else {
            bean.setImmutable(false);
            bean.setImageFalse(requestContextPath
                + "/PrivacyLens/not_sending.png");
            bean.setImageTrue(requestContextPath + "/PrivacyLens/sending.png");
        }

    }

    /**
     * Sort the attribute beans into a list according to config preferences
     *
     * @param attributeBeans
     * @return a sorted list of toggle beans
     */
    public static List<ToggleBean> sortBeans(
        final Map<Attribute, ToggleBean> attributeBeans) {
        // sort the map based on preference in config
        final List<ToggleBean> out = new ArrayList<ToggleBean>();
        // XXXstroucki really complex.
        final ServletContext servletContext = Util.servletContext;
        final WebApplicationContext appContext =
            WebApplicationContextUtils
                .getRequiredWebApplicationContext(servletContext);
        final SAMLHelper samlHelper =
            (SAMLHelper) appContext.getBean("PrivacyLens.samlHelper",
                SAMLHelper.class);
        final AttributeProcessor attributeProcessor =
            samlHelper.getAttributeProcessor();

        final List<Attribute> attributeKeys =
            new ArrayList<Attribute>(attributeBeans.keySet());
        attributeProcessor.sortAttributes(attributeKeys);
        for (final Attribute attribute : attributeKeys) {
            out.add(attributeBeans.get(attribute));
        }
        return out;
    }

    public static boolean groupBeanToValue(final ToggleBean bean,
        final List<Attribute> groupAttrList,
        final Map<Attribute, ToggleBean> attributeBeans) {
        // assign value true, unless the members have different settings
        boolean value = true;

        for (final Attribute attr : groupAttrList) {
            final ToggleBean memberBean = attributeBeans.get(attr);
            attributeBeans.remove(attr);
            // XXX mixed values?
            value &= memberBean.isValue();
            bean.addMember(memberBean);
        }

        return value;
    }

    /**
     * Generate the text (visible) field for a toggle bean
     *
     * @param bean
     * @param description "color"
     * @param valueText "blue"
     * @param isStar whether to put a star at the end
     */
    public static void setBeanText(final ToggleBean bean,
        final String description, final String valueText, final boolean isStar) {
        final StringBuilder textStringBuilder = new StringBuilder();
        textStringBuilder.append(description);

        if (valueText != null) {
            textStringBuilder.append(" (<b>");
            textStringBuilder.append(valueText);
            textStringBuilder.append("</b>)");
        }

        if (isStar) {
            textStringBuilder.append('*');
        }

        final String text = textStringBuilder.toString();
        bean.setText(text);
    }

}
