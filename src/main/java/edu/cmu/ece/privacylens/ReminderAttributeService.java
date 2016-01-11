/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2015, Carnegie Mellon University
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
import java.util.List;
import java.util.Map;

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
 * Get reminder attributes
 *
 */

public final class ReminderAttributeService extends AbstractAttributeReleaseAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(ReminderAttributeService.class);

    private static final String explanationCoda = HTMLUtils
            .getEmailAdminBoilerText(General.getInstance().getAdminMail());

    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
    }

    /**
     * Obtains a string list of attributes, and possibly their values for
     * insertion into reminder view
     *
     * @return html block to insert into view
     */
    private String getReminderAttributes(final List<Attribute> attributes) {
        final List<String> list = new ArrayList<String>();
        final Map<String, Consent> consents =
                getConsentContext().getPreviousConsents();

        for (final Attribute attr : attributes) {
            final Consent attributeConsent = consents.get(attr.getId());
            if (attributeConsent == null || !attributeConsent.isApproved()) {
                continue;
            }
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

        // CHANGEME use previous consents
        final List<Attribute> attributes =
                (List<Attribute>) flowScope.get("attributes");

        final String attributeList = getReminderAttributes(attributes);

        flowScope.put("attributeList", attributeList);

        log.debug("{} added reminder list", getLogPrefix());
    }
}
