package ch.SWITCH.aai.uApprove.storage;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class that contains the info for one user.
 * 
 * Copyright (c) 2005-2006 SWITCH - The Swiss Education & Research Network
 * 
 * 
 * Purpose: store all info for a user in a class and keep it in memory, such
 * that it can be retrieved very quickly.
 * 
 * Developped based on the original version from F.Poroli. Addition of global
 * and mapProviderIds parameter
 * 
 * @author C.Witzig 8.12.2005
 * 
 */

public class UserLogInfo {
  private String username; // user's name
  private String version; // version of setting accepted
  private String ondate; // date when last setting applied
  private String termsVersion; // terms of use version accepted
  private String global; // whether the user wants to have a global approval
  private Map<String, String> mapProviderIds;

  private static Logger LOG = LoggerFactory.getLogger(UserLogInfo.class);


  
  /**
   * 
   * @param username
   * @param version
   * @param ondate
   */
  public UserLogInfo(String username, String version, String ondate) {
    this(username, version, ondate, "", "no");
  }

  public UserLogInfo(String username, String version, String ondate,
      String termsVersion) {
    this(username, version, ondate, termsVersion, "no");
  }

  /**
   * 
   * @param username
   * @param version
   * @param ondate
   * @param termsVersion
   * @param sGlobal
   */
  public UserLogInfo(String username, String version, String ondate,
      String termsVersion, String sGlobal) {
    this.username = username;
    this.version = version;
    this.ondate = ondate;
    this.termsVersion = termsVersion;
    this.global = sGlobal;
    // setProviderIds = Collections.synchronizedSortedSet( new TreeSet<String>()
    // );
    mapProviderIds = Collections.synchronizedSortedMap(new TreeMap<String, String>());
  }

  public UserLogInfo(String username, String version, String ondate,
      String termsVersion, String sGlobal, Map<String, String> mapProviderIds) {
    this.username = username;
    this.version = version;
    this.ondate = ondate;
    this.termsVersion = termsVersion;
    this.global = sGlobal;
    this.mapProviderIds = mapProviderIds;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setOndate(String ondate) {
    this.ondate = ondate;
  }

  public void setTermsVersion(String version) {
    this.termsVersion = version;
  }

  public void setGlobal(String sGlobal) {
    this.global = sGlobal;
  }

  public String getUsername() {
    return username;
  }

  public String getVersion() {
    return version;
  }

  public String getOndate() {
    return ondate;
  }

  public String getTermsVersion() {
    return termsVersion != null ? termsVersion : "";
  }

  public String getGlobal() {
    return global;
  }

  public void addProviderId(String sProviderId, String sAttributes) {
    mapProviderIds.put(sProviderId, sAttributes);
  }

  public boolean containsProviderId(String sProviderId) {
    return mapProviderIds.containsKey(sProviderId);
  }

  public boolean containsAttributeForProviderId(String sProviderId,
      String sAttribute, String sDelimiter) {
    String theAttributes = mapProviderIds.get(sProviderId);
    if (theAttributes == null)
      return false;

    return theAttributes.indexOf(sDelimiter + sAttribute + sDelimiter) != -1 ? true
        : false;
  }

  public String getAttributesForProviderId(String sProviderId) {
    return mapProviderIds.get(sProviderId);
  }

  public Map<String, String> getProviderIds() {
    return mapProviderIds;
  }

  public void clearRelease() {
    mapProviderIds.clear();
  }
  
  public void clearRelease(String providerId) {
    mapProviderIds.remove(providerId);
  }
  

  public void dump() {
    LOG.info("UserLogInfo.dump: " + " user = " + username + " version = "
        + version + " ondate  = " + ondate + " termsVersion = " + termsVersion
        + " global = " + global);
    LOG.info("List of accepted providerIds ");
    if (mapProviderIds != null) {
      Set<String> keySet = mapProviderIds.keySet();
      Iterator<String> it = keySet.iterator();
      while (it.hasNext()) {
        String key = it.next();
        String value = mapProviderIds.get(key);
        LOG.info("    " + key + " = <" + value + "> ");
      }
    }
  }
  

  
}
