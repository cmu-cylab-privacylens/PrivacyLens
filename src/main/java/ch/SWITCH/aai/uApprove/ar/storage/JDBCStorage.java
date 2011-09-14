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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import ch.SWITCH.aai.uApprove.AbstractJDBCStorage;
import ch.SWITCH.aai.uApprove.ar.AttributeReleaseConsent;

/** JDBC implementation. */
public class JDBCStorage extends AbstractJDBCStorage implements Storage {

    /** Class logger. */
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(JDBCStorage.class);

    /** {@see AttributeReleaseConsent} row mapper. */
    private static final class AttributeReleaseConsentMapper implements ParameterizedRowMapper<AttributeReleaseConsent> {
        /** {@inheritDoc} */
        public AttributeReleaseConsent mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            return new AttributeReleaseConsent(rs.getString("attributeId"), rs.getString("valuesHash"), new DateTime(
                    rs.getTimestamp("consentDate")));
        }
    }

    /** The attribute release consent mapper. */
    private final AttributeReleaseConsentMapper attributeReleaseConsentMapper;

    /** Default constructor. */
    public JDBCStorage() {
        super();
        attributeReleaseConsentMapper = new AttributeReleaseConsentMapper();
    }

    /** {@inheritDoc} */
    public List<AttributeReleaseConsent> readAttributeReleaseConsents(final String userId, final String relyingPartyId) {
        try {
            return getJdbcTemplate().query(getSqlStatements().getProperty("readAttributeReleaseConsents"),
                    attributeReleaseConsentMapper, userId, relyingPartyId);
        } catch (final EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    /** {@inheritDoc} */
    public void deleteAttributeReleaseConsents(final String userId, final String relyingPartyId) {
        getJdbcTemplate().update(getSqlStatements().getProperty("deleteAttributeReleaseConsents"), userId,
                relyingPartyId);
    }

    /** {@inheritDoc} */
    public boolean containsAttributeReleaseConsent(final String userId, final String relyingPartyId,
            final String attributeId) {
        return getJdbcTemplate().queryForInt(getSqlStatements().getProperty("containsAttributeReleaseConsent"), userId,
                relyingPartyId, attributeId) > 0;
    }

    /** {@inheritDoc} */
    public void updateAttributeReleaseConsent(final String userId, final String relyingPartyId,
            final AttributeReleaseConsent attributeReleaseConsent) {
        getJdbcTemplate().update(getSqlStatements().getProperty("updateAttributeReleaseConsent"),
                attributeReleaseConsent.getValuesHash(), attributeReleaseConsent.getDate().toDate(), userId,
                relyingPartyId, attributeReleaseConsent.getAttributeId());
    }

    /** {@inheritDoc} */
    public void createAttributeReleaseConsent(final String userId, final String relyingPartyId,
            final AttributeReleaseConsent attributeReleaseConsent) {
        getJdbcTemplate().update(getSqlStatements().getProperty("createAttributeReleaseConsent"), userId,
                relyingPartyId, attributeReleaseConsent.getAttributeId(), attributeReleaseConsent.getValuesHash(),
                attributeReleaseConsent.getDate().toDate());
    }
}
