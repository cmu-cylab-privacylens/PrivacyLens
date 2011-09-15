/*
 * Copyright (c) 2011, SWITCH
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ch.SWITCH.aai.uApprove.LoginHelper;
import ch.SWITCH.aai.uApprove.ViewHelper;

/**
 * Attribute Release Servlet.
 */
public class AttributeReleaseServlet extends HttpServlet {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(AttributeReleaseServlet.class);

    /** The Attribute Release module. */
    private AttributeReleaseModule attributeReleaseModule;

    /** The SAML helper. */
    private SAMLHelper samlHelper;

    /** The view helper. */
    private ViewHelper viewHelper;

    /** {@inheritDoc} */
    public void init() throws ServletException {
        super.init();
        final WebApplicationContext appContext =
                WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        attributeReleaseModule =
                (AttributeReleaseModule) appContext.getBean("uApprove.attributeReleaseModule",
                        AttributeReleaseModule.class);
        samlHelper = (SAMLHelper) appContext.getBean("uApprove.samlHelper", SAMLHelper.class);
        viewHelper = (ViewHelper) appContext.getBean("uApprove.viewHelper", ViewHelper.class);
    }

    /** {@inheritDoc} */
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException {
        final String relyingPartyId = LoginHelper.getRelyingPartyId(getServletContext(), req);
        final List<Attribute> attributes = LoginHelper.getAttributes(getServletContext(), req);
        final Map<String, Object> context = new HashMap<String, Object>();
        context.put("relyingParty", samlHelper.readRelyingParty(relyingPartyId, viewHelper.selectLocale(req)));
        context.put("attributes", attributes);
        context.put("allowGeneralConsent", attributeReleaseModule.isAllowGeneralConsent());
        viewHelper.showView(req, resp, "attribute-release", context);
    }

    /** {@inheritDoc} */
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException {

        final boolean generalConsent =
                attributeReleaseModule.isAllowGeneralConsent()
                        && BooleanUtils.toBoolean(req.getParameter("generalConsent"));
        final String principalName = LoginHelper.getPrincipalName(getServletContext(), req);
        final String relyingPartyId = LoginHelper.getRelyingPartyId(getServletContext(), req);
        final List<Attribute> attributes = LoginHelper.getAttributes(getServletContext(), req);

        if (generalConsent) {
            logger.debug("Create general consent for {}", principalName);
            attributeReleaseModule.consentGeneralAttributeRelease(principalName);
        } else {
            logger.debug("Create consent for user {} to {}.", principalName, relyingPartyId);
            attributeReleaseModule.consentAttributeRelease(principalName, relyingPartyId, attributes);
        }

        LoginHelper.returnToIdP(getServletContext(), req, resp);
    }
}
