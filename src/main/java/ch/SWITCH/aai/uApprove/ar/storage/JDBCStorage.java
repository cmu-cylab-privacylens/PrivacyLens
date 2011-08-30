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
import ch.SWITCH.aai.uApprove.ar.AttributeRelease;

/** JDBC implementation. */
public class JDBCStorage extends AbstractJDBCStorage implements Storage {

    /** Class logger. */
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(JDBCStorage.class);

    /** {@see AttributeRelease} row mapper. */
    private static final class AttributeReleaseMapper implements ParameterizedRowMapper<AttributeRelease> {
        @Override
        public AttributeRelease mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            return new AttributeRelease(rs.getString("attributeId"), rs.getString("valuesHash"), new DateTime(
                    rs.getTimestamp("consentDate")));
        }
    }

    /** The attribute release mapper. */
    private final AttributeReleaseMapper attributeReleaseMapper;

    /** Default constructor. */
    public JDBCStorage() {
        super();
        attributeReleaseMapper = new AttributeReleaseMapper();
    }

    /** {@inheritDoc} */
    public List<AttributeRelease> readAttributeReleases(final String userId, final String relyingPartyId) {
        try {
            return jdbcTemplate.query(sqlStatements.getProperty("ar.readAttributeReleases"), attributeReleaseMapper,
                    userId, relyingPartyId);
        } catch (final EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    /** {@inheritDoc} */
    public void deleteAttributeReleases(final String userId, final String relyingPartyId) {
        jdbcTemplate.update(sqlStatements.getProperty("ar.deleteAttributeReleases"), userId, relyingPartyId);
    }

    /** {@inheritDoc} */
    public boolean containsAttributeReleases(final String userId, final String relyingPartyId) {
        return jdbcTemplate.queryForInt(sqlStatements.getProperty("ar.containsAttributeReleases"), userId,
                relyingPartyId) > 0;
    }

    /** {@inheritDoc} */
    public void updateAttributeRelease(final String userId, final String relyingPartyId,
            final AttributeRelease attributeRelease) {
        jdbcTemplate.update(sqlStatements.getProperty("ar.updateAttributeRelease"), attributeRelease.getValuesHash(),
                attributeRelease.getDate().toDate(), userId, relyingPartyId, attributeRelease.getAttributeId());
    }

    /** {@inheritDoc} */
    public void createAttributeRelease(final String userId, final String relyingPartyId,
            final AttributeRelease attributeRelease) {
        jdbcTemplate.update(sqlStatements.getProperty("ar.createAttributeRelease"), userId, relyingPartyId,
                attributeRelease.getAttributeId(), attributeRelease.getValuesHash(), attributeRelease.getDate()
                        .toDate());
    }
}
