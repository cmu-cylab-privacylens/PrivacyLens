package ch.SWITCH.aai.uApprove.storage;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Class myJdbcInterface
 * 
 * Copyright (c) 2005-2006 SWITCH - The Swiss Education & Research Network
 * 
 * Purpose: a very simple interface class to jdbc
 * 
 * This class reads jdbc connection parameters from a config file and creates a
 * connection. Then we can execute a few sql statements and close the class.
 * 
 * Note: the class is NOT threadsafe
 * 
 * @author C.Witzig
 * @date 15.1.2006
 */

public class myJdbcInterface {

  // / the configfile, where we get the connection parameters
  private String myConfigFile;

  // jdbc connection
  private Connection myConnection;

  // / jdbc statement
  private Statement myStatement;

  // / max number retries - by default 1
  private int myMaxRetries = 3;

  // / driver class name
  String myDriverClassName;

  // / jdbc connection url
  String myUrl;

  // / db user
  String myUser;

  // / db password
  String myPassword;

  // sql commands
  String sqlCommands;

  // / flag whether the config file could be read properly
  boolean bConfigFileRead = false;

  boolean bConnected = false;

  private static Logger LOG = LoggerFactory.getLogger(myJdbcInterface.class);

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

    public static final String keySqlCmds = "sqlCommands";

  }

  /*------------------------------------------------------------------------
    public member functions
    ------------------------------------------------------------------------*/

  /**
   * Creator
   * 
   * @param the
   *          config file with the jdbc connection
   */
  public myJdbcInterface(String theConfigFile) {
    myConfigFile = theConfigFile;
  }

  /**
   * creator
   * 
   * @param the
   *          configfile with the jdbc connection parameters
   * @param bDebug
   *          whether debug is true or false
   */
  public myJdbcInterface(String theConfigFile, boolean theDebug) {
    myConfigFile = theConfigFile;
    bConfigFileRead = readConfigFile();
  }

  public String getSqlCmds() {
    return sqlCommands;
  }

  /** sets the number of retries for execSQLFT */
  public void setMaxRetries(int nMaxRetries) {
    myMaxRetries = nMaxRetries;
  }

  /**
   * executes a sql statement
   * 
   * @param the
   *          sql statement as string
   * @return the result set or null in case of error
   */
  public ResultSet execSql(String theSql, boolean bIsQuery) {
    ResultSet rs = null;

    try {
      if (bIsQuery == true)
        rs = myStatement.executeQuery(theSql);
      else
        myStatement.execute(theSql);

      if (LOG.isDebugEnabled())
        LOG.debug("myJdbcInterface.execSql: executeQuery *" + theSql
            + "* done ");

    } catch (SQLException ex) {
      LOG.error("SQLException: " + ex.getMessage());
    }

    return rs;
  }

  /**
   * executes a sql statement in a fault tolerant way, i.e. at most myMaxTries
   * tries will be made
   * 
   * @param the
   *          sql statement as string
   * @return the ResultSet or null
   */
  public ResultSet execSqlFT(String theSql, boolean bIsQuery)
      throws SQLException {
    int nTries = 0;
    ResultSet rs = null;

    do {

      if (LOG.isDebugEnabled())
        LOG.debug("myJdbcInterface.execSqlFT: sql = *" + theSql + "* nTries = "
            + nTries);

      try {
        if (bIsQuery == true)
          rs = myStatement.executeQuery(theSql);
        else
          myStatement.execute(theSql);

        break;

      } catch (SQLException ex) {
        LOG
            .error("myJdbcInterface.execSqlFT: SQLException: "
                + ex.getMessage());

        close();

        initialize();

      }

    } while (nTries++ <= myMaxRetries);

    if (nTries >= myMaxRetries)
      throw new SQLException("myJdbcInterface.execSqlFT: SQLException:");
    else if (nTries > 0) {
      LOG
          .warn("myJdcInterface.execSqlFT: communication link failure due to inactive db connection successfully restored");
      LOG
          .warn("                          [possible exceptions can safely be ignored]");
    }

    return rs;
  }

  /** closes the database connection */
  public void close() {
    if (LOG.isDebugEnabled())
      LOG.debug("myJdbcInterface.close: entry");

    try {

      myStatement.close();

    } catch (SQLException ex) {
      LOG
          .error("myJdbcInterface.close: statement exception = "
              + ex.toString());
    }

    try {

      myConnection.close();

      bConnected = false;

    } catch (SQLException ex) {
      LOG.error("myJdbcInterface.close: connection exception = "
          + ex.toString());
    }

    if (LOG.isDebugEnabled())
      LOG.debug("myJdbcInterface.close: done");
  }

  /*------------------------------------------------------------------------
    private member functions
    ------------------------------------------------------------------------*/

  /**
   * reads the config file
   * 
   * @return true in case of success, false otherwise
   */
  private boolean readConfigFile() {

    try {
      File theFile = new File(myConfigFile);
      if (!theFile.exists() || !theFile.isFile() || !theFile.canRead()) {
        LOG.error("myJdbcInterface.readConfigFile: error reading file "
            + myConfigFile);
        return false;
      }
      Properties theProperties = new Properties();
      theProperties.load(new FileInputStream(theFile));

      myDriverClassName = theProperties.getProperty(
          myJdbcConfigurationKeys.keyDriver).trim();
      myUrl = theProperties.getProperty(myJdbcConfigurationKeys.keyUrl).trim();
      myUser = theProperties.getProperty(myJdbcConfigurationKeys.keyUser)
          .trim();
      myPassword = theProperties.getProperty(
          myJdbcConfigurationKeys.keyPassword).trim();
      sqlCommands = theProperties.getProperty(
          myJdbcConfigurationKeys.keySqlCmds).trim();

      if (LOG.isDebugEnabled()) {
        LOG.debug("myJdbcInterface.readConfigFile: read file successfully");
        LOG.debug("     driver = " + myDriverClassName + " url = " + myUrl
            + " user = " + myUser);
      }
    } catch (Exception ex) {
      LOG.error("myJdbcInterface.readConfigFile: execption " + ex.getMessage());
      return false;
    }
    return true;
  }

  /**
   * initializes the database connection
   * 
   * @return true in case of success, false otherwise
   */
  public boolean initialize() {
    if (LOG.isDebugEnabled())
      LOG.debug("myJdbcInterface.initialize: entry ");

    if (bConfigFileRead == false) {
      LOG
          .error("myJdbcInterface.initialize: invalid database connection parameters: check file "
              + myConfigFile);
      return false;
    }

    try {
      Class.forName(myDriverClassName);
      if (LOG.isDebugEnabled())
        LOG.debug("myJdbcInterface.initialize: class.forName done ");
    } catch (java.lang.ClassNotFoundException e) {
      LOG
          .error("myJdbcInterface.initialize: cannot find class "
              + e.toString());
      return false;
    }

    try {
      if (LOG.isDebugEnabled())
        LOG
            .debug("myJdbcInterface.initialize: about to try getConnection for *"
                + myUrl + " user = " + myUser);
      myConnection = DriverManager.getConnection(myUrl, myUser, myPassword);

      if (LOG.isDebugEnabled())
        LOG.debug("myJdbcInterface.initialize: getConnection done ");

      myStatement = myConnection.createStatement();

      bConnected = true;

      if (LOG.isDebugEnabled())
        LOG.debug("myJdbcInterface.initialize: createStatement done ");

    } catch (SQLException ex) {
      LOG.error("SQLException: " + ex.getMessage());
      return false;
    }

    return true;
  }

  public void identify() {
    LOG.info("-----------------------------------------------");
    LOG.info("myJdbcInterface: " + myDriverClassName + "  " + myUrl + " "
        + myUser);
    LOG.info("config file " + myConfigFile);
    LOG.info("bConnected = " + bConnected);
    LOG.info("-----------------------------------------------");
  }
}
