package ch.SWITCH.aai.uApprove.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Attribute implements Serializable {

  private final static Logger logger = LoggerFactory.getLogger(Attribute.class);
  
  private static final long serialVersionUID = -7555070485991286595L;
  private static final String ATTR_DELIMITER = ":";

  public String attributeID;
  public Map<String, String> attributeNames;
  public Map<String, String> attributeDescriptions;
  public Collection<String> attributeValues;


// This function extracts all the attribute names, is the the storage format.
// Keep this function for backwards compatibility and use it for all
// interaction with the storage: store, load, comparing
// Format: ":attr1:attr2:attr3:" || ""
  public static String serializeAttributeIDs(Collection<Attribute> attributes) {
	  String result = "";
	  for (Iterator<Attribute> iterator = attributes.iterator(); iterator.hasNext();) {
		  String id = iterator.next().attributeID;
		  result += ATTR_DELIMITER + id;
	  }
	  if (!result.equals("")) {
		  result += ATTR_DELIMITER;
	  }
	  return result;
  }


  public static boolean compareAttributeRelease(String approved, String current) {
  
	  if (current.equals("")) {
		  return true;
	  }
	  
	  if (approved == null) {
		  return false;
	  }
	
	  StringTokenizer tokenizer = new StringTokenizer( current,ATTR_DELIMITER );
	  while (tokenizer.hasMoreElements()) {
		  String attr = (String) tokenizer.nextElement();
		  if (approved.indexOf(ATTR_DELIMITER+attr+ATTR_DELIMITER) == -1) {
			  return false;
		  }
	  }
	  return true;
  }



@SuppressWarnings("unchecked")
public static String serializeAttributes(Collection<Attribute> attributes) {

  JSONArray result = new JSONArray();
  for (Iterator<Attribute> attrIter = attributes.iterator(); attrIter.hasNext();) {
    JSONObject attribute = new JSONObject();
    Attribute attr = attrIter.next();

    JSONObject names = new JSONObject();
    for (Iterator<String> nameIter = attr.attributeNames.keySet().iterator(); nameIter.hasNext();) {
      String key = nameIter.next();
      names.put(key, attr.attributeNames.get(key));
    }

    JSONObject descs = new JSONObject();
    for (Iterator<String> descIter = attr.attributeDescriptions.keySet().iterator(); descIter.hasNext();) {
      String key = descIter.next();
      descs.put(key, attr.attributeDescriptions.get(key));
    }

    JSONArray values = new JSONArray();
    for (Iterator<String> valueIter = attr.attributeValues.iterator(); valueIter.hasNext();) {
      values.add(valueIter.next());
    }

    attribute.put("attributeId", attr.attributeID);
    attribute.put("attributeNames", names);
    attribute.put("attributeDescriptions", descs);
    attribute.put("attributeValues", values);

    result.add(attribute);
  }
  // return JSONObject.escape(result.toString());
  logger.trace("Serialized RelyingParty: {}", result);
  return result.toString();
}

@SuppressWarnings("unchecked")
public static Collection<Attribute> unserializeAttributes(String serializedAttributes) {

  Collection<Attribute> result = new ArrayList<Attribute>();
  for (Iterator<JSONObject> attrIter = ((JSONArray)JSONValue.parse(serializedAttributes)).iterator(); attrIter.hasNext();) {
    JSONObject attribute = attrIter.next();

    Map<String, String> attributeNames = new HashMap<String, String>();
    JSONObject names = (JSONObject)JSONValue.parse(attribute.get("attributeNames").toString());
    for (Iterator<String> nameIter = names.keySet().iterator(); nameIter.hasNext();) {
      String key = nameIter.next();
      attributeNames.put(key, (String)names.get(key));
    }

    Map<String, String> attributeDescriptions = new HashMap<String, String>();
    JSONObject descs = (JSONObject)JSONValue.parse(attribute.get("attributeDescriptions").toString());
    for (Iterator<String> descIter = descs.keySet().iterator(); descIter.hasNext();) {
      String key = descIter.next();
      attributeDescriptions.put(key, (String)descs.get(key));
    }

    Collection<String> attributeValues = new ArrayList<String>();
    for (Iterator<String> valueIter = ((JSONArray)JSONValue.parse(attribute.get("attributeValues").toString())).iterator(); valueIter.hasNext();) {
      attributeValues.add(valueIter.next());
    }

    Attribute a = new Attribute();
    a.attributeID = (String)attribute.get("attributeId");
    a.attributeNames = attributeNames;
    a.attributeDescriptions = attributeDescriptions;
    a.attributeValues = attributeValues;

    result.add(a);
  }

  return result;
}

}
