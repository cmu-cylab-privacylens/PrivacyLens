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

import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Attribute Release Consent Test.
 */

public class AttributeReleaseConsentTest {

    /** Test. */
    @Test
    public void testCreateAttributeReleaseConsents() {
        final Attribute attribute = new Attribute("id", Arrays.asList(new String[] {"value1", "value2"}));
        final DateTime consentDate = new DateTime();
        final AttributeReleaseConsent attributeReleaseConsent = new AttributeReleaseConsent(attribute, consentDate);
        Assert.assertEquals(attributeReleaseConsent.getAttributeId(), attribute.getId());
        Assert.assertEquals(attributeReleaseConsent.getValuesHash(),
                AttributeReleaseHelper.hashValues(attribute.getValues()));
        Assert.assertEquals(attributeReleaseConsent.getDate(), consentDate);
    }
}