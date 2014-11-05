/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2014 Carnegie Mellon University
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

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.ece.PrivacyLens.IdPHelper;
import edu.cmu.ece.PrivacyLens.Oracle;
import edu.cmu.ece.PrivacyLens.Util;
import edu.cmu.ece.PrivacyLens.config.General;

/**
 * This should eventually be put elsewhere
 * 
 */
public class AdminViewHelper {
    /** Class logger. */
    private static final Logger logger = LoggerFactory.getLogger(AdminViewHelper.class);

    private static List<Map> processLoginEvents(final List<LoginEvent> loginEventsList) {
        final List<Map> out = new ArrayList<Map>();
        for (final LoginEvent loginEvent : loginEventsList) {
            loginEvent.getEventDetailHash();
            final DateTime loginEventDate = loginEvent.getDate();
            final DateTime now = new DateTime();
            final long relativeTime = now.getMillis() - loginEventDate.getMillis();
            final String dateTimeString = Util.millisToDuration(relativeTime);
            final String serviceString = loginEvent.getServiceName();
            final String loginEventId = loginEvent.getEventDetailHash();
            final Map<String, Object> map = new HashMap<String, Object>();
            map.put("dateTimeString", dateTimeString);
            map.put("service", serviceString);
            map.put("loginEvent", loginEvent);
            map.put("loginEventId", loginEventId);
            out.add(map);
        }
        return out;
    }

    public static void setupView(final String view, final ServletContext servletContext,
            final HttpServletRequest request, final AttributeReleaseModule attributeReleaseModule) {
        logger.trace("setupView called for view {}", view);
        if (view.equals("entry")) {
            final String principalName = IdPHelper.getPrincipalName(servletContext, request);

            // set up target view
            final int limitLoginEvents = 6;
            final int limitRelyingPartyList = 6;

            final List<LoginEvent> lastLoginEvents =
                    attributeReleaseModule.listLoginEvents(principalName, "", limitLoginEvents);
            final List<Map> loginEventList = processLoginEvents(lastLoginEvents);
            request.getSession().setAttribute("lastLoginEvents", loginEventList);
            if (loginEventList.size() == limitLoginEvents) {
                request.getSession().setAttribute("loginEventFull", true);
            }

            final List<String> servicesList =
                    attributeReleaseModule.listRelyingParties(principalName, limitRelyingPartyList);
            final Map<String, List> serviceLoginEventMap = new HashMap<String, List>();
            for (final String service : servicesList) {
                final List<LoginEvent> serviceLoginEvents =
                        attributeReleaseModule.listLoginEvents(principalName, service, limitLoginEvents);
                final List<Map> serviceLoginEventList = processLoginEvents(serviceLoginEvents);
                serviceLoginEventMap.put(service, serviceLoginEventList);
            }
            request.getSession().setAttribute("relyingPartiesList", servicesList);
            request.getSession().setAttribute("serviceLoginEvents", serviceLoginEventMap);
            if (servicesList.size() == limitRelyingPartyList) {
                request.getSession().setAttribute("relyingPartyListFull", true);
            }

            request.getSession().setAttribute("userName", Oracle.getInstance().getUserName());
            request.getSession().setAttribute("idpName", General.getInstance().getIdpName());
        }
    }
}
