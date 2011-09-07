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

import ch.SWITCH.aai.uApprove.ar.AttributeRelease;

/**
 * Tests JDBC storage using the Spring JDBC framework.
 */

@ContextConfiguration(locations = {"classpath:/context/uApprove-test.xml"})
@TransactionConfiguration(defaultRollback = true)
public class JDBCStorageTest extends AbstractTransactionalTestNGSpringContextTests {

    private final Logger logger = LoggerFactory.getLogger(JDBCStorageTest.class);

    @Autowired
    private Storage storage;

    @BeforeClass
    @Rollback(false)
    public void initialize() {
        super.executeSqlScript("classpath:/storage/attribute-release-schema.sql", false);
    }

    @Test
    public void crudAttributeRelease() {
        final String userId = "userId";
        final String relyingPartyId = "relyingPartyId";
        final AttributeRelease attributeRelease1 = new AttributeRelease("id", "hash", new DateTime());

        Assert.assertFalse(storage.containsAttributeReleases(userId, relyingPartyId));

        storage.createAttributeRelease(userId, relyingPartyId, attributeRelease1);
        Assert.assertTrue(storage.containsAttributeReleases(userId, relyingPartyId));

        List<AttributeRelease> attributeReleases = storage.readAttributeReleases(userId, relyingPartyId);
        Assert.assertEquals(attributeReleases.size(), 1);
        Assert.assertEquals(attributeReleases.get(0).getAttributeId(), attributeRelease1.getAttributeId());
        Assert.assertEquals(attributeReleases.get(0).getValuesHash(), attributeRelease1.getValuesHash());
        Assert.assertEquals(attributeReleases.get(0).getDate(), attributeRelease1.getDate());

        final AttributeRelease attributeRelease2 = new AttributeRelease("id", "otherhash", new DateTime());
        storage.updateAttributeRelease(userId, relyingPartyId, attributeRelease2);

        attributeReleases = storage.readAttributeReleases(userId, relyingPartyId);
        Assert.assertEquals(attributeReleases.size(), 1);
        Assert.assertEquals(attributeReleases.get(0).getValuesHash(), attributeRelease2.getValuesHash());

        storage.deleteAttributeReleases(userId, relyingPartyId);
        Assert.assertFalse(storage.containsAttributeReleases(userId, relyingPartyId));
        Assert.assertTrue(storage.readAttributeReleases(userId, relyingPartyId).isEmpty());
    }
}
