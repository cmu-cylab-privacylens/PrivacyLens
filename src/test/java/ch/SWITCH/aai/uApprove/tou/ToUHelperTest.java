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

/**
 *
 */
public class ToUHelperTest {

    private ToU tou1a, tou1b, tou2;

    @BeforeClass
    public void initialize() {
        tou1a = new ToU();
        tou1a.setVersion("1.0");
        tou1a.setResource(new ClassPathResource("examples/terms-of-use.html"));
        tou1a.initialize();

        tou1b = new ToU();
        tou1b.setVersion("1.0");
        tou1b.setResource(new ByteArrayResource("some other text".getBytes()));
        tou1b.initialize();

        tou2 = new ToU();
        tou2.setVersion("2.0");
        tou2.setResource(new ClassPathResource("examples/terms-of-use.html"));
        tou2.initialize();
    }

    @Test
    public void testAcceptedToU() {
        final ToUAcceptance touAcceptance = new ToUAcceptance(tou1a, new DateTime());

        Assert.assertTrue(ToUHelper.acceptedToU(tou1a, touAcceptance, false));
        Assert.assertTrue(ToUHelper.acceptedToU(tou1a, touAcceptance, true));

        Assert.assertTrue(ToUHelper.acceptedToU(tou1b, touAcceptance, false));
        Assert.assertFalse(ToUHelper.acceptedToU(tou1b, touAcceptance, true));

        Assert.assertFalse(ToUHelper.acceptedToU(tou2, touAcceptance, false));

        final ToUAcceptance emptyToUAcceptance = ToUAcceptance.emptyToUAcceptance();
        Assert.assertFalse(ToUHelper.acceptedToU(tou2, emptyToUAcceptance, false));
    }

}
