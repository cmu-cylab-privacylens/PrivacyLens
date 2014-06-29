/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2013 Carnegie Mellon University
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
import edu.cmu.ece.PrivacyLens.ViewHelper;
import edu.cmu.ece.PrivacyLens.config.General;

/**
 * Administrative (Settings) servlet.
 */
public class AdminServlet extends HttpServlet {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(AdminServlet.class);

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

    protected void doShowMenu(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException {
        // ShowAttributes, ShowAttributeDetail, ShowDenyRequired, ShowAttributeConfirm
        logger.trace("entered doShowMenu");
        final java.io.PrintWriter out = resp.getWriter();
        out.println("<html><head><title>View test page</title><body>");
        out.println(edu.cmu.ece.PrivacyLens.Util.request2string(req).replace('<', '[').replace('>', ']') + "<br/>");

        out.println("<form action=\"DebugView\" method=\"POST\">");
        out.println("<input type=\"radio\" name=\"state\" value=\"ShowAttributes\"> Show attributes</input><br/>");
        out.println("<input type=\"radio\" name=\"state\" value=\"ShowAttributeDetail\"> Show attribute detail</input><br/>");
        out.println("<input type=\"radio\" name=\"state\" value=\"ShowDenyRequired\"> Show deny required</input><br/>");
        out.println("<input type=\"radio\" name=\"state\" value=\"ShowAttributeConfirm\"> Show attribute confirmation</input><br/>");
        out.println("<input type=\"radio\" name=\"state\" value=\"foo\"> Return here</input><br/>");

        out.println("<input type=\"submit\" name=\"submit1\">");
        out.println("<input type=\"submit\" name=\"submit2\">");

        out.println("</form>");

        out.println("</body></html>");
    }

    protected void doShowMain(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        try {
            logger.trace("entered doShowMain");
            final Action action = AdminActionFactory.getAction(request);
            final String view = action.execute(request, response);
            logger.trace("Business logic completed, next view: {}", view);

            // save our destination view in the session so that ActionFactory can tell where we came from
            final HttpSession session = request.getSession(false);
            if (session != null) {
                session.setAttribute("view", view);
            }

            final Map<String, Object> context = new HashMap<String, Object>();
            context.put("idpOrganization", General.getInstance().getOrganizationName());

            //final RelyingParty rpSample = new RelyingParty("RPId", "RPName", "RPDescription");
            //context.put("relyingParty", rpSample);
            /*
            final java.io.PrintWriter out = resp.getWriter();
            out.println("<html><head><title>Admin test page</title><body>");
            out.println(edu.cmu.ece.ua.StrouckiUtils.request2string(req).replace('<', '[').replace('>', ']') + "<br/>");

            out.println("<p>text</p>");

            out.println("</body></html>");
            */
            // XXX debugging
            if (view.equals("XXX")) {
                IdPHelper.setAttributeReleaseConsented(servletContext, request);
                IdPHelper.returnToIdP(servletContext, request, response);
            } else if (view.equals("loginEvent")) {
                viewHelper.showView(servletContext, request, response, "attribute-loginevent", context);
                logger.trace("loginEvent view finished");
            } else if (view.equals("serviceLogin")) {
                viewHelper.showView(servletContext, request, response, "attribute-service", context);
                logger.trace("serviceLogin view finished");
            } else {
                if (!view.equals("entry")) {
                    logger.error("Fell through when setting up view {}", view);
                }
                // default and entry point
                //context.put("allowGeneralConsent", attributeReleaseModule.isAllowGeneralConsent());
                viewHelper.showView(servletContext, request, response, "attribute-admin", context);
                logger.trace("entry view finished");
                return;
            }

        } catch (final Throwable t) {
            logger.error("Error while showing attribute settings.", t);
            IdPHelper.handleException(servletContext, request, response, t);
        }
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException {
        doPost(req, resp);
    }

    /** {@inheritDoc} */
    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException {
        try {
            final String state = req.getParameter("state");
            if (state == null) {
                // show menu of views
                doShowMain(req, resp);
            } else {

                doShowMain(req, resp);

            }

        } catch (final Throwable t) {
            logger.error("Error while POST Attribute Release Servlet.", t);
        }
    }
}
