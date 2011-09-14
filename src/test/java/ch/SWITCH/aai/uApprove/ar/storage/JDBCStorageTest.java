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

package ch.SWITCH.aai.uApprove.ar.storage;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.SWITCH.aai.uApprove.ar.Attribute;
import ch.SWITCH.aai.uApprove.ar.AttributeReleaseConsent;

/**
 * Tests JDBC storage using the Spring JDBC framework.
 */

@ContextConfiguration(locations = {"classpath:/uApprove-test.xml"})
@TransactionConfiguration(defaultRollback = true)
public class JDBCStorageTest extends AbstractTransactionalTestNGSpringContextTests {

    /** Class logger. */
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(JDBCStorageTest.class);

    /** The storage. */
    @Autowired
    private Storage storage;

    /** Before class. */
    @BeforeClass
    @Rollback(false)
    public void initialize() {
        super.executeSqlScript("classpath:/storage/attribute-release-schema.sql", false);
    }

    /** Test. */
    @Test
    public void crudAttributeReleaseConsent() {
        final String userId = "userId";
        final String relyingPartyId = "relyingPartyId";
        final Attribute attribute = new Attribute("id", Arrays.asList(new String[] {"value1", "value2"}));
        final AttributeReleaseConsent attributeRelease1 = new AttributeReleaseConsent(attribute, new DateTime());

        Assert.assertFalse(storage.containsAttributeReleaseConsent(userId, relyingPartyId, attribute.getId()));

        storage.createAttributeReleaseConsent(userId, relyingPartyId, attributeRelease1);
        Assert.assertTrue(storage.containsAttributeReleaseConsent(userId, relyingPartyId, attribute.getId()));

        List<AttributeReleaseConsent> attributeReleases = storage.readAttributeReleaseConsents(userId, relyingPartyId);
        Assert.assertEquals(attributeReleases.size(), 1);
        Assert.assertEquals(attributeReleases.get(0).getAttributeId(), attributeRelease1.getAttributeId());
        Assert.assertEquals(attributeReleases.get(0).getValuesHash(), attributeRelease1.getValuesHash());
        Assert.assertEquals(attributeReleases.get(0).getDate(), attributeRelease1.getDate());

        final AttributeReleaseConsent attributeRelease2 =
                new AttributeReleaseConsent("id", "otherhash", new DateTime());
        storage.updateAttributeReleaseConsent(userId, relyingPartyId, attributeRelease2);

        attributeReleases = storage.readAttributeReleaseConsents(userId, relyingPartyId);
        Assert.assertEquals(attributeReleases.size(), 1);
        Assert.assertEquals(attributeReleases.get(0).getValuesHash(), attributeRelease2.getValuesHash());

        storage.deleteAttributeReleaseConsents(userId, relyingPartyId);
        Assert.assertFalse(storage.containsAttributeReleaseConsent(userId, relyingPartyId, attribute.getId()));
        Assert.assertTrue(storage.readAttributeReleaseConsents(userId, relyingPartyId).isEmpty());
    }
}
