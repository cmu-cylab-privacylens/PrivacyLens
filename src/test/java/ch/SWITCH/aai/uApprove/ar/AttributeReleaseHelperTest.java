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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.SWITCH.aai.uApprove.Util;

/**
 * Tests AttributeReleaseHelper.
 */
public class AttributeReleaseHelperTest {

    /** Class logger. */
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(AttributeReleaseHelperTest.class);

    /** Attribute. */
    private Attribute attribute1a;

    /** Attribute. */
    private Attribute attribute1b;

    /** Attribute. */
    private Attribute attribute2;

    /** Before class. */
    @BeforeClass
    public void initialize() {
        attribute1a = new Attribute("id1", Arrays.asList(new String[] {"value1a", "value2a"}));
        attribute1b = new Attribute("id1", Arrays.asList(new String[] {"value1b", "value2b"}));
        attribute2 = new Attribute("id2", Arrays.asList(new String[] {"value1", "value2"}));
    }

    /** Test. */
    @Test
    public void testHashValues() {
        final List<String> unsortedValues = Arrays.asList(new String[] {"bbb", "ccc", "aaa"});
        final String hash = AttributeReleaseHelper.hashValues(unsortedValues);
        Assert.assertEquals(hash, Util.hash("aaa;bbb;ccc;"));
    }

    /** Test. */
    @Test
    public void testResolveFqdn() {
        final String entityId1 = "https://sp.example.org/shibboleth";
        Assert.assertEquals(AttributeReleaseHelper.resolveFqdn(entityId1), "sp.example.org");

        final String entityId2 = "urn:mace:federation.org:sp.example.org";
        Assert.assertEquals(AttributeReleaseHelper.resolveFqdn(entityId2), "sp.example.org");

        final String entityId3 = "sp.example.org";
        Assert.assertEquals(AttributeReleaseHelper.resolveFqdn(entityId3), "sp.example.org");
    }

    /** Test. */
    @Test
    public void testApprovedAttribute() {
        final AttributeReleaseConsent attributeRelease = new AttributeReleaseConsent(attribute1a, new DateTime());

        Assert.assertTrue(AttributeReleaseHelper.approvedAttribute(attribute1a, attributeRelease, false));
        Assert.assertTrue(AttributeReleaseHelper.approvedAttribute(attribute1a, attributeRelease, true));

        Assert.assertTrue(AttributeReleaseHelper.approvedAttribute(attribute1b, attributeRelease, false));
        Assert.assertFalse(AttributeReleaseHelper.approvedAttribute(attribute1b, attributeRelease, true));

        Assert.assertFalse(AttributeReleaseHelper.approvedAttribute(attribute2, attributeRelease, false));
    }

    /** Test. */
    @Test
    public void testApprovedAttributes() {
        Assert.assertTrue(AttributeReleaseHelper.approvedAttributes(Collections.<Attribute> emptyList(),
                Collections.<AttributeReleaseConsent> emptyList(), true));

        final List<Attribute> attributes = Arrays.asList(new Attribute[] {attribute1a, attribute1b, attribute2});

        Assert.assertFalse(AttributeReleaseHelper.approvedAttributes(attributes,
                Collections.<AttributeReleaseConsent> emptyList(), true));

        final List<AttributeReleaseConsent> attributeReleases = new ArrayList<AttributeReleaseConsent>();
        attributeReleases.add(new AttributeReleaseConsent(attribute1a, new DateTime()));
        attributeReleases.add(new AttributeReleaseConsent(attribute1b, new DateTime()));

        Assert.assertFalse(AttributeReleaseHelper.approvedAttributes(attributes, attributeReleases, true));

        attributeReleases.add(new AttributeReleaseConsent(attribute2, new DateTime()));
        Assert.assertTrue(AttributeReleaseHelper.approvedAttributes(attributes, attributeReleases, true));

        Assert.assertTrue(AttributeReleaseHelper.approvedAttributes(Collections.<Attribute> emptyList(),
                attributeReleases, true));
    }
}