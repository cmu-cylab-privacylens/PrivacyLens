package ch.SWITCH.aai.uApprove.idpplugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.util.Locale;

import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.SWITCH.aai.uApprove.components.UApproveException;
import ch.SWITCH.aai.uApprove.components.Attribute;
import edu.internet2.middleware.shibboleth.common.attribute.AttributeRequestException;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.SAML2AttributeAuthority;
import edu.internet2.middleware.shibboleth.common.profile.provider.BaseSAMLProfileRequestContext;
import edu.internet2.middleware.shibboleth.common.relyingparty.RelyingPartyConfiguration;
import edu.internet2.middleware.shibboleth.common.relyingparty.provider.SAMLMDRelyingPartyConfigurationManager;

public class AttributeDumper {
  private static Logger LOG = LoggerFactory.getLogger(AttributeDumper.class);

  private static AttributeDumper dumper;

  private static SAML2AttributeAuthority saml2AA;
  private static SAMLMDRelyingPartyConfigurationManager relyingPartyConfigurationManager;

  // create the dumper instance and initialize it
  private AttributeDumper(SAML2AttributeAuthority saml2AA, SAMLMDRelyingPartyConfigurationManager relyingPartyConfigurationManager){
    this.saml2AA = saml2AA;
    this.relyingPartyConfigurationManager = relyingPartyConfigurationManager;
  }

  public synchronized static void initialize(SAML2AttributeAuthority saml2AA, SAMLMDRelyingPartyConfigurationManager relyingPartyConfigurationManager) {
    if (dumper == null) 
        dumper = new AttributeDumper(saml2AA, relyingPartyConfigurationManager);
    LOG.info("AttributeDumper initialized");
  }

  public static Collection<Attribute> getAttributes(String username,
      String spEntityId) throws UApproveException {
    if (dumper == null)
      throw new UApproveException("AttributeDumper is not initializied");

    // build request context
    BaseSAMLProfileRequestContext requestCtx = new BaseSAMLProfileRequestContext();
    RelyingPartyConfiguration relyingPartyConfiguration= relyingPartyConfigurationManager.getRelyingPartyConfiguration(spEntityId);
    MetadataProvider metadataProvider = relyingPartyConfigurationManager.getMetadataProvider();
    
    String idpEntityId = relyingPartyConfiguration.getProviderId();
    
    LOG.debug("metadataProvider={}",metadataProvider);
    LOG.debug("spEntityId={}",spEntityId);
    LOG.debug("idpEntityId={}",idpEntityId);
    
    requestCtx.setRelyingPartyConfiguration(relyingPartyConfiguration);
    requestCtx.setInboundMessageIssuer(spEntityId);
    requestCtx.setOutboundMessageIssuer(idpEntityId);
    requestCtx.setPrincipalName(username);
    requestCtx.setLocalEntityId(idpEntityId);
    requestCtx.setPeerEntityId(spEntityId);

    try {
      requestCtx.setLocalEntityMetadata(metadataProvider.getEntityDescriptor(idpEntityId));
      requestCtx.setPeerEntityMetadata(metadataProvider.getEntityDescriptor(spEntityId));
    } catch (MetadataProviderException e) {
      throw new UApproveException(e);
    }
    requestCtx.setMetadataProvider(metadataProvider);
    
    Map<String, BaseAttribute> attributes;
    try {
      attributes = saml2AA.getAttributes(requestCtx);
    } catch (AttributeRequestException e) {
      LOG.error("Unable to retrieve attributes Message: " + e.getMessage());
      throw new UApproveException("Unable to retrieve attributes Message: "
          + e.getMessage());
    }

    Collection<Attribute> result = new ArrayList<Attribute>();
    LOG.debug("DIRECT OUTPUT FROM RESOLVING");
    for (BaseAttribute attr : attributes.values()) {
     
      Collection<String> attributeValues = new ArrayList<String>();
      for (Iterator iter = attr.getValues().iterator(); iter.hasNext();) {
        Object valueObj = iter.next();
        //String value =  valueObj instanceof String ? (String)valueObj : "Non-string attribute value";
        if (valueObj != null && !valueObj.toString().trim().equals("")) {
          LOG.trace(valueObj.toString());
          attributeValues.add(valueObj.toString());
        }
      }
      
      LOG.debug("Attribute '{}' has {} value(s)", attr.getId(), attributeValues.size());
      
      if (attributeValues.size() == 0)
        continue;
      
      Map<String, String> attributeNames = new HashMap<String, String>();
      for (Iterator<Locale> iter = attr.getDisplayNames().keySet().iterator(); iter.hasNext();) {
        Locale key = iter.next();
        LOG.trace((String)attr.getDisplayNames().get(key));
        attributeNames.put(key.getLanguage(), (String) attr.getDisplayNames().get(key));
      }
      
      Map<String, String> attributeDescriptions = new HashMap<String, String>();
      for (Iterator<Locale> iter = attr.getDisplayDescriptions().keySet().iterator(); iter.hasNext();) {
        Locale key = iter.next();
        LOG.trace((String) attr.getDisplayDescriptions().get(key));
        attributeDescriptions.put(key.getLanguage(), (String) attr.getDisplayDescriptions().get(key));
      }
      
      Attribute a = new Attribute();
      a.attributeID = attr.getId();
      a.attributeNames = attributeNames;
      a.attributeDescriptions = attributeDescriptions;
      a.attributeValues = attributeValues;
      
      result.add(a);
    }

    return result;
  }

}
