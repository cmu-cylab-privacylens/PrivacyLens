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

package ch.SWITCH.aai.uApprove.ar;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.SWITCH.aai.uApprove.IdPHelper;
import ch.SWITCH.aai.uApprove.Util;
import edu.cmu.ece.PrivacyLens.Action;
import edu.cmu.ece.PrivacyLens.Oracle;

/**
 * Act on what the user wants from the Reminder page If the user clicked yes, complete the transaction. If the user
 * clicked no, send him to the main page.
 */
public class ReminderAction implements Action {

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(ReminderAction.class);

    /** {@inheritDoc} */
    public String execute(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final boolean yesButton = (request.getParameter("yes") != null);
        // final boolean moreOptionsButton = (request.getParameter("moreOptions") != null);
        final boolean noButton = (request.getParameter("no") != null);
        // final boolean explainButton = (request.getParameter("explain") != null);

        final ServletContext servletContext = Util.servletContext;
        final AttributeReleaseModule attributeReleaseModule = IdPHelper.attributeReleaseModule;
        final String principalName = IdPHelper.getPrincipalName(servletContext, request);
        // this timestamp will describe this transaction
        final DateTime timestamp = new DateTime();

        // assemble list of attributes
        final List<Attribute> attributes = IdPHelper.getAttributes(servletContext, request);

        if (noButton) {
            return "entry";
        }

        if (yesButton) {
            // make entry in login database
            final Oracle oracle = Oracle.getInstance();
            // XXX bad hardcode
            attributeReleaseModule.addLogin(principalName, oracle.getServiceName(),
                    "https://scalepriv.ece.cmu.edu/shibboleth", timestamp, attributes);
            return "sink";
        }

        logger.warn("Fell through");
        return "sink";
    }
}
