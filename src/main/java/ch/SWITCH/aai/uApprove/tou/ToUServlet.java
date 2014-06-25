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

package ch.SWITCH.aai.uApprove.tou;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import edu.cmu.ece.PrivacyLens.IdPHelper;
import edu.cmu.ece.PrivacyLens.ViewHelper;

/**
 * ToU Servlet.
 */
public class ToUServlet extends HttpServlet {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(ToUServlet.class);

    /** The servlet context. */
    private ServletContext servletContext;

    /** The Terms Of Use Module. */
    private ToUModule touModule;

    /** The view helper. */
    private ViewHelper viewHelper;

    /** {@inheritDoc} */
    public void init() throws ServletException {
        try {
            super.init();
            servletContext = getServletContext();
            final WebApplicationContext appContext =
                    WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
            touModule = (ToUModule) appContext.getBean("uApprove.touModule", ToUModule.class);
            viewHelper = (ViewHelper) appContext.getBean("PrivacyLens.viewHelper", ViewHelper.class);
        } catch (final Throwable t) {
            logger.error("Error while initializing Terms of Use Servlet.", t);
            throw new ServletException(t);
        }
    }

    /** {@inheritDoc} */
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException {
        try {
            final Map<String, Object> context = new HashMap<String, Object>();
            context.put("tou", touModule.getTou());
            viewHelper.showView(servletContext, req, resp, "terms-of-use", context);
        } catch (final Throwable t) {
            logger.error("Error while GET Terms of Use Servlet.", t);
            IdPHelper.handleException(servletContext, req, resp, t);
        }
    }

    /** {@inheritDoc} */
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException {
        try {
            if (BooleanUtils.toBoolean(req.getParameter("accept"))) {
                final String principalName = IdPHelper.getPrincipalName(servletContext, req);
                touModule.acceptToU(principalName);
                IdPHelper.setToUAccepted(servletContext, req);
            }
            IdPHelper.returnToIdP(servletContext, req, resp);
        } catch (final Throwable t) {
            logger.error("Error while POST Terms of Use Servlet.", t);
            IdPHelper.handleException(servletContext, req, resp, t);
        }
    }

}
