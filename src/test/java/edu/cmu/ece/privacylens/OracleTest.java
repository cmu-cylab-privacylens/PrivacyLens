/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2014 Carnegie Mellon University
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class should test the functions in the Oracle class
 */
public class OracleTest {
    /** Class logger. */
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    Oracle oracle = Oracle.getInstance();

    /** Before class. */
    @BeforeClass
    public void initialize() {
        oracle.setConfig(new ClassPathResource("configuration/spsetup.json"));
    }

    /** Test whether the foo function works */
    @Test
    public void testFoo() {
        Assert.assertTrue(true);
    }

    /** Test whether the getServiceName function works */
    @Test
    public void testGetServiceName() {
        Oracle.setRegexpMatch(false);
        final String rpid = "plugh";
        final String serviceName = oracle.getServiceName(rpid);
        Assert.assertEquals(serviceName, "UNKNOWN");
        final String rpid2 = "https://scalepriv.ece.cmu.edu/shibboleth";
        final String serviceName2 = oracle.getServiceName(rpid2);
        Assert.assertEquals(serviceName2, "CMU's Calendar");
        Oracle.setRegexpMatch(true);
        final String rpid3 = "https://scalepriv.ece.cmu.edu/foobar";
        final String serviceName3 = oracle.getServiceName(rpid3);
        Assert.assertEquals(serviceName3, "The site");
    }

}
