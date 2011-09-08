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

package ch.SWITCH.aai.uApprove.tou;

import java.io.IOException;
import java.util.HashMap;
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
 * ToU Servlet.
 */
public class ToUServlet extends HttpServlet {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Class logger. */
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(ToUServlet.class);

    /** The Terms Of Use Module. */
    private ToUModule touModule;

    /** The view helper. */
    private ViewHelper viewHelper;

    /** {@inheritDoc} */
    public void init() throws ServletException {
        super.init();
        final WebApplicationContext appContext =
                WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        touModule = (ToUModule) appContext.getBean("uApprove.touModule", ToUModule.class);
        viewHelper = (ViewHelper) appContext.getBean("uApprove.viewHelper", ViewHelper.class);
    }

    /** {@inheritDoc} */
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException {
        final Map<String, Object> context = new HashMap<String, Object>();
        context.put("tou", touModule.getTou());
        viewHelper.showView(req, resp, "terms-of-use", context);
    }

    /** {@inheritDoc} */
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException {

        if (BooleanUtils.toBoolean(req.getParameter("accept"))) {
            final String principalName = LoginHelper.getPrincipalName(getServletContext(), req);
            touModule.acceptToU(principalName);
        }

        LoginHelper.returnToIdP(getServletContext(), req, resp);
    }

}
