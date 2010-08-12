package ch.SWITCH.aai.uApprove.storage;

import ch.SWITCH.aai.uApprove.components.UApproveException;
import ch.SWITCH.aai.uApprove.storage.myJdbcInterface;

import java.util.TreeMap;
import java.util.Map;
import java.util.Collections;
import java.util.Enumeration;

import java.sql.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class: LogInfoJdbc
 * 
 * Copyright (c) 2005-2006 SWITCH - The Swiss Education & Research Network
 * 
 * Purpose: Subclass instantiating the LogInfo for JDBC based datastores
 * 
 * @author C.Witzig
 * 
 * 
 * 
 */
public class LogInfoJdbc extends LogInfo {
  private static Logger LOG = LoggerFactory.getLogger(LogInfoJdbc.class);
  private static LogInfoJdbc arpLogInfo = null;

  private static String theConfigFile;

  private myJdbcInterface theDB;

  // / properties for all the sql commands. Read from config file
  private static Properties theSqlCmds;

  private static int theGlobalShibProviderIndex = -1;

  private static final boolean bDebug = false;

  private class keySqlCmd {
    public static final String selGlobalShibProvider = "selGlobalShibProvider";

    public static final String selIdxUser = "selIdxUser";

    public static final String selShibProvider = "selShibProvider";

    public static final String insShibProvider = "insShibProvider";

    public static final String selArpInfoByUsername1 = "selArpInfoByUsername1";
    public static final String selArpInfoByUsername2 = "selArpInfoByUsername2";

    public static final String insUser = "insUser";

    public static final String updUser = "updUser";
    public static final String updUser1 = "updUser1";

    public static final String selGlobalArp = "selGlobalArp";

    public static final String insAttrApproval = "insAttrApproval";

    public static final String insAttrApproval1 = "insAttrApproval1";

    public static final String delAttrApproval = "delAttrApproval";

    public static final String updAttrApproval = "updAttrApproval";

    public static final String selIdxAttrApproval = "selIdxAttrApproval";

    public static final String selIdxAttrApprovalGlobal = "selIdxAttrApprovalGlobal";

    public static final String insProviderAccess = "insProviderAccess";

    public static final String clearReleaseForAccess = "clearReleaseForAccess";

    public static final String delAttrReleaseApprovals = "delAttrReleaseApprovals";
  }

  /**
   * Singleton Constructor
   */
  private LogInfoJdbc() {
  }

  private synchronized int getGlobalShibProvider() throws UApproveException {
    if (theGlobalShibProviderIndex != -1)
      return theGlobalShibProviderIndex;

    int idxShibProvider = -1;

    try {

      // "select idxShibProvider as idx from ShibProvider where spProviderName is null";
      String sql = (String) theSqlCmds.getProperty(keySqlCmd.selGlobalShibProvider);

      ResultSet rs = theDB.execSqlFT(sql, true);

      if (rs != null && rs.next()) {
        idxShibProvider = rs.getInt("idx");
        theGlobalShibProviderIndex = idxShibProvider;
      }

    } catch (SQLException ex) {
      throw new UApproveException("LogInfoJdbc.getGlobalShibProvider: "
          + ex.getMessage());
    }

    return idxShibProvider;
  }

  /**
   * Returns the index of the user username in the table ArpUser
   * 
   * @param username
   *          the user name
   * @return ArpUser.idxArpUser or -1 if no such user
   */
  private int getUserIndex(String username) throws UApproveException {
    // String sql =
    // "select idxArpUser as idxUser from ArpUser where auUserName = '?'";
    String sql = (String) theSqlCmds.getProperty(keySqlCmd.selIdxUser);

    sql = sql.replaceFirst("\\?", username);

    int idx = -1;

    try {
      ResultSet rs = theDB.execSqlFT(sql, true);

      if (rs != null && rs.next())
        idx = rs.getInt("idxUser");

    } catch (SQLException ex) {
    	LOG.error("LogInfoJdbc.getUserIndex: user = {}, {}", username, ex);
    	throw new UApproveException(ex);
    }

    return idx;
  }

