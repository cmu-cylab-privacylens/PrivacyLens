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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * Util.
 */
public final class Util {

    /** Class logger. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    /** The message digest. */
    private static MessageDigest sha256;

    static {
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (final NoSuchAlgorithmException e) {
            throw new UApproveException("Error getting message digest instance.", e);
        }
    }

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
            stringBuilder.append(line + "\n");
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
        final byte[] digest = sha256.digest(input.getBytes());
        return Hex.encodeHexString(digest);
    }

    /**
     * Converts a whitespace separated list into a list of Strings.
     * 
     * @param string The list.
     * @return Returns a list of Strings.
     */
    public static List<String> stringToList(final String string) {
        return Arrays.asList(string.split("\\s+"));
    }
}
