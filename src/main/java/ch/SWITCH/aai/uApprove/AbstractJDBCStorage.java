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

package ch.SWITCH.aai.uApprove;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Abstract JDBC Storage.
 */
public abstract class AbstractJDBCStorage {

    /** Class logger. */
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(AbstractJDBCStorage.class);

    /** The SQL Statements. */
    private Properties sqlStatements;

    /** The JDBC template. */
    private SimpleJdbcTemplate jdbcTemplate;

    /**
     * Sets the data source.
     * 
     * @param dataSource The datasource.
     */
    public void setDataSource(final DataSource dataSource) {
        jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }

    public void setSqlStatements(final Resource sqlStamentsResource) {
        sqlStatements = new Properties();
        try {
            sqlStatements.load(sqlStamentsResource.getInputStream());
        } catch (final IOException e) {
            throw new UApproveException("Error reading SQL statements from resource "
                    + sqlStamentsResource.getDescription(), e);
        }
    }

    /**
     * @return Returns the sqlStatements.
     */
    protected Properties getSqlStatements() {
        return sqlStatements;
    }

    /**
     * @return Returns the jdbcTemplate.
     */
    protected SimpleJdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void initialize() {
        Validate.notNull(jdbcTemplate, "Datasource is not set.");
        Validate.notEmpty(sqlStatements, "SQL statements are not set.");
    }
}