  private int getProviderIndex(String sProviderName) throws UApproveException {
    // String sql =
    // "select idxShibProvider as idxProvider from ShibProvider where spProviderName = '?'";
    String sql = (String) theSqlCmds.getProperty(keySqlCmd.selShibProvider);

    sql = sql.replaceFirst("\\?", sProviderName);

    int idx = -1;

    try {
      ResultSet rs = theDB.execSqlFT(sql, true);

      if (rs != null && rs.next())
        idx = rs.getInt("idxProvider");

    } catch (SQLException ex) {
      	
        LOG.error("Provider = {}, {}",sProviderName, ex);
        throw new UApproveException(ex);
    }

    LOG.debug("Pprovider {} has index {}", sProviderName, idx);

    return idx;
  }

  private void addProvider(String sProviderName) throws UApproveException {
    // String sql = "insert into ShibProvider (spProviderName) values ( '?' )";
    String sql = (String) theSqlCmds.getProperty(keySqlCmd.insShibProvider);

    sql = sql.replaceFirst("\\?", sProviderName);

    try {
      theDB.execSqlFT(sql, false);
    } catch (SQLException ex) {
      throw new UApproveException(ex);
    }

    LOG.debug("LogInfoJdbc.addProvider: added provider {}", sProviderName);
  }

  private UserLogInfo getUserArpInfoByName(String theUserName)
      throws UApproveException {
    UserLogInfo userArp = getUserArpInfoByName1(theUserName);

    if (userArp == null)
      userArp = getUserArpInfoByName2(theUserName);

    return userArp;
  }

  /**
   * Retrieves the user info from the tables ArpUser, AttrReleaseApproval and
   * ShibProvider. Only returns info if the user exists and has at least one
   * AttrReleaseApproval.
   * 
   * @param username
   * @return UserLogInfo
   */
  private UserLogInfo getUserArpInfoByName1(String theUserName)
      throws UApproveException {

    UserLogInfo userArp = null;

    // String theSQL =
    // "select idxArpUser as idxUser, date_format(araTimeStamp,'%Y-%m-%d %H:%i%s') as ArpDate, araTermsVersion as TermsOfUseManager, araAttributes as Attributes, spProviderName as ShibProvider from ArpUser, AttrReleaseApproval, ShibProvider where auUserName='?' and idxArpUser=araIdxArpUser and araIdxShibProvider = idxShibProvider order by araTimeStamp desc";

    String theSQL = (String) theSqlCmds.getProperty(keySqlCmd.selArpInfoByUsername1);

    theSQL = theSQL.replaceFirst("\\?", theUserName);

    ResultSet rs = null;

    try {
      rs = theDB.execSqlFT(theSQL, true);
      LOG.trace("SQL {} executed", theSQL);


      String sTermsVersion = null;
      String sDate = null;
      String sGlobal = "no";
      Map<String, String> mapProviderIds = Collections.synchronizedSortedMap(new TreeMap<String, String>());
      
      while (rs.next()) {
    	LOG.trace("Iterate over results, row={}", rs.getRow());
        if (sTermsVersion == null)
          sTermsVersion = rs.getString("TermsOfUseManager");
        if (sDate == null)
          sDate = rs.getString("ArpDate");

        if (rs.getString("ShibProvider") == null)
          sGlobal = "yes";
        else {
          String sKey = rs.getString("ShibProvider");
          if (mapProviderIds.containsKey(sKey) == false)
            mapProviderIds.put(sKey, rs.getString("Attributes"));
        }

        LOG.trace("Building UserInfo with sTermsVersion={}", sTermsVersion);
        
        userArp = new UserLogInfo(theUserName, "dummy", sDate, sTermsVersion, sGlobal, mapProviderIds);
      }
    } catch (SQLException ex) {
      	LOG.error("SQL exception", ex);
      	throw new UApproveException(ex);
    }

    return userArp;
  }

