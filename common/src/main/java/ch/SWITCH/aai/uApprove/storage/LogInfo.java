package ch.SWITCH.aai.uApprove.storage;

import ch.SWITCH.aai.uApprove.components.ConfigurationManager;
import ch.SWITCH.aai.uApprove.components.UApproveException;

import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * interface: LogInfo
 * 
 * Copyright (c) 2005-2006 SWITCH - The Swiss Education & Research Network
 * 
 * 
 * 
 * Purpose: interface to communicating the the Log information, which is
 * stored "somewhere" in a datastore.
 * 
 * The following subclasses have been implemented: LogInfoFile for file based
 * datastores LogInfoJdbc for JDBD based datastores
 * 
 * @author: C.Witzig date: 1.3.2006
 * 
 */
public abstract class LogInfo {
  public static final String STORE_TYPE_FILE = "File";
  public static final String STORE_TYPE_DB = "Database";
  
  private static LogInfo logInfo = null;


  
  private static Logger LOG = LoggerFactory.getLogger(LogInfo.class);

  public synchronized static void initialize(String type) throws UApproveException {
    if (type.equalsIgnoreCase(STORE_TYPE_FILE))
      logInfo = LogInfoFile.getInstance(ConfigurationManager.getParam(ConfigurationManager.COMMON_STORE_FILE_LOG));
    else if (type.equalsIgnoreCase(STORE_TYPE_DB))
      logInfo = LogInfoJdbc.getInstance(ConfigurationManager.getParam(ConfigurationManager.COMMON_STORE_DB_CONF));
    else
      throw new UApproveException("Unable to initilaize storage with type "+ type);
  }
  public static LogInfo getInstance() throws UApproveException {
    if (logInfo == null) {
      LOG.error("LogInfo has to be initialized first");
      throw new UApproveException("LogInfo has to be initialized first");
    }
    return logInfo;
  }



  public abstract Map<String, UserLogInfo> getData();
  

  public abstract UserLogInfo getData(String username);


  // / Updates the user info for a given provider
  abstract public void update(UserLogInfo theUserData, String theProviderId)
      throws UApproveException;

  // / Updates the provider independent part of the UserLogInfo
  public void update(UserLogInfo theUserData) throws UApproveException {
    update(theUserData, null);
  }

  abstract public void updateProviderAccess(String theUsername,
      String theProvider, boolean bGlobal) throws UApproveException;

  abstract public void updateProviderAccessWithNoARA(String theUsername,
      String theProvider) throws UApproveException;

  abstract public UserLogInfo addUserLogInfoData(String username,
      String version, String date, String termsVersion, String global,
      String providerId, String attributesReleased);

}
