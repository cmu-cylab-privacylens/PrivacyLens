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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import ch.SWITCH.aai.uApprove.AbstractJDBCStorage;
import ch.SWITCH.aai.uApprove.tou.ToUAcceptance;

/** JDBC implementation. */
public class JDBCStorage extends AbstractJDBCStorage implements Storage {

    /** Class logger. */
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(JDBCStorage.class);

    /** {@see ToUAcceptance} row mapper. */
    private static final class ToUAcceptanceMapper implements ParameterizedRowMapper<ToUAcceptance> {
        public ToUAcceptance mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final ToUAcceptance touAcceptance =
                    new ToUAcceptance(rs.getString("version"), rs.getString("fingerprint"), new DateTime(
                            rs.getTimestamp("acceptanceDate")));
            return touAcceptance;
        }
    }

    /** The terms of use acceptance mapper. */
    private final ToUAcceptanceMapper touAcceptanceMapper;

    /** Default constructor. */
    public JDBCStorage() {
        super();
        touAcceptanceMapper = new ToUAcceptanceMapper();
    }

    /** {@inheritDoc} */
    public void createToUAcceptance(final String userId, final ToUAcceptance touAcceptance) {
        getJdbcTemplate().update(getSqlStatements().getProperty("createToUAcceptance"), userId,
                touAcceptance.getVersion(), touAcceptance.getFingerprint(), touAcceptance.getAcceptanceDate().toDate());
    }

    /** {@inheritDoc} */
    public void updateToUAcceptance(final String userId, final ToUAcceptance touAcceptance) {
        getJdbcTemplate().update(getSqlStatements().getProperty("updateToUAcceptance"), touAcceptance.getFingerprint(),
                touAcceptance.getAcceptanceDate().toDate(), userId, touAcceptance.getVersion());
    }

    /** {@inheritDoc} */
    public ToUAcceptance readToUAcceptance(final String userId, final String version) {
        try {
            return getJdbcTemplate().queryForObject(getSqlStatements().getProperty("readToUAcceptance"),
                    touAcceptanceMapper, userId, version);
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    /** {@inheritDoc} */
    public boolean containsToUAcceptance(final String userId, final String version) {
        return getJdbcTemplate().queryForInt(getSqlStatements().getProperty("containsToUAcceptance"), userId, version) > 0;
    }
}
