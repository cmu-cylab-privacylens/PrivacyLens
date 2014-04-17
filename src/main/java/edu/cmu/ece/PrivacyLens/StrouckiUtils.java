
package edu.cmu.ece.PrivacyLens;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import bmsi.util.Diff;
import bmsi.util.DiffPrint.Base;
import bmsi.util.DiffPrint.ContextPrint;

public class StrouckiUtils {
    private static Random randomObject = new Random();

    /**
     * Generate a random integer within bounds
     * 
     * @param min Minimum random value (inclusive)
     * @param max Maximum random value (exclusive)
     * @return random integer x, where min <= x < max
     */
    public static int getRandomRange(final int min, final int max) {
        final int mymax = max - min;
        final int myrand = randomObject.nextInt(mymax);
        final int returnrand = myrand + min;
        return returnrand;
    }

    /**
     * Generate a random boolean
     * 
     * @return random true/false value
     */
    public static boolean getRandomBoolean() {
        // out: random true/false value
        return randomObject.nextBoolean();
    }

    /**
     * Return a string of human readable duration information
     * 
     * @param millis Milliseconds (60000)
     * @return String representation (1 minute)
     */
    public static String millisToDuration(final long millis) {
        final long SECONDS = 1000;
        final long MINUTES = 60 * SECONDS;
        final long HOURS = 60 * MINUTES;
        final long DAYS = 24 * HOURS;
        final long WEEKS = 7 * DAYS;
        final long MONTHS = 30 * DAYS;
        final long YEARS = 365 * DAYS;

        long foo = millis;
        final long years = foo / YEARS;
        foo %= YEARS;
        final long months = foo / MONTHS;
        foo %= MONTHS;
        final long weeks = foo / WEEKS;
        foo %= WEEKS;
        final long days = foo / DAYS;
        foo %= DAYS;
        final long hours = foo / HOURS;
        foo %= HOURS;
        final long minutes = foo / MINUTES;
        foo %= MINUTES;
        final long seconds = foo / SECONDS;

        final StringBuilder text = new StringBuilder();

        if (years > 0) {
            text.append(years);
            text.append(" year");
            if (years != 1) {
                text.append('s');
            }
            return text.toString();
        }
        if (months > 0) {
            text.append(months);
            text.append(" month");
            if (months != 1) {
                text.append('s');
            }
            return text.toString();
        }
        if (weeks > 0) {
            text.append(weeks);
            text.append(" week");
            if (weeks != 1) {
                text.append('s');
            }
            return text.toString();
        }
        if (days > 0) {
            text.append(days);
            text.append(" day");
            if (days != 1) {
                text.append('s');
            }
            return text.toString();
        }
        if (hours > 0) {
            text.append(hours);
            text.append(" hour");
            if (hours != 1) {
                text.append('s');
            }
            return text.toString();
        }
        if (minutes > 0) {
            text.append(minutes);
            text.append(" minute");
            if (minutes != 1) {
                text.append('s');
            }
            return text.toString();
        }
        if (seconds > 0) {
            text.append(seconds);
            text.append(" second");
            if (seconds != 1) {
                text.append('s');
            }
            return text.toString();
        }
        return "less than one second";
    }

    /**
     * Diff two string arrays
     * 
     * @param before
     * @param after
     * @return String containing diff
     */
    public static String diff(final String[] before, final String[] after) {
        final Diff d = new Diff(before, after);
        final Base p = new ContextPrint(before, after);
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        for (final String line : before) {
            pw.print(line);
        }
        p.setOutput(pw);
        p.print_header("before", "after");
        p.print_script(d.diff_2(false));
        pw.flush();
        pw.close();
        return sw.toString();
    }

    /**
     * Convert a @see HttpServletRequest to a human interpretable string
     * 
     * @param request The HttpServletRequest
     * @return String containing request information
     */
    public static String request2string(final HttpServletRequest request) {
        final List<String> reqL = request2list(request);
        final StringBuilder string = new StringBuilder();
        final String[] reqA = reqL.toArray(new String[reqL.size()]);
        for (final String line : reqA) {
            string.append(line);
        }
        return string.toString();
    }

    /**
     * Convert a @see HttpServletRequest to a list of information
     * 
     * @param request The HttpServletRequest
     * @return List of String request information
     */
    public static List<String> request2list(final HttpServletRequest request) {
        final List<String> list = new ArrayList<String>();

        list.add("<request AuthType='" + request.getAuthType() + "' CharacterEncoding='"
                + request.getCharacterEncoding() + "' ContentType='" + request.getContentType() + "' ContextPath='"
                + request.getContextPath() + "' NetEndpoints='" + request.getRemoteHost() + ":"
                + request.getRemotePort() + "->" + request.getServerName() + ":" + request.getServerPort() + "->"
                + request.getLocalName() + ":" + request.getLocalPort() + "' Method='" + request.getMethod()
                + "' PathInfo='" + request.getPathInfo() + "' PathTranslated='" + request.getPathTranslated()
                + "' Protocol='" + request.getProtocol() + "' QueryString='" + request.getQueryString() + "' User='"
                + request.getRemoteUser() + "' RequestedSession='" + request.getRequestedSessionId() + "' RequestURI='"
                + request.getRequestURI() + "' Scheme='" + request.getScheme() + "' ServletPath='"
                + request.getServletPath() + "' Session='" + request.getSession(false) + "' UserPrincipal='"
                + request.getUserPrincipal() + "' >");
        final Enumeration<String> attribs = request.getAttributeNames();
        String attrib;
        while (attribs.hasMoreElements()) {
            attrib = attribs.nextElement();
            list.add("<attrib name='" + attrib + "' value='" + request.getAttribute(attrib) + "' />");
        }

        final Enumeration<String> headers = request.getHeaderNames();
        String header;
        while (headers.hasMoreElements()) {
            header = headers.nextElement();
            list.add("<header name='" + header + "' value='" + request.getHeader(header) + "' />");
        }

        final javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (final javax.servlet.http.Cookie cookie : cookies) {
                list.add("<cookie string='" + cookie + "' />");
            }
        }

        final Enumeration<String> params = request.getParameterNames();
        String param;
        while (params.hasMoreElements()) {
            param = params.nextElement();
            list.add("<param name='" + param + "' value='" + request.getParameter(param) + "' />");
        }

        list.add("<body length='" + request.getContentLength() + "' >");
        try {
            final BufferedReader br = request.getReader();
            String body;
            while ((body = br.readLine()) != null) {
                list.add(body);
            }
            br.close();
        } catch (final IOException x) {
        }

        list.add("</body>");
        list.add("</request>");
        return list;
    }

    /**
     * Build a human readable string from list
     * 
     * @param in List of String
     * @return String
     */
    public static String listToString(final List<String> in) {
        final int size = in.size();

        if (size == 0) {
            return "(nothing)";
        }
        if (size == 1) {
            return in.get(1);
        }

        final String lastItem = in.remove(size - 1);

        final StringBuilder sb = new StringBuilder();
        String delim = "";
        for (final String i : in) {
            sb.append(delim).append(i);
            delim = ", ";
        }
        sb.append(", and ");
        sb.append(lastItem);

        final String out = sb.toString();
        return out;
    }
}