  /**
   * Retrieves the user information only from the ArpUser table. It is possible
   * to have a user in the ArpUser table without a entry in AttrReleaseApproval,
   * namely if the user accepted the terms the first time but subsequently
   * declines every ArpRelease.
   * 
   * @param username
   * @return UserLogInfo with an empty map for the provider accesses
   */
  private UserLogInfo getUserArpInfoByName2(String theUserName)
      throws UApproveException {

    UserLogInfo userArp = null;

    // String theSQL =
    // "select idxArpUser as idxUser, auLastTermsVersion as TermsOfUseManager from ArpUser where auUserName='?'";

    String theSQL = (String) theSqlCmds.getProperty(keySqlCmd.selArpInfoByUsername2);

    theSQL = theSQL.replaceFirst("\\?", theUserName);

    ResultSet rs = null;

    try {
      rs = theDB.execSqlFT(theSQL, true);
      LOG.trace("SQL {} executed", theSQL);

      String sTermsVersion = null;
      String sDate = null;
      String sGlobal = "no";
      // make sure we return a Map, but it is empty
      Map<String, String> mapProviderIds = Collections.synchronizedSortedMap(new TreeMap<String, String>());

      while (rs.next()) {
        if (sTermsVersion == null)
          sTermsVersion = rs.getString("TermsOfUseManager");
        if (sDate == null)
          sDate = rs.getString("ArpDate");

        userArp = new UserLogInfo(theUserName, "dummy", sDate, sTermsVersion, sGlobal, mapProviderIds);
      }
    } catch (SQLException ex) {
      	LOG.error("LogInfo.getUserArpInfoByName2: SQL exception ", ex);
      	throw new UApproveException(ex);
    }

    return userArp;
  }

  private void createUser(UserLogInfo theUserData) throws UApproveException {
    // String sql =
    // "insert into ArpUser (auUserName, auLastTermsVersion, auFirstAccess, auLastAccess ) values ( '?', '?', now(), now() )";

    String sql = (String) theSqlCmds.getProperty(keySqlCmd.insUser);
    sql = sql.replaceFirst("\\?", theUserData.getUsername());
    sql = sql.replaceFirst("\\?", theUserData.getTermsVersion());

    try {
      theDB.execSqlFT(sql, false);
    } catch (SQLException ex) {
      throw new UApproveException(ex);
    }
  }

  /**
   * Updates the relevant fields in the ArpUser table. The current TermsVersion
   * is stored and the time of acess.
   */
  private void updateUser(UserLogInfo theUserData) throws UApproveException {
    // String sql =
    // "update ArpUser set auLastTermsVersion = '?', auFirstAccess=auFirstAccess, auLastAccess=now() where auUsername = '?'";
    String sql = (String) theSqlCmds.getProperty(keySqlCmd.updUser);

    sql = sql.replaceFirst("\\?", theUserData.getTermsVersion());
    sql = sql.replaceFirst("\\?", theUserData.getUsername());

    try {
      theDB.execSqlFT(sql, false);
      LOG.trace("SQL {} executed", sql);
    } catch (SQLException ex) {
      throw new UApproveException(ex);
    }
  }

  /** Returns true if the user has its arp set to global */
  private boolean hasUserGlobalArp(String theUsername) throws UApproveException {
    // String sql =
    // "select count(*) as cnt from AttrReleaseApproval, ArpUser, ShibProvider where idxArpUser=araIdxArpUser and idxShibProvider = araIdxShibProvider and spProviderName is null and auUserName = '?'";

    String sql = (String) theSqlCmds.getProperty(keySqlCmd.selGlobalArp);
    sql = sql.replaceFirst("\\?", theUsername);

    int nCount = 0;
    try {
      ResultSet rs = theDB.execSqlFT(sql, true);
      if (rs.next())
        nCount = rs.getInt(1);

    } catch (SQLException ex) {
      throw new UApproveException(ex);
    }

    LOG.debug("LogInfoJdbc.hasUserGlobalArp: user = {} has global {}",
    		theUsername, nCount);

    return nCount == 1 ? true : false;
  }

