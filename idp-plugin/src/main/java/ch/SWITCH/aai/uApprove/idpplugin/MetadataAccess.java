package ch.SWITCH.aai.uApprove.idpplugin;

import java.util.ArrayList;
import java.util.List;

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.AttributeConsumingService;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.ServiceDescription;
import org.opensaml.saml2.metadata.ServiceName;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.SWITCH.aai.uApprove.components.RelyingParty;
import ch.SWITCH.aai.uApprove.components.UApproveException;

import edu.internet2.middleware.shibboleth.common.relyingparty.provider.SAMLMDRelyingPartyConfigurationManager;

public class MetadataAccess {
	private static Logger LOG = LoggerFactory.getLogger(MetadataAccess.class);
	private MetadataProvider metadataProvider;
	
	public MetadataAccess(SAMLMDRelyingPartyConfigurationManager relyingPartyConfigurationManager) {
	  metadataProvider = relyingPartyConfigurationManager.getMetadataProvider();
	}
	
	public RelyingParty getRelyingPartyInfo(String entityId) throws UApproveException {
		LOG.debug("RelyingParty info for entityId {}", entityId);
		EntityDescriptor entityDescriptor = null;
		try {
			entityDescriptor = metadataProvider.getEntityDescriptor(entityId);
		} catch (MetadataProviderException e) {
			LOG.error("MetadataProviderException", e);
			throw new UApproveException(e);
		}
		LOG.debug("entityDescriptor {}", entityDescriptor);
		
		AttributeConsumingService attrService = getAttributeConsumingService(entityDescriptor);
		
		if (attrService != null)
			return new RelyingParty(entityId, attrService.getNames(), attrService.getDescriptions());
		else
			return new RelyingParty(entityId, new ArrayList<ServiceName>(), new ArrayList<ServiceDescription>());
	}
	
	private AttributeConsumingService getAttributeConsumingService(EntityDescriptor entityDescriptor) {
	
		String[] protocols = {SAMLConstants.SAML20P_NS, SAMLConstants.SAML11P_NS, SAMLConstants.SAML10P_NS};

		AttributeConsumingService result = null;
		List<AttributeConsumingService> list;
		for (String protocol: protocols) {
			SPSSODescriptor spSSODescriptor = entityDescriptor.getSPSSODescriptor(protocol);
			LOG.debug("SPSSODescriptor for protocol {} is {}", protocol, spSSODescriptor);
			
			if (spSSODescriptor == null) {
				continue;
			}
			
			result = spSSODescriptor.getDefaultAttributeConsumingService();
			if (result != null) {
				LOG.debug("DefaultAttributeConsumingService found");
				return result;
			}
				
			list = spSSODescriptor.getAttributeConsumingServices();
			if (list != null && !list.isEmpty()) {
				LOG.debug("AttributeConsumingService with index 0 found");
				return list.get(0);
			}
				
		}
		
		return result;
	}
}
