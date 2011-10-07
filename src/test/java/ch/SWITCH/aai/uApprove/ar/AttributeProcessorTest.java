/*
 * Copyright (c) 2011, SWITCH
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

package ch.SWITCH.aai.uApprove.ar;

import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        attributes.add(new Attribute("id1", null));
        attributes.add(new Attribute("id2", null));
        attributes.add(new Attribute("id3", null));
        attributes.add(new Attribute("id4", null));

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
        attributes.add(new Attribute("id2", null));
        attributes.add(new Attribute("id3", null));
        attributes.add(new Attribute("id4", null));
        attributes.add(new Attribute("id1", null));

        attributeProcessor.sortAttributes(attributes);

        Assert.assertEquals(attributes.get(0).getId(), "id1");
        Assert.assertEquals(attributes.get(1).getId(), "id2");
    }

    /** Test. */
    @Test
    public void testRemoveEmptyValues() {
        final Attribute attribute1 =
                new Attribute("id1", new ArrayList<String>(Arrays.asList(new String[] {"", "  ", null, "value"})));

        attributeProcessor.removeEmptyValues(attribute1);
        Assert.assertTrue(attribute1.getValues().contains("value"));
        Assert.assertFalse(attribute1.getValues().contains(""));
        Assert.assertFalse(attribute1.getValues().contains("  "));
        Assert.assertFalse(attribute1.getValues().contains(null));

        final Attribute attribute2 = new Attribute("id2", new ArrayList<String>(Arrays.asList(new String[] {null})));
        attributeProcessor.removeEmptyValues(attribute2);
        Assert.assertTrue(attribute2.getValues().isEmpty());
    }

    /** Test. */
    @Test
    public void testRemoveDuplicateValues() {
        final Attribute attribute =
                new Attribute("id1", new ArrayList<String>(Arrays.asList(new String[] {"value1", "value2", "value1"})));

        attributeProcessor.removeDuplicateValues(attribute);
        Assert.assertEquals(attribute.getValues().size(), 2);
        Assert.assertTrue(attribute.getValues().contains("value1"));
        Assert.assertTrue(attribute.getValues().contains("value2"));
    }

    /** Test. */
    @Test
    public void testRemoveEmptyAttributes() {
        final List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("id1", Collections.<String> emptyList()));
        attributes.add(new Attribute("id2", Arrays.asList(new String[] {"value1", "value2"})));

        attributeProcessor.removeEmptyAttributes(attributes);
        Assert.assertEquals(attributes.size(), 1);
        Assert.assertEquals(attributes.get(0).getId(), "id2");
    }

}