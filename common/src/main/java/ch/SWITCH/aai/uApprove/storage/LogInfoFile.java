package ch.SWITCH.aai.uApprove.storage;

import ch.SWITCH.aai.uApprove.components.UApproveException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.TreeMap;
import java.util.Set;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Class: LogInfo
 * 
 * Copyright (c) 2005-2006 SWITCH - The Swiss Education & Research Network
 * 
 * 
 * 
 * @author poroli
 *         <p>
 *         The class LogInfo holds a set of UserLogInfo classes. In the class
 *         UserLogInfo we record the version number approved by a user.
 *         </p>
 * 
 *         This is the original implementation of LogInfo, which was damned to
 *         be a subclass of LogInfo, when the support for JDBC was added.
 * 
 * @author C.Witzig 1.3.2006
 * 
 * 
 * 
 *         Modifications: -------------- 1. Added proper exception handling in
 *         LogInfoFile.initialize if the file couldn't be created chw 18.6.2007
 * 
 */

public class LogInfoFile extends LogInfo {
  private static LogInfoFile logInfo;

  private static String filename;

  private Map<String, UserLogInfo> data;

  private static Logger LOG = LoggerFactory.getLogger(LogInfoFile.class);

  /**
   * Singleton Constructor
   */
  private LogInfoFile() {
    data = new HashMap<String, UserLogInfo>();
  }

  /**
   * Singleton getInstance
   */
  public static synchronized LogInfo getInstance(String filename)
      throws UApproveException {
    logInfo = new LogInfoFile();
    LogInfoFile.filename = filename;
    logInfo.initialize();
    return logInfo;
  }

  /**
   * 
   * read data from xml file into hashmap
   */
  private void initialize() throws UApproveException {
    File userLogInfoFile = null;
    try {
      userLogInfoFile = new File(filename);
      if (!userLogInfoFile.exists()) {
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(
            userLogInfoFile, false));
        out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        out.write("<Rule>");
        out.write("</Rule>");
        out.flush();
        out.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new UApproveException(
          "LogInfoFile.initialize: cannot open or write file [" + filename
              + "]");
    }
    Document userSettingDoc = null;
    try {
      DocumentBuilder db = DocumentBuilderFactory.newInstance()
          .newDocumentBuilder();
      userSettingDoc = db.parse(new FileInputStream(userLogInfoFile));
    } catch (Exception e) {
      throw new UApproveException(
          "LogInfoFile:initialize: An exception occurred trying to read the file ["
              + filename + "]");
    }
    logInfo.readFromXmlDocument(userSettingDoc);
  }

  /**
   * Read the user settings LogInfo XML document
   * 
   * @param doc
   * @throws UApproveException
   */
  private void readFromXmlDocument(Document doc) throws UApproveException {
    LOG.trace("XML is re-readed");
    Node n, t;
    String username, version, date, termsVersion;
    String global = "no";
    NodeList userList;

    NodeList list = doc.getElementsByTagName("User");

    for (int i = 0, lasti = list.getLength(); i < lasti; i++) {
      // Set<String> setProviderIds = Collections.synchronizedSortedSet( new
      // TreeSet<String>() );
      Map<String, String> mapProviderIds = Collections.synchronizedSortedMap(new TreeMap<String, String>());
      version = date = termsVersion = null;
      n = list.item(i);
      t = n.getAttributes().getNamedItem("name");
      if (t == null)
        throw new UApproveException("In file [" + filename
            + "] file found a <User> tag without the attribute \"name\"");
      username = t.getNodeValue();
      userList = n.getChildNodes();
      for (int j = 0, lastj = userList.getLength(); j < lastj; j++) {
        n = userList.item(j);
        if (n.getNodeName().equals("Version")) {
          t = n.getAttributes().getNamedItem("id");
          if (t == null) {
            version = ""; // version can be left out when not using user
          } else {
            version = t.getNodeValue();
          }
          t = n.getAttributes().getNamedItem("terms-id");
          if (t == null) {
            termsVersion = ""; // termsVersion is not needed
          } else {
            termsVersion = t.getNodeValue();
          }
          t = n.getAttributes().getNamedItem("installationdate");
          if (t == null)
            throw new UApproveException(
                "In file ["
                    + filename
                    + "] found a <Version> tag without the \"installationdate\" attribute.");
          date = t.getNodeValue();
          t = n.getAttributes().getNamedItem("global");
          global = (t == null) ? "no" : t.getNodeValue();
        }
        if (n.getNodeName().equals("providerId")) {
          Node nodeProviderName = n.getAttributes().getNamedItem("name");
          if (nodeProviderName != null) {
            Node nodeAttributes = n.getAttributes().getNamedItem("attributes");
            String sAttributes = (nodeAttributes != null) ? nodeAttributes
                .getNodeValue() : "";
            String theProviderId = nodeProviderName.getNodeValue();
            mapProviderIds.put(theProviderId, sAttributes);
          }
        }
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("LogInfo.readFromXmlDocument: user = " + username
            + " version = " + version + " date = " + date + " termsVersion = "
            + termsVersion + " global " + global);
      }

      if (username == null || username == "")
        throw new UApproveException("Could not read the username in the file "
            + filename + ".");

      if (date == null || date == "")
        throw new UApproveException(
            "Could not detect the install date in the file " + filename + ".");

      this.addUserLogInfoData(username, version, date, termsVersion, global,
          mapProviderIds);
    }
  }

