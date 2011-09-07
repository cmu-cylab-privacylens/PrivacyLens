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

package ch.SWITCH.aai.uApprove.tou.storage;

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

import ch.SWITCH.aai.uApprove.tou.ToUAcceptance;

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
        super.executeSqlScript("classpath:/storage/terms-of-use-schema.sql", false);
    }

    @Test
    public void crudToUAcceptance() {

        final String userId = "student1";
        final String version = "1.0";
        final String fingerprint = "5b2ee897c08c79a09cd57e8602d605bf8c52db17de9793677c36b5c78644b2b2";
        final DateTime acceptanceDate = new DateTime("2011-11-11T11:11:11");

        Assert.assertFalse(storage.containsToUAcceptance(userId, version));
        Assert.assertNull(storage.readToUAcceptance(userId, version));

        ToUAcceptance touAcceptance = new ToUAcceptance(version, fingerprint, acceptanceDate);
        storage.createToUAcceptance(userId, touAcceptance);
        Assert.assertTrue(storage.containsToUAcceptance(userId, version));

        touAcceptance = storage.readToUAcceptance(userId, version);

        Assert.assertEquals(version, touAcceptance.getVersion());
        Assert.assertEquals(fingerprint, touAcceptance.getFingerprint());
        Assert.assertEquals(acceptanceDate, touAcceptance.getAcceptanceDate());

        touAcceptance = new ToUAcceptance(version, fingerprint.substring(1), acceptanceDate.plusMonths(1));
        storage.updateToUAcceptance(userId, touAcceptance);

        touAcceptance = storage.readToUAcceptance(userId, version);
        Assert.assertEquals(version, touAcceptance.getVersion());
        Assert.assertEquals(fingerprint.substring(1), touAcceptance.getFingerprint());
        Assert.assertEquals(acceptanceDate.plusMonths(1), touAcceptance.getAcceptanceDate());
    }
}
