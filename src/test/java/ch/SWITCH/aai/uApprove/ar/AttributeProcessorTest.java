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

import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.SWITCH.aai.uApprove.Util;

/**
 * Tests AttributeReleaseHelper.
 */
public class AttributeProcessorTest {

    /** Class logger. */
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(AttributeProcessorTest.class);

    /** The attribute processor. */
    private AttributeProcessor attributeProcessor;

    /** Before class. */
    @BeforeClass
    public void initialize() {
        attributeProcessor = new AttributeProcessor();
    }

    /** Test. */
    @Test
    public void testRemoveBlacklistedAttributes() {

        final String blacklist = "id2 id4";
        attributeProcessor.setBlacklist(blacklist);
        final List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("id1", null, null, null));
        attributes.add(new Attribute("id2", null, null, null));
        attributes.add(new Attribute("id3", null, null, null));
        attributes.add(new Attribute("id4", null, null, null));

        attributeProcessor.removeBlacklistedAttributes(attributes);

        for (final Attribute attribute : attributes) {
            if (Util.stringToList(blacklist).contains(attribute.getId())) {
                fail("Blacklisted attribute found");
            }
        }
        Assert.assertTrue(true);
    }

    /** Test. */
    @Test
    public void testSortAttributes() {

        attributeProcessor.setOrder("id1 id2");

        final List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("id2", null, null, null));
        attributes.add(new Attribute("id3", null, null, null));
        attributes.add(new Attribute("id4", null, null, null));
        attributes.add(new Attribute("id1", null, null, null));

        attributeProcessor.sortAttributes(attributes);

        Assert.assertEquals(attributes.get(0).getId(), "id1");
        Assert.assertEquals(attributes.get(1).getId(), "id2");
    }

}