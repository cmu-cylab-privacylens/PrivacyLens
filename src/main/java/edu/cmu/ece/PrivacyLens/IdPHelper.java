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

package edu.cmu.ece.PrivacyLens;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.Validate;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.ece.PrivacyLens.ar.Attribute;
import edu.cmu.ece.PrivacyLens.ar.AttributeReleaseModule;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.filtering.provider.ShibbolethFilteringContext;
import edu.internet2.middleware.shibboleth.common.log.AuditLogEntry;
import edu.internet2.middleware.shibboleth.common.profile.AbstractErrorHandler;
import edu.internet2.middleware.shibboleth.common.session.Session;
import edu.internet2.middleware.shibboleth.idp.authn.AuthenticationException;
import edu.internet2.middleware.shibboleth.idp.authn.LoginContext;
import edu.internet2.middleware.shibboleth.idp.util.HttpServletHelper;

/**
 * Provides helper functions to retrieve information from the IdP.
 */
public final class IdPHelper {

    /** Class logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(IdPHelper.class);

    public static AttributeReleaseModule attributeReleaseModule;

    /** Default constructor for utility classes is private. */
    private IdPHelper() {
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
        LOGGER.trace("[IdPHelper] getLoginContext");
        final LoginContext loginContext =
                HttpServletHelper.getLoginContext(HttpServletHelper.getStorageService(servletContext), servletContext,
                        request);
        if (required) {
            Validate.notNull(loginContext, "Login context is required for this operation.");
        }
        return loginContext;
    }

    /**
     * Gets the authentication context class reference.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     * @return Returns the authentication context class reference.
     */
    public static String getAuthContextClassRef(final ServletContext servletContext, final HttpServletRequest request) {
        LOGGER.trace("[IdPHelper] getAuthContextClassRef");
        final LoginContext loginContext = getLoginContext(servletContext, request, false);
        return loginContext != null ? loginContext.getAuthenticationMethod() : null;
    }

    /**
     * Gets the session.
     * 
     * @param request The HTTP request.
     * @return Returns the session.
     */
    public static Session getSession(final HttpServletRequest request) {
        LOGGER.trace("[IdPHelper] getSession");
        return HttpServletHelper.getUserSession(request);
    }

    /**
     * Indicates whether the user is authenticated or not.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @return Returns true if the user is authenticated.
     */
    public static boolean isAuthenticated(final ServletContext servletContext, final HttpServletRequest request,
            final HttpServletResponse response) {
        LOGGER.trace("[IdPHelper] isAuthenticated");
        final LoginContext loginContext = getLoginContext(servletContext, request, false);

        if (loginContext == null) {
            LOGGER.trace("LoginContext is null.");
            return false;
        }

        if (loginContext.getAuthenticationFailure() != null) {
            LOGGER.trace("LoginContext contains authentication failure.");
            return false;
        }

        if (request.getParameterMap().containsKey("SAMLRequest") || request.getParameterMap().containsKey("shire")) {
            LOGGER.trace("Unbind login context.");
            HttpServletHelper.unbindLoginContext(HttpServletHelper.getStorageService(servletContext), servletContext,
                    request, response);
            return false;
        }

        final boolean isPrincipalAuthenticated = loginContext.isPrincipalAuthenticated();
        LOGGER.trace("Principal is{}authenticated.", isPrincipalAuthenticated ? " " : " not ");

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
        LOGGER.trace("[IdPHelper] getPrincipalName");
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
        LOGGER.trace("[IdPHelper] getRelyingPartyId");
        final String relyingPartyId = getLoginContext(servletContext, request, true).getRelyingPartyId();
        LOGGER.trace("Relying party id is {}.", relyingPartyId);
        return relyingPartyId;
    }

    /**
     * Indicates whether the request is passive or not.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     * @return Returns whether the request is passive or not.
     */
    public static boolean isPassiveRequest(final ServletContext servletContext, final HttpServletRequest request) {
        LOGGER.trace("[IdPHelper] isPassiveRequest");
        final boolean isPassive = getLoginContext(servletContext, request, true).isPassiveAuthRequired();
        LOGGER.trace("Request is {} passive.", isPassive ? "" : "not");
        return isPassive;
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
        LOGGER.trace("[IdPHelper] returnToIdP");
        final LoginContext loginContext = getLoginContext(servletContext, request, true);
        final String profileUrl =
                HttpServletHelper.getContextRelativeUrl(request, loginContext.getProfileHandlerURL()).buildURL();
        try {
            LOGGER.trace("Redirect to {}.", profileUrl);
            response.sendRedirect(profileUrl);
        } catch (final IOException e) {
            LOGGER.error("Error redirecting to profile handler at {}.", profileUrl, e);
            handleException(servletContext, request, response, e);
        }
    }

