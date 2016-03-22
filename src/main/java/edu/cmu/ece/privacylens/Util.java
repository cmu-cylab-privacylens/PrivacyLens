/*
 * Copyright (c) 2011, SWITCH
 * Copyright (c) 2013-2016, Carnegie Mellon University
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

package edu.cmu.ece.privacylens;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.authn.context.navigate.SubjectContextPrincipalLookupFunction;

/**
 * Util.
 */
public final class Util {

    /** Class logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    private static ServletContext servletContext;

    private static WebApplicationContext springContext;

    private static Random randomObject = new Random();

    /** Default constructor for utility classes is private. */
    private Util() {
    }

    /**
     * @return Returns the servletContext.
     */
    public static ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * Returns the springContext
     *
     * @return the springContext.
     */
    public static synchronized WebApplicationContext getSpringContext() {
        if (springContext == null) {
            springContext =
                    WebApplicationContextUtils
                            .getRequiredWebApplicationContext(servletContext);
        }
        return springContext;
    }

    /**
     * Returns the principal name
     *
     * @param prc ProfileRequestContext
     * @return the principal name
     */
    public static String getPrincipalName(final ProfileRequestContext prc) {
        final SubjectContextPrincipalLookupFunction subject =
                springContext.getBean("shibboleth.PrincipalNameLookup.Subject",
                        SubjectContextPrincipalLookupFunction.class);
        final ChildContextLookup<ProfileRequestContext, SubjectContext> cc =
                new ChildContextLookup<ProfileRequestContext, SubjectContext>(
                        SubjectContext.class);

        final Function<ProfileRequestContext, String> getter =
                Functions.compose(subject, cc);
        final String result = getter.apply(prc);
        return result;
    }

    /**
     * Reads a resource into a String.
     *
     * @param resource The resource.
     * @return Returns the content of the resource.
     * @throws IOException in case of failure.
     */
    public static String readResource(final Resource resource)
            throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        final InputStream inputStream = resource.getInputStream();

        final BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Creates a fingerprint.
     *
     * @param input The input String.
     * @return Returns a fingerprint.
     */
    public static String hash(final String input) {
        return DigestUtils.sha256Hex(input);
    }

    /**
     * Converts a whitespace separated list into a list of Strings. Note it doesn't handle quoted text!
     *
     * @param string The list.
     * @return Returns an _unmodifiable_ list of Strings.
     */
    public static List<String> stringToList(final String string) {
        final String input = StringUtils.trimToEmpty(string);
        if (StringUtils.isEmpty(input)) {
            return Collections.emptyList();
        }
        return Arrays.asList(input.split("\\s+"));
    }

    /**
     * Logs an audit log.
     *
     * @param event The event to log.
     * @param principalName The principal name to log.
     * @param relyingPartyId The relying party id to log.
     * @param data The data to log.
     */
    public static void auditLog(final String event, final String principalName, final String relyingPartyId,
            final List<String> data) {
        final String requestBinding = "edu.cmu.ece.privacylens";

        final StringBuilder entryString = new StringBuilder();
        entryString.append(new DateTime().toString(ISODateTimeFormat.basicDateTimeNoMillis().withZone(DateTimeZone.UTC)));
        entryString.append('|');

        if (requestBinding != null) {
            entryString.append(requestBinding);
        }
        entryString.append('|');

        if (event != null) {
            entryString.append(event);
        }
        entryString.append('|');

        if (relyingPartyId != null) {
            entryString.append(relyingPartyId);
        }
        entryString.append('|');

        if (principalName != null) {
            entryString.append(principalName);
        }
        entryString.append('|');

        for (final String attribute : data) {
            entryString.append(attribute);
            entryString.append(",");
        }
        entryString.append("|");

        final Logger logger = LoggerFactory.getLogger("Shibboleth-Audit");
        logger.info(entryString.toString());
    }

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
        // Don't modify the list provided, create new one
        final List<String> list = new LinkedList<String>(in);
        final int size = list.size();

        if (size == 0) {
            return "(nothing)";
        }
        if (size == 1) {
            return list.get(0);
        }

        final String lastItem = list.remove(size - 1);

        final StringBuilder sb = new StringBuilder();
        String delim = "";
        for (final String i : list) {
            sb.append(delim).append(i);
            delim = ", ";
        }
        sb.append(", and ");
        sb.append(lastItem);

        final String out = sb.toString();
        return out;
    }
}
