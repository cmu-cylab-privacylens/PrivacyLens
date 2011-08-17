
package ch.SWITCH.aai.uApprove;

import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewHelper {

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(Util.class);

    /** Velocity engine. */
    private final VelocityEngine velocityEngine;

    /** Default constructor. */
    public ViewHelper() {
        velocityEngine = new VelocityEngine();
    }

    public void setVelocityProperties(final Properties velocityProperties) {
        try {
            velocityEngine.init(velocityProperties);
        } catch (final Exception e) {
            logger.error("Error while initializing the velocity engine.", e);
        }
    }

    public void showView(final HttpServletResponse response, final String viewName, final Map<String, ?> context) {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        try {
            velocityEngine.mergeTemplate(viewName, "UTF-8", new VelocityContext(context), response.getWriter());
        } catch (final Exception e) {
            throw new UApproveException(e);
        }
    }
}
