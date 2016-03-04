/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2015-2016, Carnegie Mellon University
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

package edu.cmu.ece.privacylens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import edu.cmu.ece.privacylens.ar.Attribute;
import edu.cmu.ece.privacylens.config.General;
import edu.cmu.ece.privacylens.consent.flow.ar.AbstractAttributeReleaseAction;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * Get toggle beans
 *
 */

public final class ToggleBeanService extends AbstractAttributeReleaseAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(ToggleBeanService.class);

    private static final String explanationCoda = HTMLUtils
            .getEmailAdminBoilerText(General.getInstance().getAdminMail());

    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(
            @Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        final RequestContext requestContext =
                RequestContextHolder.getRequestContext();

        final MutableAttributeMap<Object> flowScope =
                requestContext.getFlowScope();

        final HttpServletRequest request =
                (HttpServletRequest) requestContext.getExternalContext()
                        .getNativeRequest();
        final String requestContextPath = request.getContextPath();
        final Oracle oracle = Oracle.getInstance();

        final List<Attribute> attributes =
                (List<Attribute>) flowScope.get("attributes");

        final String relyingPartyId =
                IdPHelper.getRelyingPartyId(profileRequestContext);
        final Map<String, Boolean> settingsMap =
                (Map<String, Boolean>) flowScope.get("attributeSettings");
        final List<ToggleBean> toggleBeans =
                generateToggleFromAttributes(attributes, settingsMap, oracle,
                        relyingPartyId, requestContextPath);

        flowScope.put("attributeBeans", toggleBeans);

        log.debug("{} added toggle beans", getLogPrefix());
    }

    private List<ToggleBean> generateToggleFromAttributes(
            final List<Attribute> attributes,
            final Map<String, Boolean> settingsMap, final Oracle oracle,
            final String relyingPartyId, final String requestContextPath) {
        final Map<String, Boolean> attrIsRequiredMap =
                oracle.getAttributeRequired(relyingPartyId);

        final Map<Attribute, ToggleBean> attributeBeans =
                InterfaceUtil
                        .generateToggleFromAttributes(attributes, settingsMap,
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

        for (final Entry<String, List<Attribute>> entry : beanToGroup
                .entrySet()) {
            final String groupId = entry.getKey();
            final String attrReason =
                    (String) attrGroups.get(groupId).get("reason");
            final String attrPrivacy =
                    (String) attrGroups.get(groupId).get("privpolicy");
            final String description =
                    (String) attrGroups.get(groupId).get("description");

            final boolean required =
                    Boolean.valueOf((String) attrGroups.get(groupId).get(
                            "required"));
            final String parameter = groupId;
            final String serviceName = oracle.getServiceName(relyingPartyId);

            // for each group, pick the member attributes out of the map
            // created before
            final List<Attribute> groupAttrList = entry.getValue();

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
            InterfaceUtil.addPrivacyAndReasonText(explanationStringBuilder,
                    attrReason, attrPrivacy);
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
            InterfaceUtil.addConsequenceText(explanationStringBuilder,
                    serviceName, value, description, explanationCoda);

            final String explanation = explanationStringBuilder.toString();
            bean.setExplanation(explanation);

            bean.setParameter(parameter);

            // set up first presentation
            InterfaceUtil.setBeanText(bean, description, valueText, required);

            if (!bean.validate()) {
                log.error("{} did not validate", parameter);
            }

            // XXXstroucki should we do a super attribute which can be sorted?
            final Attribute fakeAttribute = new Attribute(groupId, null);
            attributeBeans.put(fakeAttribute, bean);
        }

        // now sort this based on preference in config
        final List<ToggleBean> out = InterfaceUtil.sortBeans(attributeBeans);

        return out;

    }

    // XXXstroucki probably remove
    /*
    private List<ToggleBean> getToggleBeans(final RequestContext rc) {
        final HttpServletRequest request =
                (HttpServletRequest) rc.getExternalContext().getNativeRequest();
        final String requestContextPath = request.getContextPath();
        final Oracle oracle = Oracle.getInstance();

        final RequestContext requestContext =
                RequestContextHolder.getRequestContext();

        final MutableAttributeMap<Object> flowScope =
                requestContext.getFlowScope();

        final List<Attribute> attributes =
                (List<Attribute>) flowScope.get("attributes");

        final String relyingPartyId = "foobar";
        final Map<String, Boolean> settingsMap = null;

        final List<ToggleBean> out =
                generateToggleFromAttributes(attributes, settingsMap, oracle,
                        relyingPartyId, requestContextPath);
        return out;

    }
    */
}
