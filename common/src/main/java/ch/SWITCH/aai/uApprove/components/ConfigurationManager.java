/*
 * Class ConfigurationManager:
 *
 * Copyright (c) 2005-2008 SWITCH - Serving Swiss Universities
 *
 * initialize the configuration and load them
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

public class ConfigurationManager {
  
  public static final String COMMON_STORE_TYPE = "storageType";
  public static final String COMMON_STORE_DB_CONF = "databaseConfig";
  public static final String COMMON_STORE_FILE_LOG = "flatFile";
  public static final String COMMON_TERMS = "termsOfUse";
  public static final String COMMON_SHARED_SECRET = "sharedSecret";
  
  public static final String PLUGIN_SP_BLACKLIST = "spBlacklist";
  public static final String PLUGIN_LOG_PROVIDER_ACCESS = "logProviderAccess";
  public static final String PLUGIN_MONITORING_ONLY = "monitoringOnly";
  public static final String PLUGIN_VIEWER_DEPLOYPATH = "uApproveViewer";
  public static final String PLUGIN_ISPASSIVE_SUPPORT = "isPassiveSupport";
  
  public static final String VIEWER_USELOCALE = "useLocale";
  public static final String VIEWER_ATTRIBUTELIST = "attributeList";
  public static final String VIEWER_GLOBAL_CONSENT = "globalConsentPossible";
  public static final String VIEWER_LOGBACK_CONFIG = "loggingConfig";
  
  // common used constants
  public static final String HTTP_PARAM_RETURNURL = "returnurl";
  public static final String HTTP_PARAM_RESET = "resetuserconsent";
  public static final String HTTP_PARAM_RELYINGPARTY = "relyingParty";
  public static final String HTTP_PARAM_ATTRIBUTES = "attributes";
  public static final String HTTP_PARAM_PRINCIPAL = "principal";
  
  private static ConfigurationManager configurationManager = null;
  private static Properties properties = null;

  private ConfigurationManager(String[] files) throws IOException, UApproveException {
    properties = new Properties();
    for (int i = 0; i < files.length; i++) {
      properties.load(new FileInputStream(new File(files[i].trim())));
    }
    checkConsistency();
  }
  
  public synchronized static void initialize(String filenames) throws UApproveException {
    if (configurationManager == null) {
      String[] filelist = filenames.split(";");
      try {
        configurationManager = new ConfigurationManager(filelist);
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
    if (configKey.equals(PLUGIN_LOG_PROVIDER_ACCESS)) {
       if (value == null || value.trim().equals(""))
         return "false";
    }
    
    // check for default value
    if (configKey.equals(PLUGIN_MONITORING_ONLY)) {
       if (value == null || value.trim().equals(""))
         return "false";
    }
    
    // check for default value
    if (configKey.equals(PLUGIN_ISPASSIVE_SUPPORT)) {
       if (value == null || value.trim().equals(""))
         return "false";
    }
    
    // check for default value
    if (configKey.equals(VIEWER_GLOBAL_CONSENT)) {
       if (value == null || value.trim().equals(""))
         return "true";
    }
    
    // check for default value
    if (configKey.equals(PLUGIN_VIEWER_DEPLOYPATH)) {
      if (value == null || value.trim().equals(""))
        return "/uApprove/Controller";
    }
    
    //LOG.debug("{} => {}", new Object[] {configKey, value});
    return (value != null) ? value.trim() : null;
  }
  
  private void checkConsistency() throws UApproveException {
    if (makeBoolean(getParam(PLUGIN_LOG_PROVIDER_ACCESS))) {
      if (!getParam(COMMON_STORE_TYPE).equalsIgnoreCase("Database"))
        throw new UApproveException("For logging provider access you have to use Database as store type");
    }
    if (makeBoolean(getParam(PLUGIN_MONITORING_ONLY))) {
      if (!makeBoolean(getParam(PLUGIN_LOG_PROVIDER_ACCESS)))
        throw new UApproveException("For monitoring only you have enable logging provider access");
    }
    
    // check for configuration consistency
    String storetype = getParam(COMMON_STORE_TYPE);
    if ( storetype != null && !storetype.equals("")) {
      if (!storetype.equalsIgnoreCase("file") && !storetype.equalsIgnoreCase("database"))
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