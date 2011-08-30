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
import java.util.Collections;
import java.util.Locale;

import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 */

public class AttributeReleaseTest {

    private Attribute attribute1, attribute2;

    private DateTime consentDate;

    @BeforeClass
    public void initialize() {
        attribute1 =
                new Attribute("id1", Collections.<Locale, String> emptyMap(), Collections.<Locale, String> emptyMap(),
                        Arrays.asList(new String[] {"value1", "value2"}));

        attribute2 =
                new Attribute("id2", Collections.<Locale, String> emptyMap(), Collections.<Locale, String> emptyMap(),
                        Arrays.asList(new String[] {"value1", "value2"}));

        consentDate = new DateTime();
    }

    @Test
    public void createAttributeReleases() {
        final AttributeRelease attributeRelease = new AttributeRelease(attribute1, consentDate);
        Assert.assertEquals(attributeRelease.getAttributeId(), attribute1.getId());
        Assert.assertEquals(attributeRelease.getValuesHash(), AttributeReleaseHelper.hashValues(attribute1.getValues()));
        Assert.assertEquals(attributeRelease.getDate(), consentDate);
    }

    @Test
    public void contains() {
        final AttributeRelease attributeRelease = new AttributeRelease(attribute1, consentDate);
        Assert.assertTrue(attributeRelease.contains(attribute1));
        Assert.assertFalse(attributeRelease.contains(attribute2));

        final Attribute attribute3 =
                new Attribute(attribute1.getId(), Collections.<Locale, String> emptyMap(),
                        Collections.<Locale, String> emptyMap(), Arrays.asList(new String[] {"other value"}));

        Assert.assertFalse(attributeRelease.contains(attribute3));
    }
}