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

import org.joda.time.DateTime;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.SWITCH.aai.uApprove.Util;

/**
 * Tests ToUAcceptance.
 */

@Test
public class ToUAcceptanceTest {

    private ToU tou;

    @BeforeClass
    public void initialize() {
        tou = new ToU();
        tou.setVersion("1.0");
        tou.setResource(new ClassPathResource("examples/terms-of-use.html"));
        tou.initialize();
    }

    public void createToUAcceptance() {
        final DateTime date = new DateTime();
        final ToUAcceptance touAcceptance = new ToUAcceptance(tou, date);
        Assert.assertEquals(tou.getVersion(), touAcceptance.getVersion());
        final String fingerprint = Util.hash(tou.getContent());
        Assert.assertEquals(fingerprint, touAcceptance.getFingerprint());
        Assert.assertEquals(date, touAcceptance.getAcceptanceDate());
    }

    public void contains() {
        final ToUAcceptance touAcceptance = new ToUAcceptance(tou, new DateTime());
        Assert.assertTrue(touAcceptance.contains(tou));

        final ToUAcceptance emptyToUAcceptance = ToUAcceptance.emptyToUAcceptance();
        Assert.assertFalse(emptyToUAcceptance.contains(tou));

        final ToU otherToU = new ToU();
        otherToU.setVersion("2.0");
        otherToU.setResource(new ClassPathResource("examples/terms-of-use.html"));
        otherToU.initialize();
        ToUAcceptance otherToUAcceptance = new ToUAcceptance(otherToU, new DateTime());
        Assert.assertFalse(otherToUAcceptance.contains(tou));

        otherToU.setVersion("1.0");
        otherToU.setResource(new ByteArrayResource("some other text".getBytes()));
        otherToU.initialize();
        otherToUAcceptance = new ToUAcceptance(otherToU, new DateTime());
        Assert.assertFalse(otherToUAcceptance.contains(tou));

    }
}