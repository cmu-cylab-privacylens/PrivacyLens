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

import java.lang.reflect.Type;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.cmu.ece.PrivacyLens.AbstractJDBCStorage;
import edu.cmu.ece.PrivacyLens.ar.Attribute;
import edu.cmu.ece.PrivacyLens.ar.AttributeReleaseChoice;
import edu.cmu.ece.PrivacyLens.ar.LoginEvent;
import edu.cmu.ece.PrivacyLens.ar.LoginEventDetail;
import edu.cmu.ece.PrivacyLens.ar.ReminderInterval;

/** JDBC implementation. */
public class JDBCStorage extends AbstractJDBCStorage implements Storage {

    /** Class logger. */
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(JDBCStorage.class);

    /** {@see AttributeReleaseConsent} row mapper. */
    private static final class AttributeReleaseChoiceMapper implements ParameterizedRowMapper<AttributeReleaseChoice> {
        /** {@inheritDoc} */
        public AttributeReleaseChoice mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            return new AttributeReleaseChoice(rs.getString("attributeId"), rs.getString("valuesHash"), new DateTime(
                    rs.getTimestamp("choiceDate")), rs.getBoolean("isConsented"));
        }
    }

    /** {@see LoginEvent} row mapper. */
    private static final class LoginEventMapper implements ParameterizedRowMapper<LoginEvent> {
        /** {@inheritDoc} */
        public LoginEvent mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            return new LoginEvent(rs.getString("userId"), rs.getString("serviceName"), rs.getString("serviceUrl"),
                    new DateTime(rs.getTimestamp("eventDate")), rs.getString("eventDetailHash"));
        }
    }

    /** {@see LoginEventDetail} row mapper. */
    private static final class LoginEventDetailMapper implements ParameterizedRowMapper<LoginEventDetail> {
        /** {@inheritDoc} */
        public LoginEventDetail mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final String eventDetailHash = rs.getString("eventDetailHash");
            final Blob attributesBlob = rs.getBlob("eventDetailData");
            final byte[] attributesBytes = attributesBlob.getBytes(1, (int) attributesBlob.length());
            final String json = new String(attributesBytes);
            final Type listAttributeType = new TypeToken<List<Attribute>>() {}.getType();
            final List<Attribute> attributeList = new Gson().fromJson(json, listAttributeType);
            return new LoginEventDetail(eventDetailHash, attributeList);
        }
    }

    /** Single list of string row mapper. */
    private static final class ServiceNameStringMapper implements ParameterizedRowMapper<String> {
        /** {@inheritDoc} */
        public String mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            return rs.getString("serviceName");
        }
    }

    /** {@see ReminderInterval} row mapper. */
    private static final class ReminderIntervalMapper implements ParameterizedRowMapper<ReminderInterval> {
        /** {@inheritDoc} */
        public ReminderInterval mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            return new ReminderInterval(rs.getString("userId"), rs.getString("relyingPartyId"),
                    rs.getInt("remindAfter"), rs.getInt("currentCount"));
        }
    }

    /** The attribute release consent mapper. */
    private final AttributeReleaseChoiceMapper attributeReleaseChoiceMapper;

    private final LoginEventDetailMapper loginEventDetailMapper;

    private final LoginEventMapper loginEventMapper;

    private final ServiceNameStringMapper listOfStringMapper;

    private final ReminderIntervalMapper reminderIntervalMapper;

    /** Default constructor. */
    public JDBCStorage() {
        super();
        attributeReleaseChoiceMapper = new AttributeReleaseChoiceMapper();
        loginEventDetailMapper = new LoginEventDetailMapper();
        loginEventMapper = new LoginEventMapper();
        listOfStringMapper = new ServiceNameStringMapper();
        reminderIntervalMapper = new ReminderIntervalMapper();
    }

    /** {@inheritDoc} */
    public void createAttributeReleaseChoice(final String userId, final String relyingPartyId,
            final AttributeReleaseChoice attributeReleaseConsent) {
        try {
            getJdbcTemplate().update(getSqlStatements().getProperty("createAttributeReleaseChoice"), userId,
                    relyingPartyId, attributeReleaseConsent.getAttributeId(), attributeReleaseConsent.getValuesHash(),
                    attributeReleaseConsent.getDate().toDate());
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
        }

    }

    /** {@inheritDoc} */
    public List<AttributeReleaseChoice> readAttributeReleaseChoices(final String userId, final String relyingPartyId) {
        try {
            return getJdbcTemplate().query(getSqlStatements().getProperty("readAttributeReleaseChoices"),
                    attributeReleaseChoiceMapper, userId, relyingPartyId);
        } catch (final EmptyResultDataAccessException e) {
            return Collections.emptyList();
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
            return Collections.emptyList();
        }
    }

    /** {@inheritDoc} */
    public void updateAttributeReleaseChoice(final String userId, final String relyingPartyId,
            final AttributeReleaseChoice attributeReleaseConsent) {
        try {
            getJdbcTemplate().update(getSqlStatements().getProperty("updateAttributeReleaseChoice"),
                    attributeReleaseConsent.getValuesHash(), attributeReleaseConsent.getDate().toDate(),
                    attributeReleaseConsent.isConsented(), userId, relyingPartyId,
                    attributeReleaseConsent.getAttributeId());
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
        }
    }

    /** {@inheritDoc} */
    public void deleteAttributeReleaseChoices(final String userId, final String relyingPartyId) {
        try {
            getJdbcTemplate().update(getSqlStatements().getProperty("deleteAttributeReleaseChoices"), userId,
                    relyingPartyId);
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
        }
    }

    /** {@inheritDoc} */
    public boolean containsAttributeReleaseChoice(final String userId, final String relyingPartyId,
            final String attributeId) {
        try {
            return getJdbcTemplate().queryForInt(getSqlStatements().getProperty("containsAttributeReleaseChoice"),
                    userId, relyingPartyId, attributeId) > 0;
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
            return false;
        }
    }

    /** {@inheritDoc} */
    public void createLoginEvent(final LoginEvent loginEvent, final LoginEventDetail loginEventDetail) {
        try {
            final String userId = loginEvent.getUserId();
            final String serviceName = loginEvent.getServiceName();
            final String serviceUrl = loginEvent.getServiceUrl();
            final DateTime eventDate = loginEvent.getDate();
            final String eventDetailHash = loginEvent.getEventDetailHash();

            final List<Attribute> detail = loginEventDetail.getAttributes();
            final String detailJson = new Gson().toJson(detail);
            final byte[] detailData = detailJson.getBytes();

            getJdbcTemplate().update(getSqlStatements().getProperty("createLoginEvent"), userId, serviceName,
                    serviceUrl, eventDate.toDate(), eventDetailHash);
            getJdbcTemplate().update(getSqlStatements().getProperty("createLoginEventDetail"), eventDetailHash,
                    detailData);
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
        }

    }

    /** {@inheritDoc} */
    public LoginEvent readLoginEvent(final String loginEventId) {
        try {
            return getJdbcTemplate().queryForObject(getSqlStatements().getProperty("readLoginEvent"), loginEventMapper,
                    loginEventId);
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
            return null; // ok?
        }
    }

    /** {@inheritDoc} */
    public LoginEventDetail readLoginEventDetail(final LoginEvent loginEvent) {
        try {
            final String loginEventDetailHash = loginEvent.getEventDetailHash();
            return getJdbcTemplate().queryForObject(getSqlStatements().getProperty("readLoginEventDetail"),
                    loginEventDetailMapper, loginEventDetailHash);
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
            return null; // ok?
        }
    }

    /** {@inheritDoc} */
    public void deleteLoginEvent(final LoginEvent loginEvent) {
        try {
            final String eventDetailHash = loginEvent.getEventDetailHash();
            getJdbcTemplate().update(getSqlStatements().getProperty("deleteLoginEventDetail"), eventDetailHash);
            getJdbcTemplate().update(getSqlStatements().getProperty("deleteLoginEvent"), eventDetailHash);
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
        }
    }

    /** {@inheritDoc} */
    public List<LoginEvent> listLoginEvents(final String userId, final String serviceName, final int limit) {
        try {
            List<LoginEvent> data;
            if (serviceName.equals("")) {
                data =
                        getJdbcTemplate().query(getSqlStatements().getProperty("listLoginEvents"), loginEventMapper,
                                userId, limit);
            } else {
                data =
                        getJdbcTemplate().query(getSqlStatements().getProperty("listLoginEventsS"), loginEventMapper,
                                userId, serviceName, limit);
            }
            return data;
        } catch (final EmptyResultDataAccessException e) {
            return Collections.emptyList();
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
            return Collections.emptyList();
        }
    }

    /** {@inheritDoc} */
    public List<String> listRelyingParties(final String userId, final int limit) {
        try {
            return getJdbcTemplate().query(getSqlStatements().getProperty("listServiceNames"), listOfStringMapper,
                    userId, limit);
        } catch (final EmptyResultDataAccessException e) {
            return Collections.emptyList();
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
            return Collections.emptyList();
        }
    }

    /** {@inheritDoc} */
    public void createForceShowInterface(final String userId, final String relyingPartyId, final boolean forceShow) {
        try {
            getJdbcTemplate().update(getSqlStatements().getProperty("createForceShowInterface"), userId,
                    relyingPartyId, forceShow);
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
        }

    }

    /** {@inheritDoc} */
    public boolean readForceShowInterface(final String userId, final String relyingPartyId) {
        Boolean retval = null;
        try {
            retval =
                    getJdbcTemplate().queryForObject(getSqlStatements().getProperty("readForceShowInterface"),
                            Boolean.class, userId, relyingPartyId);
        } catch (final EmptyResultDataAccessException e) {
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
        }
        if (retval == null) {
            // my decision
            return true;
        }
        return retval.booleanValue();
    }

    /** {@inheritDoc} */
    public void updateForceShowInterface(final String userId, final String relyingPartyId, final boolean forceShow) {
        try {
            getJdbcTemplate().update(getSqlStatements().getProperty("updateForceShowInterface"), forceShow, userId,
                    relyingPartyId);
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
        }

    }

    /** {@inheritDoc} */
    public void createReminderInterval(final ReminderInterval reminderInterval) {
        try {
            final String userId = reminderInterval.getUserId();
            final String relyingPartyId = reminderInterval.getRelyingPartyId();
            final int currentCount = reminderInterval.getCurrentCount();
            final int remindAfter = reminderInterval.getRemindAfter();

            getJdbcTemplate().update(getSqlStatements().getProperty("createReminderInterval"), userId, relyingPartyId,
                    remindAfter, currentCount);
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
        }

    }

    /** {@inheritDoc} */
    public ReminderInterval readReminderInterval(final String userId, final String relyingPartyId) {
        try {

            final ReminderInterval reminderInterval =
                    getJdbcTemplate().queryForObject(getSqlStatements().getProperty("readReminderInterval"),
                            reminderIntervalMapper, userId, relyingPartyId);

            return reminderInterval;

        } catch (final DataAccessException e) {
            handleDataAccessException(e);
            return null;
        }
    }

    /** {@inheritDoc} */
    public void updateReminderInterval(final ReminderInterval reminderInterval) {
        try {
            final String userId = reminderInterval.getUserId();
            final String relyingPartyId = reminderInterval.getRelyingPartyId();
            final int currentCount = reminderInterval.getCurrentCount();
            final int remindAfter = reminderInterval.getRemindAfter();

            getJdbcTemplate().update(getSqlStatements().getProperty("updateReminderInterval"), remindAfter,
                    currentCount, userId, relyingPartyId);
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
        }
    }
}
