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

@ContextConfiguration(locations = {"classpath:/PrivacyLens-test.xml"})
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
        super.executeSqlScript("classpath:/storage/terms-of-use-schema.sql", false);
    }

    /** Test. */
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
