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

package ch.SWITCH.aai.uApprove.tou;

import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import ch.SWITCH.aai.uApprove.UApproveException;

/**
 * Tests ToU.
 */

@Test
public class ToUTest {

    public void testInitialize() {
        final ToU tou = new ToU();
        try {
            tou.initialize();
            Assert.fail();
        } catch (final IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
        tou.setVersion("1.0");
        try {
            tou.initialize();
            Assert.fail();
        } catch (final IllegalArgumentException e) {
            Assert.assertTrue(true);
        }

        try {
            tou.setResource(new ClassPathResource("not-existent.html"));
            Assert.fail();
        } catch (final UApproveException e) {
            Assert.assertTrue(true);
        }

        tou.setResource(new ClassPathResource("examples/terms-of-use.html"));
        tou.initialize();
        Assert.assertEquals(tou.getVersion(), "1.0");
        Assert.assertTrue(tou.getContent().contains("This is an example ToU"));

    }
}
