/*
 * Copyright (c) 2011, SWITCH
 * Copyright (c) 2013, Carnegie Mellon University
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

package edu.cmu.ece.PrivacyLens.ar.storage;

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

import edu.cmu.ece.PrivacyLens.StrouckiUtils;
import edu.cmu.ece.PrivacyLens.ar.Attribute;
import edu.cmu.ece.PrivacyLens.ar.AttributeReleaseChoice;
import edu.cmu.ece.PrivacyLens.ar.LoginEvent;
import edu.cmu.ece.PrivacyLens.ar.LoginEventDetail;
import edu.cmu.ece.PrivacyLens.ar.ReminderInterval;
import edu.cmu.ece.PrivacyLens.ar.storage.Storage;

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
        super.executeSqlScript("classpath:/storage/attribute-release-schema.sql", false);
    }

    /** Test. */
    @Test
    public void crudAttributeReleaseConsent() {
        final String userId = "userId";
        final String relyingPartyId = "relyingPartyId";
        final Attribute attribute = new Attribute("id", Arrays.asList(new String[] {"value1", "value2"}));
        final AttributeReleaseChoice attributeRelease1 = new AttributeReleaseChoice(attribute, new DateTime(), true);

        Assert.assertFalse(storage.containsAttributeReleaseChoice(userId, relyingPartyId, attribute.getId()));

        storage.createAttributeReleaseChoice(userId, relyingPartyId, attributeRelease1);
        Assert.assertTrue(storage.containsAttributeReleaseChoice(userId, relyingPartyId, attribute.getId()));

        List<AttributeReleaseChoice> attributeReleases = storage.readAttributeReleaseChoices(userId, relyingPartyId);
        Assert.assertEquals(attributeReleases.size(), 1);
        Assert.assertEquals(attributeReleases.get(0).getAttributeId(), attributeRelease1.getAttributeId());
        Assert.assertEquals(attributeReleases.get(0).getValuesHash(), attributeRelease1.getValuesHash());
        Assert.assertEquals(attributeReleases.get(0).getDate(), attributeRelease1.getDate());
        Assert.assertEquals(attributeReleases.get(0).isConsented(), attributeRelease1.isConsented());

        final AttributeReleaseChoice attributeRelease2 =
                new AttributeReleaseChoice("id", "otherhash", new DateTime(), false);
        storage.updateAttributeReleaseChoice(userId, relyingPartyId, attributeRelease2);

        attributeReleases = storage.readAttributeReleaseChoices(userId, relyingPartyId);
        Assert.assertEquals(attributeReleases.size(), 1);
        Assert.assertEquals(attributeReleases.get(0).getValuesHash(), attributeRelease2.getValuesHash());
        Assert.assertEquals(attributeReleases.get(0).isConsented(), attributeRelease2.isConsented());

        storage.deleteAttributeReleaseChoices(userId, relyingPartyId);
        Assert.assertFalse(storage.containsAttributeReleaseChoice(userId, relyingPartyId, attribute.getId()));
        Assert.assertTrue(storage.readAttributeReleaseChoices(userId, relyingPartyId).isEmpty());

    }

    /** Test. */
    @Test
    public void crudForceShowInterface() {
        // new state
        final String userId = "userId";
        final String relyingPartyId = "relyingPartyId";
        final boolean isForceShow = StrouckiUtils.getRandomBoolean();

        storage.createForceShowInterface(userId, relyingPartyId, isForceShow);
        Assert.assertTrue(storage.readForceShowInterface(userId, relyingPartyId) == isForceShow);

        storage.updateForceShowInterface(userId, relyingPartyId, !isForceShow);
        Assert.assertFalse(storage.readForceShowInterface(userId, relyingPartyId) == isForceShow);

        // deletion 
    }

    /** Test. */
    @Test
    public void crudReminderInterval() {
        // new state
        final String userId = "userId";
        final String relyingPartyId = "relyingPartyId";
        final int currentCount = StrouckiUtils.getRandomRange(1, 10);
        final int remindAfter = StrouckiUtils.getRandomRange(1, 10);

        final ReminderInterval reminderInterval =
                new ReminderInterval(userId, relyingPartyId, remindAfter, currentCount);

        storage.createReminderInterval(reminderInterval);
        ReminderInterval blorf = storage.readReminderInterval(userId, relyingPartyId);
        Assert.assertEquals(blorf.getRemindAfter(), remindAfter);
        Assert.assertEquals(blorf.getCurrentCount(), currentCount);

        reminderInterval.setCurrentCount(currentCount + 1);
        storage.updateReminderInterval(reminderInterval);

        blorf = storage.readReminderInterval(userId, relyingPartyId);
        Assert.assertEquals(blorf.getRemindAfter(), remindAfter);
        Assert.assertEquals(blorf.getCurrentCount(), currentCount + 1);

        // deletion
    }

    @Test
    public void testLoginEventOperations() {
        final String userId = "userId";
        final String serviceName = "serviceName";
        final String serviceUrl = "serviceUrl";
        final DateTime eventDate = new DateTime(System.currentTimeMillis());
        final int limit = 2;
        final Attribute attribute1 = new Attribute("id", Arrays.asList(new String[] {"value1", "value2"}));
        final Attribute attribute2 = new Attribute("foo", Arrays.asList(new String[] {"bar", "baz"}));
        final List<Attribute> attributes = Arrays.asList(new Attribute[] {attribute1, attribute2});

        final LoginEvent loginEvent = new LoginEvent(userId, serviceName, serviceUrl, eventDate);
        final LoginEventDetail loginEventDetail = new LoginEventDetail(loginEvent.getEventDetailHash(), attributes);

        // database should be empty
        Assert.assertTrue(storage.listLoginEvents(userId, serviceName, limit).isEmpty());

        storage.createLoginEvent(loginEvent, loginEventDetail);
        // now should have one entry
        final List<LoginEvent> test1 = storage.listLoginEvents(userId, serviceName, limit);
        Assert.assertTrue(test1.size() == 1);

        // we should be able to retrieve that entry
        final LoginEvent foo = storage.readLoginEvent(loginEvent.getEventDetailHash());
        logger.error("foobar {} {}", loginEvent, foo);
        Assert.assertTrue(foo.equals(loginEvent));

        // The attributes should be the same we put in
        final LoginEventDetail test2 = storage.readLoginEventDetail(loginEvent);
        Assert.assertTrue(test2.getEventDetailHash().equals(loginEvent.getEventDetailHash()));
        Assert.assertTrue(test2.getAttributes().equals(loginEventDetail.getAttributes()));

        // should be one SP
        final List<String> test3 = storage.listRelyingParties(userId, limit);
        Assert.assertTrue(test3.size() == 1);

        storage.deleteLoginEvent(loginEvent);
        // now database should be empty again
        Assert.assertTrue(storage.listLoginEvents(userId, serviceName, limit).isEmpty());

        // XXX debug code
        //cheap way for me to generate the oracle database
        /*
        final List<Map> attrs = new ArrayList<Map>();
        Map<String, String> attrmap = new HashMap<String, String>();
        attrmap.put("id", "eduPersonPrincipalName");
        attrmap.put("desc", "Andrew ID");
        attrs.add(attrmap);
        attrmap = new HashMap<String, String>();
        attrmap.put("id", "eduPersonPrimaryAffiliation");
        attrmap.put("desc", "CMU affiliation");
        attrs.add(attrmap);
        attrmap = new HashMap<String, String>();
        attrmap.put("id", "eduPersonEntitlement");
        attrmap.put("desc", "credentials to access CMU services");
        attrs.add(attrmap);
        attrmap = new HashMap<String, String>();
        attrmap.put("id", "cn");
        attrmap.put("desc", "full name");
        attrs.add(attrmap);
        attrmap = new HashMap<String, String>();
        attrmap.put("id", "sn");
        attrmap.put("desc", "surname");
        attrs.add(attrmap);
        
        final List<Map> top = new ArrayList<Map>();
        final List<Map> sps = new ArrayList<Map>();
        final Map<String, Object> spmap = new HashMap<String, Object>();
        spmap.put("name", "CMU's Calendar");
        spmap.put("id", "https://scalepriv.ece.cmu.edu/shibboleth");
        final List<Map> spattrs = new ArrayList<Map>();
        attrmap = new HashMap<String, String>();
        attrmap.put("id", "eduPersonPrincipalName");
        attrmap.put(
                "reason",
                "CMU's Calendar needs your <b>Andrew ID</b> in order to display the correct calendar information. CMU's Calendar cannot function properly if an Andrew ID is not supplied.");
        attrmap.put(
                "privpolicy",
                "CMU's Calendar will not use your Andrew ID for any other purpose, and will not keep this information after you close the calendar window.");
        attrmap.put("required", "true");
        spattrs.add(attrmap);
        attrmap = new HashMap<String, String>();
        attrmap.put("id", "eduPersonEntitlement");
        attrmap.put(
                "reason",
                "CMU's Calendar is asking for your <b>credentials to access CMU services</b>. This information is not necessary for CMU's Calendar to function, and CMU does not know whether or how it will be used.");
        attrmap.put("privpolicy", "CMU does not know how long CMU's Calendar will keep this information.");
        spattrs.add(attrmap);
        attrmap = new HashMap<String, String>();
        attrmap.put("id", "eduPersonPrimaryAffiliation");
        attrmap.put(
                "reason",
                "CMU's Calendar is asking for your <b>CMU affiliation</b>. This information is not necessary for CMU's Calendar to function, and CMU does not know whether or how it will be used.");
        attrmap.put("privpolicy", "CMU does not know how long CMU's Calendar will keep this information.");
        spattrs.add(attrmap);
        attrmap = new HashMap<String, String>();
        attrmap.put("id", "cn");
        attrmap.put("reason",
                "CMU's Calendar is asking for your <b>full name</b> in order to personalize your calendar.");
        attrmap.put(
                "privpolicy",
                "CMU's Calendar will not use your full name for any other purpose, and will not keep this information after you close the calendar window.");
        spattrs.add(attrmap);
        spmap.put("attrs", spattrs);
        attrmap = new HashMap<String, String>();
        attrmap.put("id", "sn");
        attrmap.put("reason",
                "CMU's Calendar is asking for your <b>surname</b> in order to personalize your calendar.");
        attrmap.put(
                "privpolicy",
                "CMU's Calendar will not use your surname for any other purpose, and will not keep this information after you close the calendar window.");
        spattrs.add(attrmap);
        spmap.put("attrs", spattrs);
        sps.add(spmap);

        final Map<String, Object> topmap = new HashMap<String, Object>();
        //topmap.put("IdPname", "CMU");
        //topmap.put("AdminUrl", "https://scalepriv-idp.ece.cmu.edu/idp/uApprove/AdminServlet");
        topmap.put("SPs", sps);
        topmap.put("attrs", attrs);

        try {
            final PrintWriter pw = new PrintWriter("/tmp/json", "UTF-8");
            pw.println(new Gson().toJson(topmap));
            pw.close();
        } catch (final Exception x) {
            Assert.assertEquals(true, false);
        }
        */
    }
}
