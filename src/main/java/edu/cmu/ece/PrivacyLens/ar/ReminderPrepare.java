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
package edu.cmu.ece.PrivacyLens.ar;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import edu.cmu.ece.PrivacyLens.Util;

/**
 * Prepares the view for showing the attribute reminder
 */
public class ReminderPrepare {

    public static class Databag {
        private final List<Attribute> reminderAttributes;

        private final HttpSession session;

        public Databag(final List<Attribute> reminderAttributes,
            final HttpSession session) {
            this.reminderAttributes = reminderAttributes;
            this.session = session;
        }
    }

    /**
     * Obtains a string list of attributes, and possibly their values for
     * insertion into reminder view
     *
     * @return html block to insert into view
     */
    private static String
        getReminderAttributes(final List<Attribute> attributes) {
        final List<String> list = new ArrayList<String>();

        for (final Attribute attr : attributes) {
            final StringBuilder sb = new StringBuilder();

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

    public static void prepare(final Databag databag) {
        final HttpSession session = databag.session;
        final List<Attribute> reminderAttributes = databag.reminderAttributes;

        final String attributeList = getReminderAttributes(reminderAttributes);
        session.setAttribute("attributeList", attributeList);
    }
}
