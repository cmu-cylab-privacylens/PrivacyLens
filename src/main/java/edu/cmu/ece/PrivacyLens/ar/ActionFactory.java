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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.ece.PrivacyLens.Action;

/**
 *
 */
public class ActionFactory {
    private static Map<String, Action> actions;

    /** Class logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ActionFactory.class);

    static {
        // defines what action should handle the response from a particular view
        actions = new HashMap<String, Action>();
        actions.put("source", new SourceAction());
        actions.put("entry", new EntryAction());
        actions.put("reminder", new ReminderAction());
        // sink action will return to IdP, we shouldn't get any requests for actions from there
    }

    public static Action getAction(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session == null) {
            // should already have a session from Shibboleth, but just cover the case
            LOGGER.debug("ActionFactory did not find a session");
            return actions.get("source");
        }

        // attribute view is set in the controller
        final Object sourceViewO = session.getAttribute("view");
        if (sourceViewO == null || !(sourceViewO instanceof String)) {
            // corrupt or null, send back to source
            LOGGER.debug("ActionFactory found corrupt flow information");
            return actions.get("source");
        }

        final String sourceView = (String) sourceViewO;

        LOGGER.trace("ActionFactory handling request from view {}", sourceView);
        final Action returnAction = actions.get(sourceView);
        if (returnAction == null) {
            // no action found, send back to source
            return actions.get("source");
        }

        // finally return the corresponding action
        LOGGER.trace("ActionFactory returning action {}", returnAction);
        return returnAction;
    }
}
