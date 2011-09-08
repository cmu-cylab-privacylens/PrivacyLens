/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.SWITCH.aai.uApprove;

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

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ch.SWITCH.aai.uApprove.ar.Attribute;
import ch.SWITCH.aai.uApprove.ar.AttributeReleaseModule;
import ch.SWITCH.aai.uApprove.ar.SAMLHelper;
import ch.SWITCH.aai.uApprove.tou.ToUModule;

/**
 * uApprove request intercepter.
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

    /** {@inheritDoc} */
    public void init(final FilterConfig filterConfig) throws ServletException {
        servletContext = filterConfig.getServletContext();
        final WebApplicationContext appContext =
                WebApplicationContextUtils.getRequiredWebApplicationContext(filterConfig.getServletContext());
        touModule = (ToUModule) appContext.getBean("uApprove.touModule", ToUModule.class);
        attributeReleaseModule =
                (AttributeReleaseModule) appContext.getBean("uApprove.attributeReleaseModule",
                        AttributeReleaseModule.class);
        samlHelper = (SAMLHelper) appContext.getBean("uApprove.samlHelper", SAMLHelper.class);

        viewHelper = (ViewHelper) appContext.getBean("uApprove.viewHelper", ViewHelper.class);

        Validate.notNull(touModule, "ToU module isn't properly configured.");
        Validate.notNull(attributeReleaseModule, "Attribute Release module isn't properly configured.");
        Validate.notNull(samlHelper, "SAML Helper isn't properly configured.");
        Validate.notNull(viewHelper, "View Helper isn't properly configured.");

        logger.debug("uApprove initialized.");
    }

    /** {@inheritDoc} */
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        Validate.isTrue(request instanceof HttpServletRequest, "Not an HttpServletRequest.");
        Validate.isTrue(response instanceof HttpServletResponse, "Not an HttpServletResponse.");
        intercept((HttpServletRequest) request, (HttpServletResponse) response, chain);
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
    public void
            intercept(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
                    throws IOException, ServletException {

        if (!LoginHelper.isAuthenticated(servletContext, request, response)) {
            logger.trace("Request is not authenticated.");
            LoginHelper.testAndSetConsentRevocation(servletContext, request);
            chain.doFilter(request, response);
            return;
        }

        final String principalName = LoginHelper.getPrincipalName(servletContext, request);
        final String relyingPartyId = LoginHelper.getRelyingPartyId(servletContext, request);

        if (touModule.isEnabled() && touModule.requiresToUAcceptance(principalName, relyingPartyId)) {
            LoginHelper.redirectToServlet(request, response, "/uApprove/TermsOfUse");
            return;
        } else {
            if (attributeReleaseModule.isEnabled()) {

                if (LoginHelper.isConsentRevocation(servletContext, request)) {
                    logger.debug("Consent revovation requested. Clear consent.");
                    attributeReleaseModule.clearConsent(principalName, relyingPartyId);
                    LoginHelper.clearConsentRevocation(servletContext, request);
                }

                logger.debug("Resolving attributes for user {} and relying party {}.", principalName, relyingPartyId);
                final List<Attribute> attributes =
                        samlHelper.resolveAttributes(principalName, relyingPartyId, viewHelper.selectLocale(request),
                                LoginHelper.getSession(request));

                if (attributeReleaseModule.requiresConsent(principalName, relyingPartyId, attributes)) {
                    LoginHelper.setAttributes(servletContext, request, attributes);
                    LoginHelper.redirectToServlet(request, response, "/uApprove/AttributeRelease");
                    return;
                } else {
                    chain.doFilter(request, response);
                    return;
                }
            } else {
                chain.doFilter(request, response);
                return;
            }
        }

    }

    /** {@inheritDoc} */
    public void destroy() {
        logger.debug("uApprove destroyed.");
    }

}
