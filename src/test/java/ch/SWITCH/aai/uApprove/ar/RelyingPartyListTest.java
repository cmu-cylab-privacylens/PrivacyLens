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

package ch.SWITCH.aai.uApprove.ar;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.SWITCH.aai.uApprove.RelyingPartyList;

/**
 * Tests the relying party list.
 */
public class RelyingPartyListTest {

    /** The relying party list. */
    private RelyingPartyList relyingPartyList;

    /** Before class. */
    @BeforeClass
    public void initialize() {
        relyingPartyList = new RelyingPartyList();
        final String expressions =
                "https://sp\\.example1\\.org/shibboleth https://sp\\.example2\\.org/shibboleth https://.*\\.example3\\.org/shibboleth";
        relyingPartyList.setRegularExpressions(expressions);
    }

    /** Test. */
    @Test
    public void testContains() {
        for (final Boolean isBlacklist : new Boolean[] {true, false}) {
            relyingPartyList.setBlacklist(isBlacklist);
            Assert.assertTrue(relyingPartyList.contains("https://sp.example1.org/shibboleth") != isBlacklist);
            Assert.assertTrue(relyingPartyList.contains("https://sp.example1.org/shibboleth") != isBlacklist);
            Assert.assertTrue(relyingPartyList.contains("https://sp.example2.org/shibboleth") != isBlacklist);
            Assert.assertTrue(relyingPartyList.contains("https://sp.example3.org/shibboleth") != isBlacklist);
            Assert.assertTrue(relyingPartyList.contains("https://xx.example3.org/shibboleth") != isBlacklist);
            Assert.assertFalse(relyingPartyList.contains("https://xx.example1.org/shibboleth") != isBlacklist);
            Assert.assertFalse(relyingPartyList.contains("https://sp.example4.org/shibboleth") != isBlacklist);
        }
    }
}
