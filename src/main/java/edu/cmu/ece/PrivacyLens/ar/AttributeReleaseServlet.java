/*
 * Copyright (c) 2011, SWITCH
 * Copyright (c) 2013, Carnegie Mellon University
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import edu.cmu.ece.PrivacyLens.Action;
import edu.cmu.ece.PrivacyLens.IdPHelper;
import edu.cmu.ece.PrivacyLens.Oracle;
import edu.cmu.ece.PrivacyLens.Util;
import edu.cmu.ece.PrivacyLens.ViewHelper;
import edu.cmu.ece.PrivacyLens.config.General;

/**
 * Attribute Release Servlet.
 */
public class AttributeReleaseServlet extends HttpServlet {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(AttributeReleaseServlet.class);

    /** The servlet context. */
    private ServletContext servletContext;

    /** The Attribute Release module. */
    private AttributeReleaseModule attributeReleaseModule;

    /** The SAML helper. */
    private SAMLHelper samlHelper;

    /** The view helper. */
    private ViewHelper viewHelper;

    /** {@inheritDoc} */
    public void init() throws ServletException {
        try {
            super.init();
            servletContext = getServletContext();
            Util.servletContext = servletContext;

            final WebApplicationContext appContext =
                    WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
            attributeReleaseModule =
                    (AttributeReleaseModule) appContext.getBean("PrivacyLens.attributeReleaseModule",
                            AttributeReleaseModule.class);
            samlHelper = (SAMLHelper) appContext.getBean("PrivacyLens.samlHelper", SAMLHelper.class);
            viewHelper = (ViewHelper) appContext.getBean("PrivacyLens.viewHelper", ViewHelper.class);
        } catch (final Throwable t) {
            logger.error("Error while initializing Attribute Release Servlet.", t);
            throw new ServletException(t);
        }
    }

    /** {@inheritDoc} */
    protected void service(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        try {
            logger.trace("Incoming signal");
            final Action action = ActionFactory.getAction(request);
            final String view = action.execute(request, response);
            logger.trace("Business logic completed, next view: {}", view);

            // save our destination view in the session so that ActionFactory can tell where we came from
            final HttpSession session = request.getSession(false);
            if (session != null) {
                session.setAttribute("view", view);
            }

            final String relyingPartyId = IdPHelper.getRelyingPartyId(servletContext, request);
            final Map<String, Object> context = new HashMap<String, Object>();
            final Oracle oracle = Oracle.getInstance();
            // may be done in SourceAction already
            oracle.setUserName(IdPHelper.getPrincipalName(servletContext, request));
            oracle.setRelyingPartyId(relyingPartyId);
            context.put("adminUrl", General.getInstance().getAdminUrl());
            context.put("adminMail", General.getInstance().getAdminMail());

            context.put("idpOrganization", General.getInstance().getOrganizationName());

            final Map<String, String> attributeReason = oracle.getAttributeReason(relyingPartyId);
            final Map<String, String> attributePrivacy = oracle.getAttributePrivacy(relyingPartyId);
            final Map<String, Boolean> attributeRequired = oracle.getAttributeRequired(relyingPartyId);
            final String serviceName = oracle.getServiceName();
            context.put("service", serviceName);

            //context.put("remoteAttributeReason", attributeReason);
            //context.put("remoteAttributePrivacy", attributePrivacy);
            //context.put("remoteAttributeRequired", attributeRequired);

            context.put("requirementStatement", "Use the toggle switches to select the items that will be sent to "
                    + serviceName + ". Items marked with * are required to access and personalize " + serviceName
                    + " and cannot be unselected.");
            context.put("relyingParty", samlHelper.readRelyingParty(relyingPartyId, viewHelper.selectLocale(request)));
            context.put("allowDenyRequired", false);

            if (view.equals("reminder")) {
                viewHelper.showView(servletContext, request, response, "attribute-reminder", context);
                return;
            } else if (view.equals("admin")) {
                // don't carry in a view attribute as we send the user to a new app
                request.getSession().removeAttribute("view");
                IdPHelper.redirectToUrl(servletContext, request, response, (String) context.get("adminUrl"));
                return;
            } else if (view.equals("sink")) {
                IdPHelper.setAttributeReleaseConsented(servletContext, request);
                IdPHelper.returnToIdP(servletContext, request, response);
            } else {
                if (!view.equals("entry")) {
                    logger.error("Fell through when setting up view {}", view);
                }
                // default and entry point
                context.put("allowGeneralConsent", attributeReleaseModule.isAllowGeneralConsent());
                viewHelper.showView(servletContext, request, response, "attribute-detail", context);
                return;
            }

            /*
             * if (view.equals(request.getPathInfo().substring(1)) { request.getRequestDispatcher("/WEB-INF/" + view +
             * ".jsp").forward(request, response); } else { response.sendRedirect(view); // We'd like to fire redirect
             * in case of a view change as result of the action (PRG pattern). }
             */
        } catch (final Exception e) {
            throw new ServletException("Executing action failed.", e);
        }
    }
}
