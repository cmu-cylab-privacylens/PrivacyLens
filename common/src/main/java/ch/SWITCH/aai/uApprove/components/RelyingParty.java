package ch.SWITCH.aai.uApprove.components;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.opensaml.saml2.metadata.ServiceDescription;
import org.opensaml.saml2.metadata.ServiceName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelyingParty implements Serializable {

	private static final long serialVersionUID = 4022033377848736850L;

	private final Logger logger = LoggerFactory.getLogger(RelyingParty.class);

	
	private String entityId;
	private Map<String, String> rpNames;
	private Map<String, String> rpDescriptions;
	
	public RelyingParty(String entityId, List<ServiceName> names, List<ServiceDescription> descriptions) {
		this.entityId = entityId;
		
		rpNames = new HashMap<String, String>();
		for (ServiceName element : names) {
			rpNames.put(element.getName().getLanguage(), element.getName().getLocalString());
		}

		rpDescriptions = new HashMap<String, String>();
		for (ServiceDescription element : descriptions) {
			rpDescriptions.put(element.getDescription().getLanguage(), element.getDescription().getLocalString());
		}	
	}
	
	public RelyingParty(String serialized) {
		JSONObject rpSerialized = (JSONObject) JSONValue.parse(serialized);
		this.entityId = (String) rpSerialized.get("entityId");
		
		JSONObject rpNamesSerialized = (JSONObject) rpSerialized.get("rpNames");
		rpNames = new HashMap<String, String>();
		for (Object key : rpNamesSerialized.keySet()) {
			rpNames.put((String) key, (String) rpNamesSerialized.get(key));
		}
		
		JSONObject rpDescriptionsSerialized = (JSONObject) rpSerialized.get("rpDescriptions");
		rpDescriptions = new HashMap<String, String>();
		for (Object key : rpDescriptionsSerialized.keySet()) {
			rpDescriptions.put((String) key, (String) rpDescriptionsSerialized.get(key));
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public String serialize() {
		logger.trace("Attempt to serialize data for {}", entityId);
		JSONObject result = new JSONObject();
		
		logger.trace("Attempt to serialize names {}", rpNames);
	    JSONObject rpNamesSerialized = new JSONObject();
	    for (String key : rpNames.keySet()) {
	    	rpNamesSerialized.put(key, rpNames.get(key));
	    }	    
		logger.trace("Serialized names {}", rpNamesSerialized);
	    
		logger.trace("Attempt to serialize description {}", rpDescriptions);
	    JSONObject rpDescriptionsSerialized = new JSONObject();
	    for (String key : rpDescriptions.keySet()) {
	    	rpDescriptionsSerialized.put(key, rpDescriptions.get(key));
	    }    
		logger.trace("Serialized descriptions {}", rpDescriptionsSerialized);
		
		result.put("entityId", entityId);
		result.put("rpNames", rpNamesSerialized);
		result.put("rpDescriptions", rpDescriptionsSerialized);
		
		logger.trace("Serialized data '{}'", result);
		return result.toString();
	}

	public String getEntityId() {
		return entityId;
	}

	public Map<String, String> getRpNames() {
		return rpNames;
	}

	public Map<String, String> getRpDescriptions() {
		return rpDescriptions;
	}
	
	

}