  /**
   * Sets or unsets the user arp to global
   * 
   * @param username
   * @param idxUser
   *          : database entry ArpUser.idxArpUser
   * @param bValue
   *          : if true set global arp, if false delete global arp
   */
  private void setUserArpGlobal(UserLogInfo theUserData, int idxUser,
      boolean bValue) throws UApproveException {
    try {
      if (hasUserGlobalArp(theUserData.getUsername()) == bValue)
        return;

      int idxGlobalProvider = getGlobalShibProvider();

      String sql;

      if (bValue == true) {
        // sql =
        // "insert into AttrReleaseApproval ( araIdxArpUser, araIdxShibProvider, araTermsVersion,araAttributes ) values ( ?, ?, '?', null )";

        sql = (String) theSqlCmds.getProperty(keySqlCmd.insAttrApproval);
        sql = sql.replaceFirst("\\?", (new Integer(idxUser)).toString());
        sql = sql.replaceFirst("\\?", (new Integer(idxGlobalProvider))
            .toString());
        sql = sql.replaceFirst("\\?", theUserData.getTermsVersion());

      } else {
        // sql =
        // "delete from AttrReleaseApproval where araIdxArpUser = ? and araIdxShibProvider = ?";
        sql = (String) theSqlCmds.getProperty(keySqlCmd.delAttrApproval);
        sql = sql.replaceFirst("\\?", (new Integer(idxUser)).toString());
        sql = sql.replaceFirst("\\?", (new Integer(idxGlobalProvider))
            .toString());
      }

      theDB.execSqlFT(sql, false);

    } catch (SQLException ex) {
      throw new UApproveException(ex);
    }

    return;
  }

  private void updateAttrReleaseApp(String theUsername, String theTermsVersion,
      String theProviderId, String theAttr) throws UApproveException {
    try {

      int idxUser = getUserIndex(theUsername);
      int idxShibProvider = getProviderIndex(theProviderId);
      if (idxShibProvider < 0) {
        addProvider(theProviderId);
        idxShibProvider = getProviderIndex(theProviderId);
      }

      // String theSql =
      // "update AttrReleaseApproval set araTermsVersion = '?', araAttributes = '?' where araIdxArpUser = ? and araIdxShibProvider = ?";
      String theSql = (String) theSqlCmds
          .getProperty(keySqlCmd.updAttrApproval);
      theSql = theSql.replaceFirst("\\?", theTermsVersion);
      theSql = theSql.replaceFirst("\\?", theAttr);
      theSql = theSql.replaceFirst("\\?", (new Integer(idxUser)).toString());
      theSql = theSql.replaceFirst("\\?", (new Integer(idxShibProvider))
          .toString());

      theDB.execSqlFT(theSql, false);

      LOG.debug("LogInfoJdbc.updateAttrReleaseApp: updating release approval for "+theUsername+" {} {}", theProviderId, theAttr);
    } catch (Exception ex) {
      throw new UApproveException(ex);
    }

    return;
  }

  private void addAttrReleaseApp(String theUsername, String theTermsVersion,
      String theProviderId, String theAttr) throws UApproveException {
    try {

      int idxUser = getUserIndex(theUsername);
      int idxShibProvider = getProviderIndex(theProviderId);
      if (idxShibProvider < 0) {
        addProvider(theProviderId);
        idxShibProvider = getProviderIndex(theProviderId);
      }
      // String theSql =
      // "insert into AttrReleaseApproval (araIdxArpUser, araIdxShibProvider, araTermsVersion, araAttributes ) values ( ?, ?, '?', '?' )";
      String theSql = (String) theSqlCmds
          .getProperty(keySqlCmd.insAttrApproval1);
      theSql = theSql.replaceFirst("\\?", (new Integer(idxUser)).toString());
      theSql = theSql.replaceFirst("\\?", (new Integer(idxShibProvider))
          .toString());

      theSql = theSql.replaceFirst("\\?", theTermsVersion);

      theSql = theSql.replaceFirst("\\?", theAttr);

      theDB.execSqlFT(theSql, false);

      
      LOG.debug("LogInfoJdbc.updateAttrReleaseApp: updating release approval for "+theUsername+" {} {}", theProviderId, theAttr);
    } catch (Exception ex) {
      throw new UApproveException(ex);
    }

    return;
  }

