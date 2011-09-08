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
import java.io.Serializable;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.SWITCH.aai.uApprove.ar.Attribute;
import edu.internet2.middleware.shibboleth.common.session.Session;
import edu.internet2.middleware.shibboleth.idp.authn.LoginContext;
import edu.internet2.middleware.shibboleth.idp.util.HttpServletHelper;

/**
 * Provides helper functions to retrieve information about the login from the IdP.
 */
public final class LoginHelper {

    /** Class logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginHelper.class);

    /** Default constructor for utility classes is private. */
    private LoginHelper() {
    }

    /**
     * Gets the login context.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     * @param required Indicates whether the login context is required or not.
     * @return Returns the login context.
     */
    private static LoginContext getLoginContext(final ServletContext servletContext, final HttpServletRequest request,
            final boolean required) {
        final LoginContext loginContext =
                HttpServletHelper.getLoginContext(HttpServletHelper.getStorageService(servletContext), servletContext,
                        request);
        if (required) {
            Validate.notNull(loginContext, "Login context is required for this operation.");
        }
        return loginContext;
    }

    /**
     * Gets the session.
     * 
     * @param request The HTTP request.
     * @return Returns the session.
     */
    public static Session getSession(final HttpServletRequest request) {
        return HttpServletHelper.getUserSession(request);
    }

    /**
     * Indicates whether the user is authenticated or not.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     * @return Returns true if the user is authenticated.
     */
    public static boolean isAuthenticated(final ServletContext servletContext, final HttpServletRequest request,
            final HttpServletResponse response) {
        final LoginContext loginContext = getLoginContext(servletContext, request, false);

        if (loginContext == null) {
            LOGGER.trace("LoginContext is null.");
            return false;
        }

        if (loginContext.getAuthenticationFailure() != null) {
            LOGGER.trace("LoginContext contains authentication failure.");
            return false;
        }

        if (request.getParameterMap().containsKey("SAMLRequest")) {
            LOGGER.trace("Unbind login context.");
            HttpServletHelper.unbindLoginContext(HttpServletHelper.getStorageService(servletContext), servletContext,
                    request, response);
            return false;
        }

        final boolean isPrincipalAuthenticated = loginContext.isPrincipalAuthenticated();
        LOGGER.trace("Principal is {}autenticated.", isPrincipalAuthenticated ? "" : "not ");

        return isPrincipalAuthenticated;
    }

    /**
     * Gets the principal Name.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     * @return Returns the principal Name.
     */
    public static String getPrincipalName(final ServletContext servletContext, final HttpServletRequest request) {
        final String principalName = getLoginContext(servletContext, request, true).getPrincipalName();
        LOGGER.trace("Principal name is {}.", principalName);
        return principalName;
    }

    /**
     * Gets the relying party id.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     * @return Returns the relying party id.
     */
    public static String getRelyingPartyId(final ServletContext servletContext, final HttpServletRequest request) {
        final String relyingPartyId = getLoginContext(servletContext, request, true).getRelyingPartyId();
        LOGGER.trace("Relying party id is {}.", relyingPartyId);
        return relyingPartyId;
    }

    /**
     * Return to the IdP.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     * @param response The HTTP response.
     */
    public static void returnToIdP(final ServletContext servletContext, final HttpServletRequest request,
            final HttpServletResponse response) {
        final LoginContext loginContext = getLoginContext(servletContext, request, true);
        final String profileUrl =
                HttpServletHelper.getContextRelativeUrl(request, loginContext.getProfileHandlerURL()).buildURL();
        try {
            LOGGER.trace("Redirect to {}.", profileUrl);
            response.sendRedirect(profileUrl);
        } catch (final IOException e) {
            throw new UApproveException("Error sending user back to profile handler at " + profileUrl, e);
        }
    }

    /**
     * Redirects to a servlet.
     * 
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @param servletPath The servlet path.
     */
    public static void redirectToServlet(final HttpServletRequest request, final HttpServletResponse response,
            final String servletPath) {
        final String servletUrl = HttpServletHelper.getContextRelativeUrl(request, servletPath).buildURL();
        try {
            LOGGER.trace("Redirect to {}.", servletUrl);
            response.sendRedirect(servletUrl);
        } catch (final IOException e) {
            throw new UApproveException("Error sending user to servlet " + servletUrl, e);
        }
    }

    /**
     * Test if consent revocation was requested and sets it.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     */
    public static void
            testAndSetConsentRevocation(final ServletContext servletContext, final HttpServletRequest request) {
        final boolean consentRevocation = BooleanUtils.toBoolean(request.getParameter("uApprove.consent-revocation"));
        if (consentRevocation) {
            LOGGER.trace("Consent revocation is set.");
            final LoginContext loginContext = getLoginContext(servletContext, request, true);
            loginContext.setProperty("uApprove.consent-revocation", consentRevocation);
        }
    }

    /**
     * Indicates whether consent revocation was requested or not.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     * @return Returns true if consent revocation was requested.
     */
    public static boolean isConsentRevocation(final ServletContext servletContext, final HttpServletRequest request) {
        final LoginContext loginContext = getLoginContext(servletContext, request, true);
        final Object consentRevocation = loginContext.getProperty("uApprove.consent-revocation");
        if (consentRevocation instanceof Boolean) {
            return (Boolean) consentRevocation;
        } else {
            return false;
        }
    }

    /**
     * Clears the consent revocation request.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     */
    public static void clearConsentRevocation(final ServletContext servletContext, final HttpServletRequest request) {
        final LoginContext loginContext = getLoginContext(servletContext, request, true);
        loginContext.setProperty("uApprove.consent-revocation", false);
    }

    /**
     * Sets the attributes.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     * @param attributes The attributes.
     */
    public static void setAttributes(final ServletContext servletContext, final HttpServletRequest request,
            final List<Attribute> attributes) {
        final LoginContext loginContext = getLoginContext(servletContext, request, true);
        loginContext.setProperty("uApprove.attributes", (Serializable) attributes);
    }

    /**
     * Gets the attributes.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     * @return Returns the attributes.
     */
    public static List<Attribute> getAttributes(final ServletContext servletContext, final HttpServletRequest request) {
        final LoginContext loginContext = getLoginContext(servletContext, request, true);
        @SuppressWarnings("unchecked") final List<Attribute> attributes =
                (List<Attribute>) loginContext.getProperty("uApprove.attributes");
        return attributes;
    }
}
