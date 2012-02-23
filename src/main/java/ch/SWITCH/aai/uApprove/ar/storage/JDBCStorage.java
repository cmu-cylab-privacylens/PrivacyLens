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
    public void createAttributeReleaseConsent(final String userId, final String relyingPartyId,
            final AttributeReleaseConsent attributeReleaseConsent) {
        try {
            getJdbcTemplate().update(getSqlStatements().getProperty("createAttributeReleaseConsent"), userId,
                    relyingPartyId, attributeReleaseConsent.getAttributeId(), attributeReleaseConsent.getValuesHash(),
                    attributeReleaseConsent.getDate().toDate());
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
        }

    }

    /** {@inheritDoc} */
    public List<AttributeReleaseConsent> readAttributeReleaseConsents(final String userId, final String relyingPartyId) {
        try {
            return getJdbcTemplate().query(getSqlStatements().getProperty("readAttributeReleaseConsents"),
                    attributeReleaseConsentMapper, userId, relyingPartyId);
        } catch (final EmptyResultDataAccessException e) {
            return Collections.emptyList();
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
            return Collections.emptyList();
        }
    }

    /** {@inheritDoc} */
    public void updateAttributeReleaseConsent(final String userId, final String relyingPartyId,
            final AttributeReleaseConsent attributeReleaseConsent) {
        try {
            getJdbcTemplate().update(getSqlStatements().getProperty("updateAttributeReleaseConsent"),
                    attributeReleaseConsent.getValuesHash(), attributeReleaseConsent.getDate().toDate(), userId,
                    relyingPartyId, attributeReleaseConsent.getAttributeId());
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
        }
    }

    /** {@inheritDoc} */
    public void deleteAttributeReleaseConsents(final String userId, final String relyingPartyId) {
        try {
            getJdbcTemplate().update(getSqlStatements().getProperty("deleteAttributeReleaseConsents"), userId,
                    relyingPartyId);
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
        }
    }

    /** {@inheritDoc} */
    public boolean containsAttributeReleaseConsent(final String userId, final String relyingPartyId,
            final String attributeId) {
        try {
            return getJdbcTemplate().queryForInt(getSqlStatements().getProperty("containsAttributeReleaseConsent"),
                    userId, relyingPartyId, attributeId) > 0;
        } catch (final DataAccessException e) {
            handleDataAccessException(e);
            return false;
        }
    }
}
