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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Util test.
 */
public class UtilTest {

    /** Class logger. */
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(UtilTest.class);

    /** Test. */
    @Test
    public void testFingerprint() {
        Assert.assertEquals("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", Util.hash("test"));
    }

    /** Test. */
    @Test
    public void testStringToList() {
        Assert.assertEquals(Util.stringToList("1 2 3").size(), 3);
        Assert.assertEquals(Util.stringToList("1  2  3").size(), 3);
        Assert.assertEquals(Util.stringToList("1\n2\n3").size(), 3);
    }
}
