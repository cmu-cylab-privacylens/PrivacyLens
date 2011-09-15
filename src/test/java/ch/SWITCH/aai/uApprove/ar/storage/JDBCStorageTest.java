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