  private void clearUserArpEntries(String theUserName) throws UApproveException {
    LOG.debug("clear user arp entries");

    int idxUser = getUserIndex(theUserName);

    try {
      // String sql1 =
      // "update ProviderAccess set paIdxAttrReleaseApproval = NULL, paTimeStamp = paTimeStamp where idxArpUser = ?"
      String sql1 = (String) theSqlCmds
          .getProperty(keySqlCmd.clearReleaseForAccess);

      sql1 = sql1.replaceFirst("\\?", (new Integer(idxUser)).toString());
      theDB.execSqlFT(sql1, false);

      // String sql2 =
      // "delete from AttrReleaseApproval where araIdxArpUser = ?";
      String sql2 = (String) theSqlCmds
          .getProperty(keySqlCmd.delAttrReleaseApprovals);

      sql2 = sql2.replaceFirst("\\?", (new Integer(idxUser)).toString());

      theDB.execSqlFT(sql2, false);
    } catch (Exception ex) {
      LOG.error("LogInfoJdbc.clearUserArp: exception {}", ex);
      throw new UApproveException(ex);
    }
  }

  /**
   * Singleton getInstance
   */
  public static synchronized LogInfo getInstance(String filename)
      throws UApproveException {
    if (arpLogInfo == null) {
      arpLogInfo = new LogInfoJdbc();
      LogInfoJdbc.theConfigFile = filename;
      arpLogInfo.initialize();
    }
    return arpLogInfo;
  }

  /**
   * initialise the database connection
   */
  private void initialize() throws UApproveException {
    theDB = new myJdbcInterface(theConfigFile, bDebug);

    if (!theDB.initialize())
      throw new UApproveException(
          "LogInfoJdbc.exception initialising the database");

    try {
      readSqlCommands(theDB.getSqlCmds());
    } catch (UApproveException ex) {
      throw new UApproveException(ex);
    }
  }

  private void readSqlCommands(String theSqlFile) throws UApproveException {

    if (theSqlFile == null)
      throw new UApproveException(
          "LogInfoJdbc.readSqlCommands: config key not defined");

    try {
      File theFile = new File(theSqlFile);
      if (!theFile.exists() || !theFile.isFile() || !theFile.canRead()) {
        throw new UApproveException("Cannot read file " + theSqlFile);

      }
      theSqlCmds = new Properties();
      theSqlCmds.load(new FileInputStream(theFile));

      if (bDebug) {
        Enumeration theEnums = theSqlCmds.propertyNames();
        for (Enumeration e =  theSqlCmds.propertyNames(); e.hasMoreElements();) {
          String theKey = (String) e.nextElement();
        }
      }

    } catch (Exception ex) {
      throw new UApproveException("Reading sql command file "+ theSqlFile + " " + ex.getMessage());
    }
  }

  /**
   * Returns the data for all the users.
   * 
   * @return Map
   */
  /*
   * public Map getData() { return data; }
   */

  public Map<String, UserLogInfo> getData() {
    return null;
  }

  /**
   * Returns the data related to a user.
   * 
   * @param username
   * @return UserLogInfo
   */
  public UserLogInfo getData(String username) {
    int idx = -1;
    UserLogInfo theUserInfo = null;

    try {
      idx = getUserIndex(username);

      if (idx <= 0)
        return null;

      theUserInfo = getUserArpInfoByName(username);

    } catch (UApproveException ex) {
      LOG.error("Exception ", ex);
      theUserInfo = null;
    }
    return theUserInfo;

  }
  