  /**
   * Returns the data for all the users.
   * 
   * @return Map
   */
  public Map<String, UserLogInfo> getData() {

    return data;
  }

  /**
   * Returns the data related to a user.
   * 
   * @param username
   * @return UserLogInfo
   */
  public UserLogInfo getData(String username) {

    if (data.containsKey(username)) {
      return (UserLogInfo) data.get(username);
    }
    return null;
  }

  /**
   * Write LogInfo to xml file
   * 
   * @param session
   * @throws UApproveException
   */
  public synchronized void update(UserLogInfo theUserData, String sProviderId)
      throws UApproveException {
    if (logInfo == null) {
      throw new UApproveException(
          "LogInfo was not yet initialized and now it need to be saved????.");
    }
    File f = new File(filename);
    OutputStreamWriter out = null;
    try {
      out = new OutputStreamWriter(new FileOutputStream(f, false));
      out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      out.write("<Users>\n");
      UserLogInfo uLogInfo;
      Iterator<String> iter = getData().keySet().iterator();
      // write all User elements
      while (iter.hasNext()) {
        uLogInfo = (UserLogInfo) getData().get(iter.next());
        out.write("  <User name=\"" + uLogInfo.getUsername() + "\">\n");
        out.write("    <Version id=\"" + uLogInfo.getVersion() + "\"");
        if (!(uLogInfo.getTermsVersion() == null || uLogInfo.getTermsVersion()
            .equals(""))) {
          out.write(" terms-id=\"" + uLogInfo.getTermsVersion() + "\"");
        }
        out.write(" global=\"" + uLogInfo.getGlobal() + "\"");
        out.write(" installationdate=\"" + uLogInfo.getOndate() + "\"/>\n");

        // write out the providers if any
        // Set<String> setProviderIds = uLogInfo.getProviderIds();
        Map<String, String> mapProviderIds = uLogInfo.getProviderIds();
        Set<String> keySet = mapProviderIds.keySet();
        Iterator<String> it = keySet.iterator();
        while (it.hasNext()) {
          String theKey = (String) it.next();
          String theAttr = (String) mapProviderIds.get(theKey);
          out.write("         <providerId name=\"" + theKey
              + "\" attributes=\"" + theAttr + "\"/>\n");
        }
        out.write("  </User>\n");
        if (LOG.isDebugEnabled()) {
          LOG.debug("LogInfo.update: writing user " + uLogInfo.getUsername()
              + " version = " + uLogInfo.getVersion() + " date = "
              + uLogInfo.getOndate() + " termsVersion = "
              + uLogInfo.getTermsVersion() + " global " + uLogInfo.getGlobal());
        }
      }
      out.write("</Users>\n");
      out.flush();
    } catch (IOException ioe) {
      throw new UApproveException(
          "An IOException occurred trying to write the loginfo.xml file ["
              + filename + "].");
    } finally {
      try {
        out.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public UserLogInfo addUserLogInfoData(String username, String version,
      String date, String termsVersion, String global, String providerId,
      String attributesReleased) {
    UserLogInfo userInfo = new UserLogInfo(username, version, date,
        termsVersion, global);
    if (providerId != null && attributesReleased != null)
      userInfo.addProviderId(providerId, attributesReleased);
    logInfo.data.put(username, userInfo);
    return userInfo;
  }

  public void addUserLogInfoData(String username, String version, String date,
      String termsVersion, String sGlobal, Map<String, String> mapProviderIds) {
    logInfo.data.put(username, new UserLogInfo(username, version, date,
        termsVersion, sGlobal, mapProviderIds));
  }

  // not implemented in LogInfoFile subclass
  public synchronized void updateProviderAccess(String theUsername,
      String theProvider, boolean bGlobal) throws UApproveException {
    return;
  }

  // not implemented in LogInfoFile subclass
  public synchronized void updateProviderAccessWithNoARA(String theUsername,
      String theProvider) throws UApproveException {
    return;
  }

}
