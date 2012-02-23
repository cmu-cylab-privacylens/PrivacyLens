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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
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
        /** {@inheritDoc} */
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
        try {
            getJdbcTemplate().update(getSqlStatements().getProperty("createToUAcceptance"), userId,
                    touAcceptance.getVersion(), touAcceptance.getFingerprint(),
                    touAcceptance.getAcceptanceDate().toDate());
        } catch (final RecoverableDataAccessException e) {
            handleRecoverableDataAccessException(e);
        }
    }

    /** {@inheritDoc} */
    public ToUAcceptance readToUAcceptance(final String userId, final String version) {
        try {
            return getJdbcTemplate().queryForObject(getSqlStatements().getProperty("readToUAcceptance"),
                    touAcceptanceMapper, userId, version);
        } catch (final EmptyResultDataAccessException e) {
            return null;
        } catch (final RecoverableDataAccessException e) {
            handleRecoverableDataAccessException(e);
            return null;
        }
    }

    /** {@inheritDoc} */
    public void updateToUAcceptance(final String userId, final ToUAcceptance touAcceptance) {
        try {
            getJdbcTemplate().update(getSqlStatements().getProperty("updateToUAcceptance"),
                    touAcceptance.getFingerprint(), touAcceptance.getAcceptanceDate().toDate(), userId,
                    touAcceptance.getVersion());
        } catch (final RecoverableDataAccessException e) {
            handleRecoverableDataAccessException(e);
        }
    }

    /** {@inheritDoc} */
    public boolean containsToUAcceptance(final String userId, final String version) {
        try {
            return getJdbcTemplate().queryForInt(getSqlStatements().getProperty("containsToUAcceptance"), userId,
                    version) > 0;
        } catch (final RecoverableDataAccessException e) {
            handleRecoverableDataAccessException(e);
            return false;
        }
    }
}