    /**
     * Redirects to a url.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @param servletPath The servlet path.
     */
    public static void redirectToUrl(final ServletContext servletContext, final HttpServletRequest request,
            final HttpServletResponse response, final String servletPath) {
        LOGGER.trace("[IdPHelper] redirectToUrl");
        final String servletUrl = servletPath;
        try {
            LOGGER.trace("Redirect to {}.", servletUrl);
            response.sendRedirect(servletUrl);
        } catch (final IOException e) {
            LOGGER.error("Error redirecting to servlet at {}.", servletUrl, e);
            handleException(servletContext, request, response, e);
        }

    }

    /**
     * Redirects to a servlet.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @param servletPath The servlet path.
     */
    public static void redirectToServlet(final ServletContext servletContext, final HttpServletRequest request,
            final HttpServletResponse response, final String servletPath) {
        LOGGER.trace("[IdPHelper] redirectToServlet");
        final String servletUrl = HttpServletHelper.getContextRelativeUrl(request, servletPath).buildURL();
        try {
            LOGGER.trace("Redirect to {}.", servletUrl);
            response.sendRedirect(servletUrl);
        } catch (final IOException e) {
            LOGGER.error("Error redirecting to servlet at {}.", servletUrl, e);
            handleException(servletContext, request, response, e);
        }

    }

    /**
     * Test if consent revocation was requested and sets it.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     */
    public static void setConsentRevocationRequested(final ServletContext servletContext,
            final HttpServletRequest request) {
        LOGGER.trace("[IdPHelper] setConsentRevocationRequested");
        final boolean consentRevocation = BooleanUtils.toBoolean(request.getParameter("uApprove.consent-revocation"));
        LOGGER.trace("Consent revocation is {} set.", consentRevocation ? "" : "not");
        if (consentRevocation) {
            final LoginContext loginContext = getLoginContext(servletContext, request, true);
            loginContext.setProperty("uApprove.consentRevocationRequested", consentRevocation);
        }
    }

    /**
     * Indicates whether consent revocation was requested or not.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     * @return Returns true if consent revocation was requested.
     */
    public static boolean isConsentRevocationRequested(final ServletContext servletContext,
            final HttpServletRequest request) {
        LOGGER.trace("[IdPHelper] isConsentRevocationRequested");
        final LoginContext loginContext = getLoginContext(servletContext, request, true);
        final Object consentRevocation = loginContext.getProperty("uApprove.consentRevocationRequested");
        if (consentRevocation instanceof Boolean) {
            return (Boolean) consentRevocation;
        } else {
            return false;
        }
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
        LOGGER.trace("[IdPHelper] setAttributes");
        final LoginContext loginContext = getLoginContext(servletContext, request, true);
        loginContext.setProperty("PrivacyLens.attributes", (Serializable) attributes);
    }

    /**
     * Gets the attributes.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     * @return Returns the attributes.
     */
    public static List<Attribute> getAttributes(final ServletContext servletContext, final HttpServletRequest request) {
        LOGGER.trace("[IdPHelper] getAttributes");
        final LoginContext loginContext = getLoginContext(servletContext, request, true);
        @SuppressWarnings("unchecked") final List<Attribute> attributes =
                (List<Attribute>) loginContext.getProperty("PrivacyLens.attributes");
        return attributes;
    }

    /**
     * Sets ToU accepted.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     */
    public static void setToUAccepted(final ServletContext servletContext, final HttpServletRequest request) {
        LOGGER.trace("[IdPHelper] setToUAccepted");
        final LoginContext loginContext = getLoginContext(servletContext, request, true);
        loginContext.setProperty("uApprove.touAccepted", true);
    }

    /**
     * Gets ToU accepted.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     * @return Returns true if ToU is accepted.
     */
    public static boolean isToUAccepted(final ServletContext servletContext, final HttpServletRequest request) {
        LOGGER.trace("[IdPHelper] isToUAccepted");
        final LoginContext loginContext = getLoginContext(servletContext, request, true);
        final Object touAccepted = loginContext.getProperty("uApprove.touAccepted");
        if (touAccepted instanceof Boolean) {
            return (Boolean) touAccepted;
        } else {
            return false;
        }
    }

    /**
     * Sets attribute release consented. It doesn't look like having any effect on IdP XXXstroucki but it has an effect
     * on Intercepter/handleAttributeRelease
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     */
    public static void setAttributeReleaseConsented(final ServletContext servletContext,
            final HttpServletRequest request) {
        LOGGER.trace("[IdPHelper] setAttributeReleaseConsented");
        final LoginContext loginContext = getLoginContext(servletContext, request, true);
        loginContext.setProperty("PrivacyLens.attributeReleaseConsented", true);
    }

