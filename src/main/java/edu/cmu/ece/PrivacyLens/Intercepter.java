/*
 * Copyright (c) 2011, SWITCH
 * Copyright (c) 2013-2015, Carnegie Mellon University
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

package edu.cmu.ece.PrivacyLens;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ch.SWITCH.aai.uApprove.tou.ToUModule;
import edu.cmu.ece.PrivacyLens.ar.Attribute;
import edu.cmu.ece.PrivacyLens.ar.AttributeReleaseModule;
import edu.cmu.ece.PrivacyLens.ar.SAMLHelper;
import edu.cmu.ece.PrivacyLens.config.General;
import edu.internet2.middleware.shibboleth.idp.authn.PassiveAuthenticationException;

/**
 * PrivacyLens request intercepter.
 */
public class Intercepter implements Filter {

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(Intercepter.class);

    /** The servlet context. */
    private ServletContext servletContext;

    /** The Terms Of Use Module. */
    private ToUModule touModule;

    /** The Attribute Release Module. */
    private AttributeReleaseModule attributeReleaseModule;

    /** The SAML helper. */
    private SAMLHelper samlHelper;

    /** The view helper. */
    private ViewHelper viewHelper;

    /**
     * If set, PrivacyLens will be only enabled if authnContextClassRef equals
     * to
     * the requested one.
     */
    private String authnContextClassRef;

    /** {@inheritDoc} */
    public void init(final FilterConfig filterConfig) throws ServletException {
        try {
            servletContext = filterConfig.getServletContext();
            final WebApplicationContext appContext =
                WebApplicationContextUtils
                    .getRequiredWebApplicationContext(filterConfig
                        .getServletContext());
            touModule =
                (ToUModule) appContext.getBean("uApprove.touModule",
                    ToUModule.class);
            attributeReleaseModule =
                (AttributeReleaseModule) appContext.getBean(
                    "PrivacyLens.attributeReleaseModule",
                    AttributeReleaseModule.class);
            samlHelper =
                (SAMLHelper) appContext.getBean("PrivacyLens.samlHelper",
                    SAMLHelper.class);

            viewHelper =
                (ViewHelper) appContext.getBean("PrivacyLens.viewHelper",
                    ViewHelper.class);

            Validate
                .notNull(touModule, "ToU module isn't properly configured.");
            Validate.notNull(attributeReleaseModule,
                "Attribute Release module isn't properly configured.");
            Validate.notNull(samlHelper,
                "SAML Helper isn't properly configured.");
            Validate.notNull(viewHelper,
                "View Helper isn't properly configured.");

            authnContextClassRef =
                filterConfig.getInitParameter("authnContextClassRef");
            logger.debug("PrivacyLens initialized.{}",
                authnContextClassRef != null
                ? " authnContextClassRef is set to " + authnContextClassRef
                    : "");
        } catch (final Throwable t) {
            logger
                .error("Error while initializing PrivacyLens intercepter.", t);
            throw new ServletException(t);
        }
    }

    /** {@inheritDoc} */
    public void doFilter(final ServletRequest request,
        final ServletResponse response, final FilterChain chain)
        throws IOException, ServletException {

        Validate.isTrue(request instanceof HttpServletRequest,
            "Not an HttpServletRequest.");
        Validate.isTrue(response instanceof HttpServletResponse,
            "Not an HttpServletResponse.");

        try {
            intercept((HttpServletRequest) request,
                (HttpServletResponse) response, chain);
        } catch (final Throwable t) {
            logger.error("Error while intercepting request.", t);
            IdPHelper
                .handleException(servletContext, (HttpServletRequest) request,
                    (HttpServletResponse) response, t);
        }
    }

    /**
     * Intercepts requests to the IdP.
     *
     * @param request The request.
     * @param response The response.
     * @param chain The filter chain.
     * @throws IOException Throws IOException.
     * @throws ServletException Throws ServletException.
     */
    public void intercept(final HttpServletRequest request,
        final HttpServletResponse response, final FilterChain chain)
        throws IOException, ServletException {

        logger.trace("Entered intercept from URL {}", request.getPathInfo());

        if (authnContextClassRef != null
            && !StringUtils.equals(authnContextClassRef,
                IdPHelper.getAuthContextClassRef(servletContext, request))) {
            logger
                .trace(
                    "Intercept sending to filter after something with authnContextClassRef {}",
                authnContextClassRef);
            chain.doFilter(request, response);
            return;
        }

        // is this how it requests revocation of consent?
        if (!IdPHelper.isAuthenticated(servletContext, request, response)) {
            logger.trace("Request is not authenticated.");
            IdPHelper.setConsentRevocationRequested(servletContext, request);
            logger
                .trace("Intercept sending to filter at request not authenticated");
            chain.doFilter(request, response);
            return;
        }

        if (handleTermsOfUse(request, response)) {
            logger.trace("Intercept after TOU was handled");
            return;
        }

        if (handleAttributeRelease(request, response)) {
            logger.trace("Intercept after AR was handled");
            return;
        }

        logger.trace("Intercept sending to filter at end");
        chain.doFilter(request, response);
        return;
    }

