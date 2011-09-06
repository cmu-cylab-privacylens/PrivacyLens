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

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import ch.SWITCH.aai.uApprove.Util;

/**
 * Tests AttributeReleaseHelper.
 */
public class AttributeReleaseHelperTest {

    final Logger logger = LoggerFactory.getLogger(AttributeReleaseHelperTest.class);

    @Test
    public void testHashValues() {
        final List<String> unsortedValues = Arrays.asList(new String[] {"bbb", "ccc", "aaa"});
        final String hash = AttributeReleaseHelper.hashValues(unsortedValues);
        Assert.assertEquals(hash, Util.hash("aaa;bbb;ccc;"));
    }

    @Test
    public void testResolveFqdn() {
        final String entityId1 = "https://sp.example.org/shibboleth";
        Assert.assertEquals(AttributeReleaseHelper.resolveFqdn(entityId1), "sp.example.org");

        final String entityId2 = "urn:mace:federation.org:sp.example.org";
        Assert.assertEquals(AttributeReleaseHelper.resolveFqdn(entityId2), "sp.example.org");

        final String entityId3 = "sp.example.org";
        Assert.assertEquals(AttributeReleaseHelper.resolveFqdn(entityId3), "sp.example.org");
    }
}