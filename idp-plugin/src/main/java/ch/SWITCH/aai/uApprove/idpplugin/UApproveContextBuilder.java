package ch.SWITCH.aai.uApprove.idpplugin;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import ch.SWITCH.aai.uApprove.components.Attribute;
import ch.SWITCH.aai.uApprove.components.ConfigurationManager;
import ch.SWITCH.aai.uApprove.components.RelyingParty;
import ch.SWITCH.aai.uApprove.components.UApproveException;
import edu.internet2.middleware.shibboleth.common.attribute.provider.SAML2AttributeAuthority;
import edu.internet2.middleware.shibboleth.common.relyingparty.provider.SAMLMDRelyingPartyConfigurationManager;
import edu.internet2.middleware.shibboleth.idp.authn.LoginContext;

public class UApproveContextBuilder {
	
	private final Dispatcher dispatcher;
	private final MetadataAccess metadataAccess;
	
	private static final String SAML2_AA_ID = "shibboleth.SAML2AttributeAuthority";
	private static final String RP_CM_ID = "shibboleth.RelyingPartyConfigurationManager";
	
	
	UApproveContextBuilder(Dispatcher dispatcher) throws UApproveException {
		
		this.dispatcher = dispatcher;
		
		SAML2AttributeAuthority saml2AA = (SAML2AttributeAuthority) dispatcher.getServletContext().getAttribute(SAML2_AA_ID);
		if (saml2AA == null)
            throw new UApproveException("Unable to load SAML2 AA from shibboleth idp context");
          
        SAMLMDRelyingPartyConfigurationManager relyingPartyConfigurationManager = (SAMLMDRelyingPartyConfigurationManager) dispatcher.getServletContext().getAttribute(RP_CM_ID);
        if (relyingPartyConfigurationManager == null)
            throw new UApproveException("Unable to load RelyingPartyConfigurationManager from shibboleth idp context");

        // metadata access 
        metadataAccess = new MetadataAccess(relyingPartyConfigurationManager);

        // initialize AttributeDumper
        AttributeDumper.initialize(saml2AA, relyingPartyConfigurationManager);       
	}
	
	public UApproveContext buildContext(HttpServletRequest request) throws UApproveException {
		  LoginContext loginContext = dispatcher.getLoginContext(request);

		  String principal = loginContext.getPrincipalName();
		  
		  if (principal == null || "".equals(principal))
			  throw new UApproveException("No principal found, assure authentication");
		  
	      RelyingParty relyingParty = metadataAccess.getRelyingPartyInfo(loginContext.getRelyingPartyId());

	      // get the attributes released for user and relying party;
	      Collection<Attribute> attributesReleased = AttributeDumper.getAttributes(principal, relyingParty.getEntityId());
		  
	      boolean resetConsent = request.getParameter(ConfigurationManager.HTTP_PARAM_RESET) != null;

	      return new UApproveContext(principal, relyingParty, attributesReleased, resetConsent);
	}
	
	public class UApproveContext {
	
		private final String principal;
		private final RelyingParty relyingParty;
		private final Collection<Attribute> attributesReleased;
		private final boolean resetConsent;
		
		private UApproveContext(String principal, RelyingParty relyingParty, Collection<Attribute> attributesReleased, boolean resetConsent) {
			this.principal = principal;
			this.relyingParty = relyingParty;
			this.attributesReleased = attributesReleased;
			this.resetConsent = resetConsent;
		}
		
		public String getPrincipal() {
			return principal;
		}
		
		public RelyingParty getRelyingParty() {
			return relyingParty;
		}
		
		public Collection<Attribute> getAttributesReleased() {
			return attributesReleased;
		}
		
		public boolean resetConsent() {
			return resetConsent;
		}
	
		public String toString() {
			return principal+" --> "+relyingParty.getEntityId();
		}
	}
	
}
