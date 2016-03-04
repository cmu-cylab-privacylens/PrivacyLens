/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2016 Carnegie Mellon University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the names of the copyright holders nor the
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

package edu.cmu.ece.privacylens.consent.flow.ar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import edu.cmu.ece.privacylens.IdPHelper;
import edu.cmu.ece.privacylens.Oracle;
import edu.cmu.ece.privacylens.ar.AdminViewHelper;
import edu.cmu.ece.privacylens.ar.AttributeReleaseModule;
import edu.cmu.ece.privacylens.ar.LoginEvent;
import edu.cmu.ece.privacylens.config.General;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.idp.profile.interceptor.AbstractProfileInterceptorAction;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * Attribute consent action to populate the interface with useful textual info
 *
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @post See above.
 */
public class DecorateEvents extends AbstractProfileInterceptorAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(DecorateEvents.class);

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
    }

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        final RequestContext requestContext =
                RequestContextHolder.getRequestContext();

        final MutableAttributeMap<Object> flowScope =
                requestContext.getFlowScope();

        final String relyingPartyId =
                IdPHelper.getRelyingPartyId(profileRequestContext);
        flowScope.put("service",
                Oracle.getInstance().getServiceName(relyingPartyId));
        flowScope.put("idpOrganization", General.getInstance().getOrganizationName());

        final AttributeReleaseModule attributeReleaseModule =
                IdPHelper.getAttributeReleaseModule();
        final String principalName =
                IdPHelper.getPrincipalName(profileRequestContext);
        final int limitLoginEvents = AdminViewHelper.limitLoginEvents;
        final int limitRelyingPartyList = AdminViewHelper.limitRelyingPartyList;

        final List<LoginEvent> lastLoginEvents =
                attributeReleaseModule.listLoginEvents(principalName, "",
                        limitLoginEvents);
        final List<Map> loginEventList =
                AdminViewHelper.processLoginEvents(lastLoginEvents);
        flowScope.put("lastLoginEvents", loginEventList);
        if (loginEventList.size() == limitLoginEvents) {
            flowScope.put("loginEventFull", true);
        }

        final List<String> servicesList =
                attributeReleaseModule.listRelyingParties(principalName,
                        limitRelyingPartyList);
        final Map<String, List> serviceLoginEventMap =
                new HashMap<String, List>();
        for (final String service : servicesList) {
            final List<LoginEvent> serviceLoginEvents =
                    attributeReleaseModule.listLoginEvents(principalName,
                            service, limitLoginEvents);
            final List<Map> serviceLoginEventList =
                    AdminViewHelper.processLoginEvents(serviceLoginEvents);
            serviceLoginEventMap.put(service, serviceLoginEventList);
        }
        flowScope.put("relyingPartiesList", servicesList);
        flowScope.put("serviceLoginEvents", serviceLoginEventMap);
        if (servicesList.size() == limitRelyingPartyList) {
            flowScope.put("relyingPartyListFull", true);
        }

        log.debug("{} Decorated login data", getLogPrefix());
    }

}
