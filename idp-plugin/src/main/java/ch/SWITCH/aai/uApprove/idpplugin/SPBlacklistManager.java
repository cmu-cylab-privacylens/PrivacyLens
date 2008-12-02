/*
 * Class SPBlacklistManager
 *
 * Copyright (c) 2005-2006 SWITCH - The Swiss Education & Research Network 
 *
 *
 * Purpose: Handling of all the sp blacklist, 
 *          a list of provider ids, where the idp plugin
 *          should not be active.
 *
 *
 * @author C.Witzig
 * @date 4.11.2006
 * 
 */

package ch.SWITCH.aai.uApprove.idpplugin;

import java.util.ArrayList;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.FileReader;
import java.io.BufferedReader;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import ch.SWITCH.aai.uApprove.components.UApproveException;

/**
 * Class SPBlacklistManager
 * 
 * This class reads a list of Shibboleth provider ids in regex format from an
 * .txt file. Whenever the idp plugin gets activated, it checks whether the
 * provider id matches one of these regular patterns and if so, then the
 * idp plugin lets the request immediately through. In other words, the blacklist
 * file contains a regex list of provider ids, where the idp plugin is turned
 * off. Use this class with caution as you can disable the idp plugin and forget
 * about it.
 * 
 * 
 * The class is implemented as a Singleton and protected by by synchronized
 * statements (as the list of regex's within the class is not thread safe).
 * 
 */

public class SPBlacklistManager {
  private static SPBlacklistManager blacklistManager = null;
  private static ArrayList<Pattern> blackList;
  private static Logger LOG = LoggerFactory.getLogger(SPBlacklistManager.class);
  
  private SPBlacklistManager(String filename) {
    LOG.debug("SPBlacklistManager initialization from file="+filename);
    blackList = new ArrayList<Pattern>();
    try {
      if (filename == null) throw new UApproveException("black list file is null");
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("#") || line.startsWith("//")
            || line.length() == 0)
          continue;
        blackList.add(Pattern.compile(line));
      }
    } catch (Exception e) {
      LOG.warn("Unable to load black list file={}. Black list is initialized empty", filename);
    }
  }


  public static synchronized void initialize(String filename) {
    if (blacklistManager == null)
      blacklistManager = new SPBlacklistManager(filename);
  }

  /**
   * Returns true if the item is matched by one of the regex patterns.
   */
  public static synchronized boolean containsItem(String item) {
    for (int i = 0; i < blackList.size(); i++) {
      Pattern pattern = blackList.get(i);
      Matcher matcher = pattern.matcher(item);
      if (matcher.find())
           return true;
      }
    return false;
  }

}
