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

import java.util.List;
import java.util.Map;

import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;

import edu.cmu.ece.privacylens.config.General;

/**
 * Prepares the view for showing logins by service
 */
public class AdminServiceLoginPrepare {

    public static class Databag {
        private final AttributeReleaseModule attributeReleaseModule;

        private final String principalName;

        private final RequestContext requestContext;

        private final String service;

        public Databag(final AttributeReleaseModule attributeReleaseModule,
                final RequestContext requestContext,
                final String principalName,
            final String service) {
            this.attributeReleaseModule = attributeReleaseModule;
            this.principalName = principalName;
            this.requestContext = requestContext;
            this.service = service;
        }
    }

    public static void prepare(final Databag databag) {
        final AttributeReleaseModule attributeReleaseModule =
            databag.attributeReleaseModule;
        final String service = databag.service;
        final String principalName = databag.principalName;
        final RequestContext requestContext = databag.requestContext;
        final int limitLoginEvents = AdminViewHelper.limitLoginEvents;

        final MutableAttributeMap<Object> flowScope =
                requestContext.getFlowScope();

        final List<LoginEvent> serviceLoginEvents =
            attributeReleaseModule.listLoginEvents(principalName, service,
                limitLoginEvents);

        final List<Map> serviceLoginList =
            AdminViewHelper.processLoginEvents(serviceLoginEvents);
        flowScope.put("lastLoginEvents", serviceLoginList);
        if (serviceLoginList.size() == limitLoginEvents) {
            flowScope.put("loginEventListFull", true);
        }

        /*
        session.setAttribute("userName",
            Oracle.getInstance().getUserName());
            */
        flowScope.put("relyingParty", service);
        flowScope.put("idpName",
            General.getInstance().getIdpName());

        //final List<Attribute> attributes = IdPHelper.getAttributes(servletContext, request);
        //request.getSession().setAttribute("attributes", attributes);
    }
}
