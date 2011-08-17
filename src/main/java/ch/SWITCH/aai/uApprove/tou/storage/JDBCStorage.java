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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.util.Assert;

import ch.SWITCH.aai.uApprove.UApproveException;
import ch.SWITCH.aai.uApprove.tou.ToUAcceptance;

/** JDBC implementation. */
public class JDBCStorage implements Storage {

    /** The JDBC template. */
    private SimpleJdbcTemplate jdbcTemplate;

    /** The SQL Statements. */
    private Properties sqlStatements;

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
    private final ToUAcceptanceMapper touAcceptanceMapper = new ToUAcceptanceMapper();

    public void setDataSource(final DataSource dataSource) {
        jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }

    public void setSqlStatements(final Resource sqlStamentsResource) throws UApproveException {
        Assert.notNull(sqlStamentsResource, "SQL statements are not set.");
        this.sqlStatements = new Properties();
        try {
            this.sqlStatements.load(sqlStamentsResource.getInputStream());
        } catch (final IOException e) {
            throw new UApproveException("Error reading SQL statements from resource "
                    + sqlStamentsResource.getDescription(), e);
        }
    }

    /** {@inheritDoc} */
    public void createToUAcceptance(final String userId, final ToUAcceptance touAcceptance) {
        jdbcTemplate.update(sqlStatements.getProperty("tou.createToUAcceptance"), userId, touAcceptance.getVersion(),
                touAcceptance.getFingerprint(), touAcceptance.getAcceptanceDate().toDate());
    }

    /** {@inheritDoc} */
    public void updateToUAcceptance(final String userId, final ToUAcceptance touAcceptance) {
        jdbcTemplate.update(sqlStatements.getProperty("tou.updateToUAcceptance"), touAcceptance.getFingerprint(),
                touAcceptance.getAcceptanceDate().toDate(), userId, touAcceptance.getVersion());
    }

    /** {@inheritDoc} */
    public ToUAcceptance readToUAcceptance(final String userId, final String version) {
        try {
            return jdbcTemplate.queryForObject(sqlStatements.getProperty("tou.readToUAcceptance"), touAcceptanceMapper,
                    userId, version);
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    /** {@inheritDoc} */
    public boolean containsToUAcceptance(final String userId, final String version) {
        return jdbcTemplate.queryForInt(sqlStatements.getProperty("tou.containsToUAcceptance"), userId, version) > 0;
    }
}
