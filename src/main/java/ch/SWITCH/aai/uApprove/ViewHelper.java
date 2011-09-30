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

package ch.SWITCH.aai.uApprove;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * View Helper.
 */
public class ViewHelper {

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(Util.class);

    /** Default locale. */
    private Locale defaultLocale;

    /** Whether the default locale should forced. */
    private boolean forceDefaultLocale;

    /** Messages base. */
    private String messagesBase;

    /** Default constructor. */
    public ViewHelper() {
        defaultLocale = Locale.getDefault();
        forceDefaultLocale = false;
        messagesBase = "messages.";
    }

    /**
     * Initializes the view helper.
     */
    public void initialize() {
        logger.debug("ViewHelper initialized with {}default locale {}.", forceDefaultLocale ? "forced " : "",
                defaultLocale);
    }

    /**
     * Sets the default locale.
     * 
     * @param defaultLocale The defaultLocale to set.
     */
    public void setDefaultLocale(final Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    /**
     * Sets whether the default locale is enforced or not.
     * 
     * @param forceDefaultLocale The forceDefaultLocale to set.
     */
    public void setForceDefaultLocale(final boolean forceDefaultLocale) {
        this.forceDefaultLocale = forceDefaultLocale;
    }

    /**
     * Sets the messages base.
     * 
     * @param messagesBase The messagesBase to set.
     */
    public void setMessagesBase(final String messagesBase) {
        this.messagesBase = messagesBase;
    }

    /**
     * Selects the locale, which should be used.
     * 
     * @param request The HTTP request.
     * @return Returns the locale to use.
     */
    public Locale selectLocale(final HttpServletRequest request) {
        if (forceDefaultLocale) {
            logger.trace("Using forced default locale {}.", defaultLocale);
            return defaultLocale;
        } else {
            final Locale requestLocale = new Locale(request.getLocale().getLanguage());
            logger.trace("Using request locale {}.", requestLocale);
            return requestLocale;
        }
    }

    /**
     * Renders and shows the view.
     * 
     * @param servletContext The servlet context;
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @param viewName The view name.
     * @param viewContext The view context.
     */
    public void showView(final ServletContext servletContext, final HttpServletRequest request,
            final HttpServletResponse response, final String viewName, final Map<String, ?> viewContext) {

        request.setAttribute("view", viewName);
        request.setAttribute("bundle", String.format("%s.%s", messagesBase, viewName));
        request.setAttribute("locale", selectLocale(request));

        for (final Entry<String, ?> entry : viewContext.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }

        final String jsp = String.format("/uApprove/%s.jsp", viewName);
        logger.trace("Show view {}.", jsp);
        try {
            request.getRequestDispatcher(jsp).forward(request, response);
        } catch (final Exception e) {
            logger.error("Error while forwarding to view {}.", jsp, e);
            IdPHelper.handleException(servletContext, request, response, e);
        }
    }
}