  public UserLogInfo getDataSimple(String username) {
	  UserLogInfo userInfo = null;
	  try {
		  userInfo = getUserArpInfoByName2(username);
	} catch (UApproveException e) {
	      LOG.error("Exception", e);
	}
	return userInfo;
  }

  /**
   * Write LogInfo to xml file
   * 
   * @param session
   * @throws UApproveException
   */
  public synchronized void update(UserLogInfo theUserData, String theProviderId)
      throws UApproveException {

    if (arpLogInfo == null)
      throw new UApproveException(
          "LogInfoJdbc.update: LogInfo was not yet initialized and now it need to be saved????.");

    if (theUserData == null)
      throw new UApproveException("LogInfoJdbc.update: theUserData is null ");

    try {
      boolean bUserHasGlobalArp = false;
      int idxUser = getUserIndex(theUserData.getUsername());

      if (idxUser < 0) {
        createUser(theUserData);
        idxUser = getUserIndex(theUserData.getUsername());
      } else {
        updateUser(theUserData);

        bUserHasGlobalArp = hasUserGlobalArp(theUserData.getUsername());
      }

      if (theUserData.getGlobal().equalsIgnoreCase("yes")
          && bUserHasGlobalArp == false) {
        setUserArpGlobal(theUserData, idxUser, true);
        return;
      }

      if ((theUserData.getGlobal().equalsIgnoreCase("yes") == false)
          && bUserHasGlobalArp == true)
        setUserArpGlobal(theUserData, idxUser, false);

      // if the map of providers is completely empty, delete the entries in the
      // db
      Map mapShibProviders = theUserData.getProviderIds();
      if (mapShibProviders.isEmpty() == true)
        clearUserArpEntries(theUserData.getUsername());

      if (theProviderId == null)
        return;

      String theAttr = (String) mapShibProviders.get(theProviderId);

      UserLogInfo theDbInfo = getUserArpInfoByName(theUserData.getUsername());
      Map<String, String> mapDbShibProviders = theDbInfo != null ? theDbInfo.getProviderIds()
          : null;

      if (mapDbShibProviders != null
          && mapDbShibProviders.containsKey(theProviderId) == true)
        updateAttrReleaseApp(theUserData.getUsername(), theUserData
            .getTermsVersion(), theProviderId, theAttr);
      else
        addAttrReleaseApp(theUserData.getUsername(), theUserData
            .getTermsVersion(), theProviderId, theAttr);
    } catch (Exception ex) {
      LOG.error("Exception.", ex);
      throw new UApproveException("Exception trying to store user", ex);
    }

  }

  public UserLogInfo addUserLogInfoData(String username, String version,
      String date, String termsVersion, String global, String providerId,
      String attributesReleased) {
    UserLogInfo theUserArpInfo = new UserLogInfo(username, version, date,
        termsVersion, global);
    if (providerId != null && attributesReleased != null)
      theUserArpInfo.addProviderId(providerId, attributesReleased);
    return theUserArpInfo;
  }

