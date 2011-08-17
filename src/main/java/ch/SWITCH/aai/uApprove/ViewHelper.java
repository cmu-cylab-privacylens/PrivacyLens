
package ch.SWITCH.aai.uApprove;

import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class ViewHelper {

    private final VelocityEngine velocityEngine;

    public ViewHelper(final Properties velocityProperties) {
        try {
            velocityEngine = new VelocityEngine(velocityProperties);
        } catch (final Exception e) {
            throw new UApproveException(e);
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
