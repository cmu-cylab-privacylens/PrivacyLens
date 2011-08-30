
package ch.SWITCH.aai.uApprove;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

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

    /** Default constructor. */
    public ViewHelper() {
        velocityEngine = new VelocityEngine();
        defaultLocale = Locale.getDefault();
        forceDefaultLocale = false;
    }

    public void initialize() {
        logger.debug("ViewHelper initialized with{}default locale {}.", forceDefaultLocale ? " forced " : " ",
                defaultLocale);
    }

    public void setVelocityProperties(final Properties velocityProperties) {
        try {
            velocityEngine.init(velocityProperties);
        } catch (final Exception e) {
            logger.error("Error while initializing the velocity engine.", e);
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

    public Locale selectLocale(final Locale userLocale, final Collection<Locale> availableLocales) {
        if (forceDefaultLocale) {
            return defaultLocale;
        } else {
            return userLocale;
        }
    }

    public void showView(final HttpServletResponse response, final String viewName, final Map<String, ?> context) {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        final String templateName = String.format("views/%s.html", viewName);
        try {
            velocityEngine.mergeTemplate(templateName, "UTF-8", new VelocityContext(context), response.getWriter());
        } catch (final Exception e) {
            logger.error("Error while merge and writing view.", e);
        }
    }

    public LocalizedStrings getLocalizedStrings(final String resource, final Locale locale) {
        final ResourceBundle resourceBundle = ResourceBundle.getBundle(String.format("messages.%s", resource), locale);
        return new LocalizedStrings(resourceBundle);
    }
}
