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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * View Helper.
 */
public class ViewHelper {

    /**
     * Localized Strings.
     */
    public class LocalizedStrings {
        /** Resource bundle. */
        private final ResourceBundle resourceBundle;

        /**
         * Constructor.
         * 
         * @param resourceBundle The resource bundle to use.
         */
        public LocalizedStrings(final ResourceBundle resourceBundle) {
            this.resourceBundle = resourceBundle;
        }

        /**
         * Gets the string.
         * 
         * @param key The key.
         * @return Returns the string.
         */
        public String get(final String key) {
            return resourceBundle.getString(key);
        }
    }

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(Util.class);

    /** Velocity engine. */
    private final VelocityEngine velocityEngine;

    /** Default locale. */
    private Locale defaultLocale;

    /** Whether the default locale should forced. */
    private boolean forceDefaultLocale;

    /** Base path for resource bundles. */
    private String resourceBundleBase;

    /** Base path for view templates. */
    private String viewTemplateBase;

    /** Default constructor. */
    public ViewHelper() {
        velocityEngine = new VelocityEngine();
        defaultLocale = Locale.getDefault();
        forceDefaultLocale = false;
        resourceBundleBase = "messages";
        viewTemplateBase = "views";
    }

    /**
     * Initializes the view helper.
     */
    public void initialize() {
        logger.debug("ViewHelper initialized with {}default locale {}.", forceDefaultLocale ? "forced " : "",
                defaultLocale);
    }

    /**
     * Sets the velocity properties.
     * 
     * @param velocityProperties The velocity properties.
     */
    public void setVelocityProperties(final Properties velocityProperties) {
        try {
            velocityEngine.init(velocityProperties);
        } catch (final Exception e) {
            throw new UApproveException("Error while initializing the velocity engine.", e);
        }
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
     * Sets the base path for resource bundles.
     * 
     * @param resourceBundleBase The resourceBundleBase to set.
     */
    public void setResourceBundleBase(final String resourceBundleBase) {
        this.resourceBundleBase = resourceBundleBase;
    }

    /**
     * Sets the base path for view templates.
     * 
     * @param viewTemplateBase The viewTemplateBase to set.
     */
    public void setViewTemplateBase(final String viewTemplateBase) {
        this.viewTemplateBase = viewTemplateBase;
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
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @param viewName The view name.
     * @param viewContext The view context.
     */
    public void showView(final HttpServletRequest request, final HttpServletResponse response, final String viewName,
            final Map<String, ?> viewContext) {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        final VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("MessageFormat", MessageFormat.class);
        velocityContext.put("messages", getLocalizedMessages(viewName, selectLocale(request)));

        final String templateName = String.format("%s/%s.html", viewTemplateBase, viewName);
        try {
            velocityEngine.mergeTemplate(templateName, "UTF-8", new VelocityContext(viewContext, velocityContext),
                    response.getWriter());
        } catch (final Exception e) {
            throw new UApproveException("Error while merge and writing view.", e);
        }
    }

    /**
     * Get the localized messages.
     * 
     * @param resource The resource.
     * @param locale The locale.
     * @return Returns the localized strings.
     */
    private LocalizedStrings getLocalizedMessages(final String resource, final Locale locale) {
        final ResourceBundle resourceBundle =
                ResourceBundle.getBundle(String.format("%s.%s", resourceBundleBase, resource), locale);
        return new LocalizedStrings(resourceBundle);
    }
}
