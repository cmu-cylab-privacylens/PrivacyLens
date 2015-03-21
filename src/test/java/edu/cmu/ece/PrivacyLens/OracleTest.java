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

package edu.cmu.ece.PrivacyLens;

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

    /**
     * Test whether the setUserName function works. Covers getUserName too.
     */
    @Test
    public void testSetUserName() {
        final String userName = "xyzzy";
        oracle.setUserName(userName);
        final String userName2 = oracle.getUserName();

        Assert.assertEquals(userName, userName2);
    }

    /** Test whether the setRelyingPartyId function works */
    @Test
    public void testSetRelyingPartyId() {
        Oracle.setRegexpMatch(false);
        final String rpid = "plugh";
        oracle.setRelyingPartyId(rpid);
        final String serviceName = oracle.getServiceName();
        Assert.assertEquals(serviceName, "UNKNOWN");
        final String rpid2 = "https://scalepriv.ece.cmu.edu/shibboleth";
        oracle.setRelyingPartyId(rpid2);
        final String serviceName2 = oracle.getServiceName();
        Assert.assertEquals(serviceName2, "CMU's Calendar");
    }

    /** Test whether matching url to service provider works */
    @Test
    public void testGetRelyingParty() {
        final String rpid = "https://scalepriv.ece.cmu.edu/shibboleth";
        Oracle.setRegexpMatch(false);

        oracle.setRelyingPartyId(rpid);
        String serviceName = oracle.getServiceName();
        Assert.assertEquals(serviceName, "CMU's Calendar");

        Oracle.setRegexpMatch(true);
        serviceName = oracle.getServiceName();
        Assert.assertEquals(serviceName, "CMU's Calendar");

        final String rpid2 = "https://scalepriv.ece.cmu.edu/foobar";
        oracle.setRelyingPartyId(rpid2);
        serviceName = oracle.getServiceName();
        Assert.assertEquals(serviceName, "The site");

        Oracle.setRegexpMatch(false);
        serviceName = oracle.getServiceName();
        Assert.assertEquals(serviceName, "UNKNOWN");

    }

}
