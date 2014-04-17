
package edu.cmu.ece.PrivacyLens;

import org.apache.commons.lang.StringEscapeUtils;

public class StrouckiHTMLUtils {
    /**
     * Escape a string for a javascript environment
     * 
     * @param in Original string
     * @return Escaped string
     */
    public static String escapeJS(final String in) {
        final String out = StringEscapeUtils.escapeJavaScript(in);
        return out;
    }

    public static String getEmailAdminBoilerText(final String email) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<p><a href=\"mailto:");
        stringBuilder.append(email);
        stringBuilder.append("\" >");
        stringBuilder.append("Click here");
        stringBuilder.append("</a>");
        stringBuilder
                .append(" to contact an administrator if you have further questions or believe this information is incorrect.");
        stringBuilder.append("</p>");
        return stringBuilder.toString();
    }
}