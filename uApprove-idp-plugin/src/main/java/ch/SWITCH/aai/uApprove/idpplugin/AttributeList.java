package ch.SWITCH.aai.uApprove.idpplugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import java.io.FileReader;
import java.io.BufferedReader;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import ch.SWITCH.aai.uApprove.components.Attribute;
import ch.SWITCH.aai.uApprove.components.UApproveException;

public class AttributeList {
  private static AttributeList attributeList = null;
  private static List <String> blackList;
  private static List <String> whiteList;
  private static Logger LOG = LoggerFactory.getLogger(AttributeList.class);
  
  private AttributeList(String filename) {
    LOG.debug("AttributeList initialization from file="+filename);
    blackList = new ArrayList<String>();
    whiteList = new ArrayList<String>();
    try {
      if (filename == null) throw new UApproveException("AttributeList file is null");
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      String line;
      while ((line = reader.readLine()) != null) {
        line.trim();
        if (line.startsWith("#") || line.startsWith("//")
            || line.length() == 0)
          continue;
        
        if (line.startsWith("!"))
          blackList.add(line.substring(1));
        else
          whiteList.add(line);
      }
    } catch (Exception e) {
      LOG.warn("Unable to load AttributeList file={}. AttributeList is initialized empty, Reason: {}", filename, e.getMessage());
    }
  }


  public static synchronized void initialize(String filename) {
    if (attributeList == null) {
    	attributeList = new AttributeList(filename);
    }
    
    LOG.debug("Attribute blacklist: {}", blackList);
    LOG.debug("Attribute sorted whitelist: {}", whiteList);
  }

  public static List <String> getWhiteList() {
      return whiteList;
  }
  
  public static boolean isBlackListed(String attr) {
    return blackList.contains(attr);
  }
  
  public static List<Attribute> sortAttributes(Collection<Attribute> unsortedAttributes) {
	  LOG.trace("Unsorted attributes: {}", getAttributeIds(unsortedAttributes));
	  List<Attribute> attributes = new ArrayList<Attribute>();
	  for (String id : getWhiteList()) {
		  for (Attribute attribute: unsortedAttributes) {
			  if (attribute.attributeID.equals(id)) {
				  attributes.add(attribute);		  
			  }
		  }
	  }
	  
	  for (Attribute attribute: unsortedAttributes) {
		  if (!getWhiteList().contains(attribute.attributeID)) {
			  attributes.add(attribute);		  
		  }
	  }
	  
	  LOG.trace("Sorted attributes: {}", getAttributeIds(attributes));
	  return attributes;
  }
  
  public static Collection<String> getAttributeIds(Collection<Attribute> attributes) {
	  Collection<String> ids = new ArrayList<String>();
	  for (Attribute attribute: attributes) {
		  ids.add(attribute.attributeID);
	  }
	  return ids;
  }
  
  public static Collection<Attribute> removeBlacklistedAttributes(Collection<Attribute> allAttributes) {
	  LOG.trace("Attributes before blacklist removal: {}", getAttributeIds(allAttributes));
	  Collection<Attribute> attributes = new HashSet<Attribute>();
	  for (Attribute attribute: allAttributes) {
		  if (!isBlackListed(attribute.attributeID)) {
			  attributes.add(attribute);
		  }
	  }
	  LOG.trace("Attributes after blacklist removal: {}", getAttributeIds(attributes));
	  return attributes;
  }
}
