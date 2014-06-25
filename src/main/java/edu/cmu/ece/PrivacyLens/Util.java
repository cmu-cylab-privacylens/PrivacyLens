/*
 * Copyright (c) 2011, SWITCH
 * Copyright (c) 2013, Carnegie Mellon University
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

package edu.cmu.ece.PrivacyLens;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * Util.
 */
public final class Util {

    /** Class logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    public static ServletContext servletContext;

    /** Default constructor for utility classes is private. */
    private Util() {
    }

    /**
     * Reads a resource into a String.
     * 
     * @param resource The resource.
     * @return Returns the content of the resource.
     * @throws IOException in case of failure.
     */
    public static String readResource(final Resource resource) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }
        bufferedReader.close();
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
     * Converts a whitespace separated list into a list of Strings.
     * 
     * @param string The list.
     * @return Returns a list of Strings.
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
        IdPHelper.writeAuditLog(event, principalName, relyingPartyId, data);
    }
}