    /**
     * Gets attribute release consented.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     * @return Returns true if attribute release is consented.
     */
    public static boolean isAttributeReleaseConsented(final ServletContext servletContext,
            final HttpServletRequest request) {
        LOGGER.trace("[IdPHelper] isAttributeReleaseConsented");
        final LoginContext loginContext = getLoginContext(servletContext, request, true);
        final Object attributeReleaseConsented = loginContext.getProperty("PrivacyLens.attributeReleaseConsented");
        if (attributeReleaseConsented instanceof Boolean) {
            return (Boolean) attributeReleaseConsented;
        } else {
            return false;
        }
    }

    /**
     * Handles exception by using the IdPs error handler.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @param t The exception.
     */
    public static void handleException(final ServletContext servletContext, final HttpServletRequest request,
            final HttpServletResponse response, final Throwable t) {
        LOGGER.trace("[IdPHelper] handleException");
        request.setAttribute(AbstractErrorHandler.ERROR_KEY, t);
        HttpServletHelper
                .getProfileHandlerManager(servletContext)
                .getErrorHandler()
                .processRequest(new HttpServletRequestAdapter(request),
                        new HttpServletResponseAdapter(response, request.isSecure()));
    }

    /**
     * Sets an authentication failure.
     * 
     * @param servletContext The servlet context.
     * @param request The HTTP request.
     * @param e The authentication exception.
     */
    public static void setAuthenticationFailure(final ServletContext servletContext, final HttpServletRequest request,
            final AuthenticationException e) {
        LOGGER.trace("[IdPHelper] setAuthenticationFailure");
        getLoginContext(servletContext, request, true).setAuthenticationFailure(e);
    }

    /**
     * Writes an audit log entry using the IdPs facilities.
     * 
     * @param event The event to log.
     * @param principalName The principal name to log.
     * @param relyingPartyId The relying party id to log.
     * @param data The data to log.
     */
    public static void writeAuditLog(final String event, final String principalName, final String relyingPartyId,
            final List<String> data) {
        LOGGER.trace("[IdPHelper] writeAuditLog");

        final AuditLogEntry auditLogEntry = new AuditLogEntry();
        auditLogEntry.setRequestBinding("edu.cmu.ece.PrivacyLens");
        auditLogEntry.setMessageProfile(event);
        auditLogEntry.setPrincipalName(principalName);
        auditLogEntry.setRelyingPartyId(relyingPartyId);
        auditLogEntry.getReleasedAttributes().addAll(data);

        final Logger logger = LoggerFactory.getLogger(AuditLogEntry.AUDIT_LOGGER_NAME);
        logger.info(auditLogEntry.toString());
    }

    public static boolean releaseFilter(final ShibbolethFilteringContext filterContext, final String attributeId,
            final String attributeValue) {
        // This is the part that does the actual filtering right now.
        LOGGER.trace("releaseFilter attributeId: {} attributeValue: {}", attributeId, attributeValue);
        // edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute foo =
        // filterContext.getUnfilteredAttributes().get(attributeId);
        final Session session = filterContext.getAttributeRequestContext().getUserSession();
        final java.util.Map<String, BaseAttribute> bla = filterContext.getUnfilteredAttributes();
        for (final java.util.Map.Entry<String, BaseAttribute> item : bla.entrySet()) {
            LOGGER.trace("attribute " + item.getKey() + " value " + item.getValue().getValues());

        }
        String principal = null;
        String rpid = null;
        try {
            principal = filterContext.getAttributeRequestContext().getPrincipalName();
            LOGGER.trace("principal is " + principal);
            // http://shibboleth.net/pipermail/users/2011-September/000903.html
            rpid = filterContext.getAttributeRequestContext().getPeerEntityId();
            // rpid = filterContext.getAttributeRequestContext().getRelyingPartyConfiguration().
            LOGGER.trace("rpid is " + rpid);
        } catch (final Exception e) {
            LOGGER.error("releaseFilter caught {}", e);
        }

        if (attributeReleaseModule == null) {
            LOGGER.error("attributeReleaseModule is null");
            return false;
        }

        final Attribute uaAttribute = new Attribute(attributeId, Arrays.asList(attributeValue));
        final List<Attribute> attributeList = Arrays.asList(uaAttribute);
        final boolean consentRequired = attributeReleaseModule.requiresConsent(principal, rpid, attributeList);

        LOGGER.trace("releaseFilter attributeID: {} consentRequired: {}", attributeId, consentRequired);
        return !consentRequired;
        /*
         * filterContext.getServletContext().; ServletConfig servletConfig = new ServletConfig();
         * 
         * ServletContext servletContext; servletContext = javax.servlet.ServletConfig.getServletContext(); final
         * WebApplicationContext appContext =
         * WebApplicationContextUtils.getRequiredWebApplicationContext(filterConfig.getServletContext());
         * AttributeReleaseModule attributeReleaseModule; attributeReleaseModule = (AttributeReleaseModule)
         * org.springframework.web.context.WebApplicationContext.getBean("PrivacyLens.attributeReleaseModule",
         * AttributeReleaseModule.class);
         * 
         * return false;
         */

    }
}
