
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

public class ViewHelper {

    /**
     *
     */
    public class LocalizedStrings {
        private final ResourceBundle resourceBundle;

        public LocalizedStrings(final ResourceBundle resourceBundle) {
            this.resourceBundle = resourceBundle;
        }

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

    private String resourceBundleBase;

    private String viewTemplateBase;

    /** Default constructor. */
    public ViewHelper() {
        velocityEngine = new VelocityEngine();
        defaultLocale = Locale.getDefault();
        forceDefaultLocale = false;
        resourceBundleBase = "messages";
        viewTemplateBase = "views";
    }

    public void initialize() {
        logger.debug("ViewHelper initialized with{}default locale {}.", forceDefaultLocale ? " forced " : " ",
                defaultLocale);
    }

    public void setVelocityProperties(final Properties velocityProperties) {
        try {
            velocityEngine.init(velocityProperties);
        } catch (final Exception e) {
            throw new UApproveException("Error while initializing the velocity engine.", e);
        }
    }

    /**
     * @param defaultLocale The defaultLocale to set.
     */
    public void setDefaultLocale(final Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    /**
     * @param forceDefaultLocale The forceDefaultLocale to set.
     */
    public void setForceDefaultLocale(final boolean forceDefaultLocale) {
        this.forceDefaultLocale = forceDefaultLocale;
    }

    /**
     * @param resourceBundleBase The resourceBundleBase to set.
     */
    public void setResourceBundleBase(final String resourceBundleBase) {
        this.resourceBundleBase = resourceBundleBase;
    }

    /**
     * @param viewTemplateBase The viewTemplateBase to set.
     */
    public void setViewTemplateBase(final String viewTemplateBase) {
        this.viewTemplateBase = viewTemplateBase;
    }

    public Locale selectLocale(final HttpServletRequest request) {
        if (forceDefaultLocale) {
            return defaultLocale;
        } else {
            return request.getLocale();
        }
    }

    public void showView(final HttpServletRequest request, final HttpServletResponse response, final String viewName,
            final Map<String, ?> viewContext) {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        final VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("MessageFormat", MessageFormat.class);
        velocityContext.put("localizedMessages", getLocalizedMessages(viewName, selectLocale(request)));

        final String templateName = String.format("%s/%s.html", viewTemplateBase, viewName);
        try {
            velocityEngine.mergeTemplate(templateName, "UTF-8", new VelocityContext(viewContext, velocityContext),
                    response.getWriter());
        } catch (final Exception e) {
            throw new UApproveException("Error while merge and writing view.", e);
        }
    }

    private LocalizedStrings getLocalizedMessages(final String resource, final Locale locale) {
        final ResourceBundle resourceBundle =
                ResourceBundle.getBundle(String.format("%s.%s", resourceBundleBase, resource), locale);
        return new LocalizedStrings(resourceBundle);
    }
}
