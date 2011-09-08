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
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.SWITCH.aai.uApprove.Util;

/**
 * Tests ToUAcceptance.
 */

public class ToUAcceptanceTest {

    /** Teh ToU. */
    private ToU tou;

    /** Before class. */
    @BeforeClass
    public void initialize() {
        tou = new ToU();
        tou.setVersion("1.0");
        tou.setResource(new ClassPathResource("examples/terms-of-use.html"));
        tou.initialize();
    }

    /** Test. */
    @Test
    public void testCreateToUAcceptance() {
        final DateTime date = new DateTime();
        final ToUAcceptance touAcceptance = new ToUAcceptance(tou, date);
        Assert.assertEquals(tou.getVersion(), touAcceptance.getVersion());
        final String fingerprint = Util.hash(tou.getContent());
        Assert.assertEquals(fingerprint, touAcceptance.getFingerprint());
        Assert.assertEquals(date, touAcceptance.getAcceptanceDate());
    }

}