    /**
     * Handles Terms Of Use.
     *
     * @param request The request.
     * @param response The response.
     * @return Returns true if ToU were handled.
     */
    private boolean handleTermsOfUse(final HttpServletRequest request,
        final HttpServletResponse response) {

        if (!touModule.isEnabled()) {
            return false;
        }

        if (IdPHelper.isToUAccepted(servletContext, request)) {
            return false;
        }

        final String principalName =
            IdPHelper.getPrincipalName(servletContext, request);
        final String relyingPartyId =
            IdPHelper.getRelyingPartyId(servletContext, request);
        if (touModule.requiresToUAcceptance(principalName, relyingPartyId)) {

            if (IdPHelper.isPassiveRequest(servletContext, request)) {
                IdPHelper.setAuthenticationFailure(servletContext, request,
                    new PassiveAuthenticationException());
                return false;
            }

            IdPHelper.redirectToServlet(servletContext, request, response,
                "/PrivacyLens/TermsOfUse");
            return true;
        }

        return false;
    }

    /**
     * Handle Attribute Release.
     *
     * @param request The request.
     * @param response The response.
     * @return Returns true if the attribute release were handled.
     */
    private boolean handleAttributeRelease(final HttpServletRequest request,
        final HttpServletResponse response) {

        if (!attributeReleaseModule.isEnabled()) {
            return false;
        }

        // has this part already been called in a previous interception?
        if (IdPHelper.isAttributeReleaseConsented(servletContext, request)) {
            return false;
        }

        final String principalName =
            IdPHelper.getPrincipalName(servletContext, request);
        final String relyingPartyId =
            IdPHelper.getRelyingPartyId(servletContext, request);

        // does the user want to see the administrative interface?
        final String adminUrl = General.getInstance().getAdminUrl();
        if (adminUrl.equals(relyingPartyId)) {
            // XXXstroucki providing attributes to the servlet the same way as for the main attribute release servlet. After review, they could be made common if both servlets really need them.
            final List<Attribute> attributes =
                samlHelper.resolveAttributes(principalName, relyingPartyId,
                    viewHelper.selectLocale(request),
                    IdPHelper.getSession(request));

            // set up the context
            // XXXstroucki i see we use different ways of setting attributes here.
            IdPHelper.setAttributes(servletContext, request, attributes);

            // don't carry in a view attribute
            // XXXstroucki maybe we should set one?
            request.getSession().removeAttribute("view");
            IdPHelper.redirectToServlet(servletContext, request, response,
                "/PrivacyLens/AdminServlet");
            return true;
        }

        final List<Attribute> attributes =
            samlHelper
                .resolveAttributes(principalName, relyingPartyId,
                    viewHelper.selectLocale(request),
                    IdPHelper.getSession(request));

        if (IdPHelper.isConsentRevocationRequested(servletContext, request)) {
            logger.debug("Consent revocation requested. Clear consent.");
            attributeReleaseModule.clearChoice(principalName, relyingPartyId);
        }

        // XXXstroucki https://wiki.shibboleth.net/confluence/display/SHIB2/isPassive
        // but I guess we'll just error anyway
        if (IdPHelper.isPassiveRequest(servletContext, request)) {
            IdPHelper.setAuthenticationFailure(servletContext, request,
                new PassiveAuthenticationException());
            return false;
        }

        // XXXstroucki handling reminder / no reminder in ar/SourceAction, but
        // SP whitelisting still happens here
        if (attributeReleaseModule.checkSPWhitelisted(principalName,
            relyingPartyId)) {
            return false;
        }

        // set up the context
        // XXXstroucki i see we use different ways of setting attributes here.
        IdPHelper.setAttributes(servletContext, request, attributes);

        // don't carry in a view attribute
        // XXXstroucki maybe we should set one?
        request.getSession().removeAttribute("view");
        IdPHelper.redirectToServlet(servletContext, request, response,
            "/PrivacyLens/AttributeRelease");
        return true;

    }

    /** {@inheritDoc} */
    public void destroy() {
        // we seem to have leakage issues with shibboleth:
        // WARNING: The web application [/idp] appears to have started a thread named [Resource Destroyer in BasicResourcePool.close()] but has failed to stop it. This is very likely to create a memory leak.
        logger.debug("PrivacyLens destroyed.");
    }

}
