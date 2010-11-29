package ch.SWITCH.aai.uApprove.storage;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Class JdbcConnectionFactory
 *
 * Original code (myJdbcInterface) copyright and written by SWITCH, C.Witzig.
 */
public class JdbcConnectionFactory {

    public static final int VALIDATE_TIMEOUT = 1;
    public static final int WAIT_FOR_CONNECTION_TIMEOUT = 3;
    // / the configfile, where we get the connection parameters
    private String myConfigFile;
    // / jndi resource name
    private String jndiResourceName;
    // / driver class name
    private String myDriverClassName;
    // / other properties
    private Properties myProperties;
    // / data source
    private DataSource dataSource;
    // / connection pool
    private BoneCP boneCP;
    // sql commands
    String sqlCommands;
    // / flag whether the config file could be read properly
    boolean bConfigFileRead = false;
    private static Logger LOG = LoggerFactory.getLogger(JdbcConnectionFactory.class);

    /**
     * Class that defines the property keys in the property file. Note: if you
     * change the keys in the corresponding property file, you must also change
     * them here!
     */
    public class myJdbcConfigurationKeys {

        public static final String keyDriver = "driver";
        public static final String keyUrl = "url";
        public static final String keyUser = "user";
        public static final String keyPassword = "password";
        public static final String keyResourceName = "resourceName";
        public static final String keySqlCmds = "sqlCommands";
    }

    /*------------------------------------------------------------------------
    public member functions
    ------------------------------------------------------------------------*/
    /**
     * creator
     *
     * @param the
     *          configfile with the jdbc connection parameters
     */
    public JdbcConnectionFactory(String theConfigFile) {
        myConfigFile = theConfigFile;
        readConfigFile();
        if (jndiResourceName != null && jndiResourceName.length() > 0) {
            try {
                LOG.info("Using JNDI resource");
                Context initCtx = new InitialContext();
                Context envCtx = (Context) initCtx.lookup("java:comp/env");
                dataSource = (DataSource) envCtx.lookup(jndiResourceName);
            } catch (NamingException e) {
                throw new IllegalStateException("Cannot retrieve DataSource", e);
            }
        } else {
            try {
                Class.forName(myDriverClassName);

                // connection pooling default properties
                Properties boneCPDefaultProperties = new Properties();
                boneCPDefaultProperties.put("minConnectionsPerPartition", "1");
                boneCPDefaultProperties.put("maxConnectionsPerPartition", "10");
                boneCPDefaultProperties.put("acquireIncrement", "1");
                
                //merge bonecp defaults with local configuration
                Properties boneCPProperties = new Properties();
                boneCPProperties.putAll(boneCPDefaultProperties);
                boneCPProperties.putAll(myProperties);

                BoneCPConfig cfg = new BoneCPConfig(boneCPProperties);
                boneCP = new BoneCP(cfg);
            } catch (Exception e) {
                throw new IllegalStateException("Cannot initialize connection pool", e);
            }
        }
    }

    public String getSqlCmds() {
        return sqlCommands;
    }

    public Connection createConnection() throws SQLException {
        if (dataSource != null) {
            //with jndi, it's very easy...
            Connection c = dataSource.getConnection();
            c.setReadOnly(false);
            return c;
        } else {
            try {
                Connection conn = null;
                boolean validated = false;
                do {
                    //beg for a connection
                    Future<Connection> asyncConnection = boneCP.getAsyncConnection();
                    conn = asyncConnection.get(WAIT_FOR_CONNECTION_TIMEOUT, TimeUnit.SECONDS);
                    conn.setReadOnly(false);
                    try {
                        //and try to validate it
                        validated = conn.isValid(VALIDATE_TIMEOUT);
                        if (!validated) {
                        	LOG.debug("Connection is invalid, dropping it");
                            closeConnection(conn);
                        }
                    } catch (AbstractMethodError e) {
                    	LOG.debug("Underlying database don't support Connection.isValid()");
                    	break;
                    } catch (SQLException ignored) {
                    	LOG.warn("Caught Connection.isValid() exception, but will continue");
                    	break;                    	
                    }
                } while (!validated);
                return conn;
            } catch (ExecutionException e) {
                throw new SQLException("Unable to get Connection from the pool", "08001", e);
            } catch (InterruptedException e) {
                throw new SQLException("Unable to get Connection from the pool", "08001", e);
            } catch (TimeoutException e) {
                throw new SQLException("Unable to get Connection from the pool", "08001", e);
            }
        }
    }

    /*------------------------------------------------------------------------
    private member functions
    ------------------------------------------------------------------------*/
    /**
     * reads the config file
     *
     * @return true in case of success, false otherwise
     */
    private void readConfigFile() {

        File theFile = new File(myConfigFile);
        if (!theFile.exists() || !theFile.isFile() || !theFile.canRead()) {
            throw new IllegalStateException("Cannot load config file");
        }
        Properties theProperties = new Properties();
        InputStream configStream = null;
        try {
            configStream = new FileInputStream(theFile);
            theProperties.load(configStream);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load config file", e);
        } finally {
            if (configStream != null) {
                try {
                    configStream.close();
                } catch (IOException e) {
                    LOG.warn("Cannot close config file after properties read", e);
                }
            }
        }
        LOG.debug("Read file successfully");
        sqlCommands = theProperties.getProperty(myJdbcConfigurationKeys.keySqlCmds).trim();

        jndiResourceName = theProperties.getProperty(myJdbcConfigurationKeys.keyResourceName);
        if (jndiResourceName != null) {
            jndiResourceName = jndiResourceName.trim();
            LOG.debug("JNDI resource name = {}", jndiResourceName);
        } else {
            myDriverClassName = theProperties.getProperty(myJdbcConfigurationKeys.keyDriver).trim();

            //transform url and username properties for BoneCP
            String myUrl = theProperties.getProperty(myJdbcConfigurationKeys.keyUrl).trim();
            theProperties.remove(myJdbcConfigurationKeys.keyUrl);
            theProperties.put("jdbcUrl", myUrl);

            String myUser = theProperties.getProperty(myJdbcConfigurationKeys.keyUser).trim();
            theProperties.remove(myJdbcConfigurationKeys.keyUser);
            theProperties.put("username", myUser);

            LOG.debug("driver = " + myDriverClassName + " url = " + myUrl + " user = " + myUser);

            //these properties are no longer needed
            theProperties.remove(myJdbcConfigurationKeys.keyDriver);
            theProperties.remove(myJdbcConfigurationKeys.keyResourceName);
            theProperties.remove(myJdbcConfigurationKeys.keySqlCmds);

            //bonecp will read remaining properties
            myProperties = theProperties;
        }
    }

    /**
     * tests the database connection
     *
     * @return true in case of success, false otherwise
     */
    public boolean testConnection() {
        Connection testConnection = null;
        try {
            LOG.debug("Trying to test connection");
            testConnection = createConnection();
            if (testConnection != null) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            LOG.error("Cannot test connection", e);
            return false;
        } finally {
            closeConnection(testConnection);
        }
    }

    public static void closeResources(ResultSet rs, Statement stmt) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                LOG.warn("Exception caught while trying to close resultset", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                LOG.warn("Exception caught while trying to close statement", e);
            }
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LOG.warn("Exception caught while trying to close connection", e);
            }
        }

    }
}
