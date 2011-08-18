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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import edu.internet2.middleware.shibboleth.idp.authn.LoginContext;
import edu.internet2.middleware.shibboleth.idp.util.HttpServletHelper;

/**
 * Provides helper functions to retrieve information about the login from the IdP.
 */
public final class LoginHelper {

    /** Class logger. */
    private static final Logger logger = LoggerFactory.getLogger(LoginHelper.class);

    /** Default constructor for utility classes is private. */
    private LoginHelper() {
    }

    private static LoginContext getLoginContext(final ServletContext servletContext, final HttpServletRequest request) {
        final LoginContext loginContext =
                HttpServletHelper.getLoginContext(HttpServletHelper.getStorageService(servletContext), servletContext,
                        request);
        return loginContext;
    }

    public static boolean isAuthenticated(final ServletContext servletContext, final HttpServletRequest request) {
        final LoginContext loginContext = getLoginContext(servletContext, request);

        if (loginContext == null) {
            logger.trace("LoginContext is null.");
            return false;
        }

        if (loginContext.getAuthenticationFailure() != null) {
            logger.trace("LoginContext contains authentication failure.");
            return false;
        }

        final boolean isPrincipalAuthenticated = loginContext.isPrincipalAuthenticated();
        logger.trace("Principal autenticated is {}.", isPrincipalAuthenticated);
        return isPrincipalAuthenticated;
    }

    public static String getPrincipalName(final ServletContext servletContext, final HttpServletRequest request) {
        final LoginContext loginContext = getLoginContext(servletContext, request);
        Assert.notNull(loginContext, "LoginContext is null.");
        final String principalName = loginContext.getPrincipalName();
        logger.trace("Principal name is {}.", principalName);
        return principalName;
    }

    public static String getRelyingPartyId(final ServletContext servletContext, final HttpServletRequest request) {
        final LoginContext loginContext = getLoginContext(servletContext, request);
        Assert.notNull(loginContext, "LoginContext is null.");
        final String relyingPartyId = loginContext.getRelyingPartyId();
        logger.trace("Relying party id is {}.", relyingPartyId);
        return relyingPartyId;
    }

    public static void returnToIdP(final ServletContext servletContext, final HttpServletRequest request,
            final HttpServletResponse response) {
        final LoginContext loginContext = getLoginContext(servletContext, request);
        final String profileUrl =
                HttpServletHelper.getContextRelativeUrl(request, loginContext.getProfileHandlerURL()).buildURL();
        try {
            logger.trace("Redirect to {}.", profileUrl);
            response.sendRedirect(profileUrl);
        } catch (final IOException e) {
            logger.error("Error sending user back to profile handler at {}", profileUrl, e);
        }
    }

    public static void redirectToServlet(final HttpServletRequest request, final HttpServletResponse response,
            final String servletPath) {
        final String servletUrl = HttpServletHelper.getContextRelativeUrl(request, servletPath).buildURL();
        try {
            logger.trace("Redirect to {}.", servletUrl);
            response.sendRedirect(servletUrl);
        } catch (final IOException e) {
            logger.error("Error sending user to servlet {} ", servletUrl, e);
        }
    }
}
