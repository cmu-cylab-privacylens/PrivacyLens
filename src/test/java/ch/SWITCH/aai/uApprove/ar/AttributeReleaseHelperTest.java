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
    public void testRemoveBlacklistedAttributes() {

        final List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("id1", null, null, null));
        attributes.add(new Attribute("id2", null, null, null));
        attributes.add(new Attribute("id3", null, null, null));
        attributes.add(new Attribute("id4", null, null, null));

        final List<String> blacklist = Arrays.asList(new String[] {"id2", "id4"});
        final List<Attribute> filteredAttributes =
                AttributeReleaseHelper.removeBlacklistedAttributes(attributes, blacklist);

        for (final Attribute filteredAttribute : filteredAttributes) {
            if (blacklist.contains(filteredAttribute.getId())) {
                fail("Blacklisted attribute found");
            }
        }
        Assert.assertTrue(true);
    }

    @Test
    public void testSortAttributes() {

        final List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("id2", null, null, null));
        attributes.add(new Attribute("id3", null, null, null));
        attributes.add(new Attribute("id4", null, null, null));
        attributes.add(new Attribute("id1", null, null, null));

        final List<String> ordering = Arrays.asList(new String[] {"id1", "id2"});

        AttributeReleaseHelper.sortAttributes(attributes, ordering);

        for (int i = 0; i < ordering.size(); i++) {
            Assert.assertEquals(attributes.get(i).getId(), ordering.get(i));
        }

    }

}