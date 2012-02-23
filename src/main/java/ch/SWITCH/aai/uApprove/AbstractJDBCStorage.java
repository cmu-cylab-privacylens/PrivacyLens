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

package ch.SWITCH.aai.uApprove;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Abstract JDBC Storage.
 */
public abstract class AbstractJDBCStorage {

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(AbstractJDBCStorage.class);

    /** The SQL Statements. */
    private Properties sqlStatements;

    /** The JDBC template. */
    private SimpleJdbcTemplate jdbcTemplate;

    /** Whether JDBC connections should be handled graceful. */
    private boolean isGraceful;

    /** Default constructor. */
    protected AbstractJDBCStorage() {
        isGraceful = false;
    }

    /**
     * Sets the data source.
     * 
     * @param dataSource The datasource.
     */
    public void setDataSource(final DataSource dataSource) {
        jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }

    /**
     * Sets the sql statements.
     * 
     * @param sqlStamentsResource the sql statements properties resource.
     */
    public void setSqlStatements(final Resource sqlStamentsResource) {
        sqlStatements = new Properties();
        try {
            sqlStatements.load(sqlStamentsResource.getInputStream());
        } catch (final IOException e) {
            logger.error("Error reading SQL statements from resource {}.", sqlStamentsResource.getDescription(), e);
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Sets whether is graceful or not.
     * 
     * @param isGraceful The graceful to set.
     */
    public void setGraceful(final boolean isGraceful) {
        this.isGraceful = isGraceful;
    }

    /**
     * Gets the sql statements.
     * 
     * @return Returns the sqlStatements.
     */
    protected Properties getSqlStatements() {
        return sqlStatements;
    }

    /**
     * Gets the JDBC template.
     * 
     * @return Returns the jdbcTemplate.
     */
    protected SimpleJdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    /**
     * Initializes the JDBC storage.
     */
    public void initialize() {
        Validate.notNull(jdbcTemplate, "Datasource is not set.");
        Validate.notEmpty(sqlStatements, "SQL statements are not set.");
    }

    /**
     * Handles recoverable exceptions dependent of being graceful or not.
     * 
     * @param e The exception.
     */
    protected void handleRecoverableDataAccessException(final RecoverableDataAccessException e) {
        if (isGraceful) {
            logger.warn("Storage issue.", e);
        } else {
            throw e;
        }
    }
}
