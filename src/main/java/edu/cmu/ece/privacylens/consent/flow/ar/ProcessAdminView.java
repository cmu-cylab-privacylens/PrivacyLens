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

package edu.cmu.ece.privacylens.consent.flow.ar;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.opensaml.profile.context.EventContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import edu.cmu.ece.privacylens.IdPHelper;
import edu.cmu.ece.privacylens.ar.AdminLoginEventPrepare;
import edu.cmu.ece.privacylens.ar.AdminServiceLoginPrepare;
import edu.cmu.ece.privacylens.ar.AttributeReleaseModule;
import edu.cmu.ece.privacylens.ar.LoginEvent;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * Get toggle beans
 *
 */

public final class ProcessAdminView extends AbstractAttributeReleaseAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(ProcessAdminView.class);


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

        final HttpServletRequest request =
                (HttpServletRequest) requestContext.getExternalContext()
                        .getNativeRequest();

        final String principalName =
                IdPHelper.getPrincipalName(profileRequestContext);

        final String sectionParameter = request.getParameter("section");
        final boolean loginEventSection =
                (sectionParameter.equals("loginEvent"));
        final boolean serviceSection = (sectionParameter.equals("service"));

        final boolean helpButton = (request.getParameter("help") != null);

        final AttributeReleaseModule attributeReleaseModule =
                IdPHelper.getAttributeReleaseModule();

        if (loginEventSection && serviceSection) { // only one section should be defined
            // error
            final EventContext event = new EventContext();
            event.setEvent("error");
            profileRequestContext.addSubcontext(event);
            return;
        }

        if (loginEventSection) {
            final String choice = request.getParameter("choice");
            // do stuff with choice
            final LoginEvent loginEvent =
                    attributeReleaseModule.readLoginEvent(choice);

            if (loginEvent == null) {
                // error
                final EventContext event = new EventContext();
                event.setEvent("error");
                profileRequestContext.addSubcontext(event);
                return;
            }

            final AdminLoginEventPrepare.Databag databag =
                    new AdminLoginEventPrepare.Databag(attributeReleaseModule,
                            requestContext, principalName, loginEvent);
            AdminLoginEventPrepare.prepare(databag);

            final EventContext event = new EventContext();
            event.setEvent("loginEvent");
            profileRequestContext.addSubcontext(event);
            return;

        }

        if (serviceSection) {
            final String choice = request.getParameter("choice");
            // do stuff with choice
            if (choice == null) {
                // error
                final EventContext event = new EventContext();
                event.setEvent("error");
                profileRequestContext.addSubcontext(event);
                return;
            }

            final String service = choice;

            final AdminServiceLoginPrepare.Databag databag =
                    new AdminServiceLoginPrepare.Databag(
                            attributeReleaseModule, requestContext,
                            principalName, service);
            AdminServiceLoginPrepare.prepare(databag);

            final EventContext event = new EventContext();
            event.setEvent("serviceLogin");
            profileRequestContext.addSubcontext(event);
            return;

        }

        // fallthrough
        log.warn("Fell through");
        final EventContext event = new EventContext();
        event.setEvent("error");
        profileRequestContext.addSubcontext(event);
        return;
    }
}