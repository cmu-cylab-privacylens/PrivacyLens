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

package edu.cmu.ece.PrivacyLens;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ch.SWITCH.aai.uApprove.IdPHelper;
import ch.SWITCH.aai.uApprove.ViewHelper;
import ch.SWITCH.aai.uApprove.ar.Attribute;
import ch.SWITCH.aai.uApprove.ar.AttributeReleaseModule;
import ch.SWITCH.aai.uApprove.ar.RelyingParty;
import ch.SWITCH.aai.uApprove.ar.SAMLHelper;

/**
 * Debug view Servlet. I used this to test the layout, but haven't looked at it in a while.
 */
@Deprecated
public class DebugViewServlet extends HttpServlet {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(DebugViewServlet.class);

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
                    (AttributeReleaseModule) appContext.getBean("uApprove.attributeReleaseModule",
                            AttributeReleaseModule.class);
            samlHelper = (SAMLHelper) appContext.getBean("uApprove.samlHelper", SAMLHelper.class);

            viewHelper = (ViewHelper) appContext.getBean("uApprove.viewHelper", ViewHelper.class);
        } catch (final Throwable t) {
            logger.error("Error while initializing Debug View Servlet.", t);
            throw new ServletException(t);
        }
    }

    private List<Attribute> genFakeAttributes() {
        final List<Attribute> attributes = new ArrayList<Attribute>();
        List<String> valuesSample;
        final int nAttributes = StrouckiUtils.getRandomRange(1, 5);
        for (int attribute = 0; attribute < nAttributes; attribute++) {
            valuesSample = new ArrayList<String>();

            final int nValues = StrouckiUtils.getRandomRange(1, 3);
            for (int value = 0; value < nValues; value++) {
                valuesSample.add("Attribute#" + attribute + "V#" + value);
            }

            final boolean required = StrouckiUtils.getRandomBoolean();
            attributes.add(new Attribute("AttributeID#" + attribute, "AttributeName#" + attribute,
                    "AttributeDescription#" + attribute, valuesSample, required));
        }

        return attributes;
    }

    /** {@inheritDoc} */
    // Appears to be set up for initial viewing of this state
    protected void doShowAttributes(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            logger.trace("entered doShowAttributes");
            // attribute: id, name, description, values[]
            final List<Attribute> attributes = genFakeAttributes();
            final Map<String, Object> context = new HashMap<String, Object>();
            final RelyingParty rpSample = new RelyingParty("RPId", "RPName", "RPDescription");
            context.put("relyingParty", rpSample);
            context.put("attributes", attributes);
            context.put("allowGeneralConsent", attributeReleaseModule.isAllowGeneralConsent());
            // no such view anymore
            //viewHelper.showView(servletContext, req, resp, "attribute-release", context);
        } catch (final Throwable t) {
            logger.error("Error while showing attributes.", t);
            IdPHelper.handleException(servletContext, req, resp, t);
        }
    }

    protected void doShowAttributeDetail(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            logger.trace("entered doShowAttributeDetail");
            // attribute: id, name, description, values[]
            final List<Attribute> attributes = genFakeAttributes();
            final Map<String, Object> context = new HashMap<String, Object>();

            final RelyingParty rpSample = new RelyingParty("RPId", "RPName", "RPDescription");
            context.put("relyingParty", rpSample);
            context.put("attributes", attributes);
            context.put("requirementStatement", "These parameters are needed to identify which resource to access. "
                    + "Your information will only be kept for that purpose.");
            viewHelper.showView(servletContext, req, resp, "attribute-detail", context);
        } catch (final Throwable t) {
            logger.error("Error while showing attribute detail.", t);
            IdPHelper.handleException(servletContext, req, resp, t);
        }
    }

    protected void doShowDenyRequired(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            logger.trace("entered doShowDenyRequired");
            // attribute: id, name, description, values[]
            final List<Attribute> attributes = genFakeAttributes();
            final Map<String, Object> context = new HashMap<String, Object>();

            final RelyingParty rpSample = new RelyingParty("RPId", "RPName", "RPDescription");
            context.put("relyingParty", rpSample);
            context.put("attributes", attributes);
            // no such view anymore
            //viewHelper.showView(servletContext, req, resp, "deny-required", context);
        } catch (final Throwable t) {
            logger.error("Error while showing deny required.", t);
            IdPHelper.handleException(servletContext, req, resp, t);
        }
    }

    protected void doShowAttributeConfirm(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            logger.trace("entered doShowAttributeConfirm");
            // attribute: id, name, description, values[]
            final List<Attribute> attributes = genFakeAttributes();
            final Map<String, Object> context = new HashMap<String, Object>();

            final RelyingParty rpSample = new RelyingParty("RPId", "RPName", "RPDescription");
            context.put("relyingParty", rpSample);
            context.put("attributes", attributes);
            viewHelper.showView(servletContext, req, resp, "attribute-confirm", context);
        } catch (final Throwable t) {
            logger.error("Error while showing attribute confirm.", t);
            IdPHelper.handleException(servletContext, req, resp, t);
        }
    }

    protected void doShowMenu(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException {
        // ShowAttributes, ShowAttributeDetail, ShowDenyRequired, ShowAttributeConfirm
        logger.trace("entered doShowMenu");
        final java.io.PrintWriter out = resp.getWriter();
        out.println("<html><head><title>View test page</title><body>");
        out.println(edu.cmu.ece.PrivacyLens.StrouckiUtils.request2string(req).replace('<', '[').replace('>', ']') + "<br/>");

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
                doShowMenu(req, resp);
            } else {
                // show the desired view
                if (state.equals("ShowAttributes")) {
                    doShowAttributes(req, resp);
                }

                else if (state.equals("ShowAttributeDetail")) {
                    doShowAttributeDetail(req, resp);
                }

                else if (state.equals("ShowDenyRequired")) {
                    doShowDenyRequired(req, resp);
                }

                else if (state.equals("ShowAttributeConfirm")) {
                    doShowAttributeConfirm(req, resp);
                }

                else {
                    // show menu of views
                    doShowMenu(req, resp);
                }

            }

        } catch (final Throwable t) {
            logger.error("Error while POST Attribute Release Servlet.", t);
        }
    }
}