  public synchronized void updateProviderAccess(String theUsername,
      String theProvider, boolean bGlobal) throws UApproveException {

    LOG.debug("LogInfoJdbc.updateArpProviderAccess: cannot find approval for user "
              + theUsername + " provider = " + theProvider + " with global access " + bGlobal);

    try {

      // String theSQL =
      // "update ArpUser set auFirstAccess = auFirstAccess, auLastAccess = now() where auUserName = '?'";
      String theSQL = (String) theSqlCmds.getProperty(keySqlCmd.updUser1);
      theSQL = theSQL.replaceFirst("\\?", theUsername);

      theDB.execSqlFT(theSQL, false);

      theSQL = bGlobal == false ? (String) theSqlCmds
          .getProperty(keySqlCmd.selIdxAttrApproval) : (String) theSqlCmds
          .getProperty(keySqlCmd.selIdxAttrApprovalGlobal);

      // "select idxAttrReleaseApproval as idxApproval, araIdxArpUser as IdxUser, araIdxShibProvider as idxProvider from ArpUser, AttrReleaseApproval, ShibProvider where auUserName='?' and idxArpUser=araIdxArpUser and spProviderName = '?' and araIdxShibProvider = idxShibProvider order by araTimeStamp desc"
      // :
      // "select idxAttrReleaseApproval as idxApproval, araIdxArpUser as IdxUser, araIdxShibProvider as idxProvider from ArpUser, AttrReleaseApproval, ShibProvider where auUserName='?' and idxArpUser=araIdxArpUser and spProviderName is null and araIdxShibProvider = idxShibProvider order by araTimeStamp desc";

      theSQL = theSQL.replaceFirst("\\?", theUsername);
      if (bGlobal == false)
        theSQL = theSQL.replaceFirst("\\?", theProvider);

      ResultSet rs = theDB.execSqlFT(theSQL, true);

      int idxApproval = -1;
      int idxUser = -1;
      int idxProvider = -1;
      String sAttributes = null;
      String sTermsVersion = null;

      if (rs != null && rs.next()) {
        idxApproval = rs.getInt(1);
        idxUser = rs.getInt(2);
        idxProvider = rs.getInt(3);
        sTermsVersion = rs.getString(4);
        sAttributes = rs.getString(5) != null ? rs.getString(5) : "";
      }

      if (idxApproval == -1)
        throw new UApproveException(
            "LogInfoJdbc.updateArpProviderAccess: cannot find approval for user "
                + theUsername + " provider = " + theProvider
                + " with global access " + bGlobal);

      // we must get the provider from the name and not rely on the
      // AttrReleaseApproval
      // in the case of a global attribute release, i.e. shibprovider = null)
      int iGlobalShibProvider = getGlobalShibProvider();
      if (idxProvider == iGlobalShibProvider) {
        idxProvider = getProviderIndex(theProvider);
        if (idxProvider < 0) {
          addProvider(theProvider);
          idxProvider = getProviderIndex(theProvider);
        }
      }

      // theSQL =
      // "insert into ProviderAccess ( paIdxAttrReleaseApproval, paTimeStamp ) values ( ?, ?, now() )";
      theSQL = (String) theSqlCmds.getProperty(keySqlCmd.insProviderAccess);
      theSQL = theSQL.replaceFirst("\\?", (new Integer(idxUser)).toString());
      theSQL = theSQL
          .replaceFirst("\\?", (new Integer(idxProvider)).toString());
      theSQL = theSQL.replaceFirst("\\?", sTermsVersion);
      theSQL = theSQL.replaceFirst("\\?", sAttributes);
      theSQL = theSQL
          .replaceFirst("\\?", (new Integer(idxApproval)).toString());

      theDB.execSqlFT(theSQL, false);
    } catch (SQLException ex) {
    	LOG.error("LogInfoJdbc.updateArpProviderAccess:" +
      		"cannot find approval for user "+theUsername+", provider = {} with with global access {}",
      		theProvider, bGlobal);
    	throw new UApproveException(ex);
    }

  }

  public synchronized void updateProviderAccessWithNoARA(String theUsername,
      String theProvider) throws UApproveException {

    try {

      int idxUser = getUserIndex(theUsername);

      int idxProvider = getProviderIndex(theProvider);
      if (idxProvider < 0) {
        addProvider(theProvider);
        idxProvider = getProviderIndex(theProvider);
      }

      String theSQL = (String) theSqlCmds
          .getProperty(keySqlCmd.insProviderAccess);
      theSQL = theSQL.replaceFirst("\\?", (new Integer(idxUser)).toString());
      theSQL = theSQL
          .replaceFirst("\\?", (new Integer(idxProvider)).toString());
      theSQL = theSQL.replaceFirst("\\?", "");
      theSQL = theSQL.replaceFirst("\\?", "");
      theSQL = theSQL.replaceFirst("\\?", "NULL");

      theDB.execSqlFT(theSQL, false);
    } catch (SQLException ex) {
      LOG.error("SQLException", ex);
      throw new UApproveException(ex);
    }

  }

}
