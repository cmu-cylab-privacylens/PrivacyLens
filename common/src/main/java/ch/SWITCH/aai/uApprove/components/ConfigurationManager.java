/*
 * Class ConfigurationManager:
 *
 * Copyright (c) 2005-2008 SWITCH - Serving Swiss Universities
 *
 * initialize the ArpProperties and load them
 * supply method to get config values
 *
 * @author CW/HR
 * @date 24.1.2008
 * 
 */

package ch.SWITCH.aai.uApprove.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Properties;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import ch.SWITCH.aai.uApprove.components.UApproveException;

public class ConfigurationManager {
  
  public static final String COMMON_STORE_TYPE = "ArpDbType";
  public static final String COMMON_STORE_DB_SQL_CONF = "ArpDbConfigFile";
  public static final String COMMON_STORE_FILE_LOG = "ArpUserLogInfo";
  public static final String COMMON_ARP_TERMS = "TermsOfUseManager";
  public static final String COMMON_SHARED_SECRET = "SharedSecret";
  
  public static final String ARPFILTER_ARP_BLACKLIST = "SPBlacklistManager";
  public static final String ARPFILTER_LOG_PROVIDER_ACCESS = "LogProviderAccess";
  public static final String ARPFILTER_MONITORING_ONLY = "MonitoringOnly";
  public static final String ARPFILTER_ARPVIEWER_DEPLOYPATH = "ArpViewerDeployPath";
  public static final String ARPFILTER_ISPASSIVE_SUPPORT = "IsPassiveSupport";
  
  public static final String ARPVIEWER_USE_LOCAL = "UseLocale";
  public static final String ARPVIEWER_ATTRIBUTELIST = "AttrListFileName";
  public static final String ARPVIEWER_GLOBAL_ARP_DISABLED = "GlobalArpDisabled";
  public static final String ARPVIEWER_LOGBACK_CONFIG = "LoggingConfig";
  
  // common used constants
  public static final String HTTP_PARAM_RETURNURL = "returnurl";
  public static final String HTTP_PARAM_EDIT = "editarpsettings";
  public static final String HTTP_PARAM_PROVIDERID = "providerid";
  public static final String HTTP_PARAM_ATTRIBUTES = "attributes";
  public static final String HTTP_PARAM_USERNAME = "username";
  
  private static Logger LOG = LoggerFactory.getLogger( ConfigurationManager.class );
  private static ConfigurationManager arpConfiguration = null;
  private static Properties properties = null;

  private ConfigurationManager(String[] files) throws IOException, UApproveException {
    properties = new Properties();
    for (int i = 0; i < files.length; i++) {
      properties.load(new FileInputStream(new File(files[i].trim())));
    }
    checkConsistency();
  }
  
  public synchronized static void initialize(String filenames) throws UApproveException {
    if (arpConfiguration == null) {
      String[] filelist = filenames.split(";");
      try {
        arpConfiguration = new ConfigurationManager(filelist);
      } catch (IOException e) {
        throw new UApproveException(e);
      }
    }
  }
  
  public static String getParam(String configKey) throws UApproveException {
    if (properties == null)
      throw new UApproveException(ConfigurationManager.class +" is not initialized");
    String value = (String) properties.getProperty(configKey);
    
    // check for default value
    if (configKey.equals(ARPFILTER_LOG_PROVIDER_ACCESS)) {
       if (value == null || value.trim().equals(""))
         return "false";
    }
    
    // check for default value
    if (configKey.equals(ARPFILTER_MONITORING_ONLY)) {
       if (value == null || value.trim().equals(""))
         return "false";
    }
    
    // check for default value
    if (configKey.equals(ARPFILTER_ISPASSIVE_SUPPORT)) {
       if (value == null || value.trim().equals(""))
         return "false";
    }
    
    // check for default value
    if (configKey.equals(ARPVIEWER_GLOBAL_ARP_DISABLED)) {
       if (value == null || value.trim().equals(""))
         return "false";
    }
    
    // check for default value
    if (configKey.equals(ARPFILTER_ARPVIEWER_DEPLOYPATH)) {
      if (value == null || value.trim().equals(""))
        return "/arpviewer/Controller";
    }
    
    LOG.debug("{} => {}", new Object[] {configKey, value});
    return (value != null) ? value.trim() : null;
  }
  
  private void checkConsistency() throws UApproveException {
    if (makeBoolean(getParam(ARPFILTER_LOG_PROVIDER_ACCESS))) {
      if (!getParam(COMMON_STORE_TYPE).equalsIgnoreCase("Database"))
        throw new UApproveException("For logging provider access you have to use Database as store type");
    }
    if (makeBoolean(getParam(ARPFILTER_MONITORING_ONLY))) {
      if (!makeBoolean(getParam(ARPFILTER_LOG_PROVIDER_ACCESS)))
        throw new UApproveException("For monitoring only you have enable logging provider access");
    }
    
    // check for configuration consistency
    String storetype = getParam(COMMON_STORE_TYPE);
    if ( storetype != null && !storetype.equals("")) {
      if (!storetype.equalsIgnoreCase("File") && !storetype.equalsIgnoreCase("Database"))
        throw new UApproveException("Store type ("+COMMON_STORE_TYPE+") has to be set to File or Database");
      // TODO check for values according store type
    }
    
    String secret = getParam(COMMON_SHARED_SECRET);
    if (secret.length() < 16 ) {
      throw new UApproveException("The shared secret has to be 16 chars at minimum");
    }
  }
  
  public static boolean makeBoolean(String value) {
    if (value != null && (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")))
      return true;
    return false;
  }
}