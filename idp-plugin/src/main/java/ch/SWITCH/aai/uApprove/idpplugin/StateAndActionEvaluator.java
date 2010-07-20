package ch.SWITCH.aai.uApprove.idpplugin;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.shibboleth.idp.authn.AuthenticationException;
import edu.internet2.middleware.shibboleth.idp.authn.LoginContext;
import edu.internet2.middleware.shibboleth.idp.session.ServiceInformation;
import edu.internet2.middleware.shibboleth.idp.session.Session;

public class StateAndActionEvaluator {

	private static enum State {NO_LOGIN_CONTEXT, NO_SESSION, AUTH_FAILURE, NOT_SPECIFIC_AUTH_CTX, PRINCIPAL_AUTHENTICATED, UNKNOWN};
	public static enum Action {PASS_TO_IDP, CHECK_ACCESS, UNKNOWN};
	private final Logger logger = LoggerFactory.getLogger(StateAndActionEvaluator.class);
	private final Dispatcher dispatcher;
	
	public StateAndActionEvaluator(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}
	
	private State evaluateState(HttpServletRequest request, String authnContextClassRef) {
		LoginContext loginContext = dispatcher.getLoginContext(request);
		if (loginContext == null) {
			return State.NO_LOGIN_CONTEXT;
		}
		
		logLoginContext(loginContext);
		
		Session idpSession = dispatcher.getIdPSession(request);
		if (idpSession == null) {
			return State.NO_SESSION;
		}
		
		logIdPSession(idpSession);
		
		AuthenticationException authenticationFailure = loginContext.getAuthenticationFailure();
		if (authenticationFailure != null) {
	        return State.AUTH_FAILURE;
		}
	   
		if (authnContextClassRef != null && !authnContextClassRef.equals(loginContext.getAuthenticationMethod())) {
			return State.NOT_SPECIFIC_AUTH_CTX;
		}
   
		if (loginContext.isPrincipalAuthenticated()) {
			return State.PRINCIPAL_AUTHENTICATED;
		}
   
		return State.UNKNOWN;
	}
	
	public Action evaluateAction(HttpServletRequest request, String authnContextClassRef) {
		State state = evaluateState(request, authnContextClassRef);
		logger.debug("State evaluated is {}", state);
		
		switch (state) {
			case NO_LOGIN_CONTEXT:
				return Action.PASS_TO_IDP;
			case NO_SESSION:
				return Action.PASS_TO_IDP;
			case AUTH_FAILURE:
				return Action.PASS_TO_IDP;
			case NOT_SPECIFIC_AUTH_CTX:
				return Action.PASS_TO_IDP;
			case PRINCIPAL_AUTHENTICATED:
				return Action.CHECK_ACCESS;
		}
		
		return Action.UNKNOWN;
		
	}

	private void logLoginContext(LoginContext loginContext) {
		logger.trace("=== LoginContext ===");
	  	if (loginContext == null) {
	  		logger.trace("NULL");
		} else {
			logger.trace("LoginContext: {}", loginContext);
			logger.trace("SessionID:    {}", loginContext.getSessionID());
			logger.trace("Principal:    {}", loginContext.getPrincipalName());	
			logger.trace("RelyingParty: {}", loginContext.getRelyingPartyId());
			logger.trace("AuthMethod:   {}", loginContext.getAuthenticationMethod());
			logger.trace("AuthTime:     {}", loginContext.getAuthenticationInstant());
		}
	  	logger.trace("====================");
	  }
	
	private void logIdPSession(Session idpSession) {
		logger.trace("=== IdP Session ===");
			if (idpSession == null) {
				logger.trace("NULL");
			} else {
				logger.trace("IdPSession:   {}", idpSession);
				logger.trace("SessionID:    {}", idpSession.getSessionID());
				logger.trace("Principal:    {}", idpSession.getPrincipalName());
				logger.trace("Subject:      {}", idpSession.getSubject());	
				logger.trace("LastActivity: {}", idpSession.getLastActivityInstant());
				logger.trace("ServiceInformation:");
				for (String key: idpSession.getServicesInformation().keySet()) {
					ServiceInformation info = idpSession.getServicesInformation().get(key);
					logger.trace("  Service {}: entityId = {}", key, info.getEntityID());
				}
			}
		logger.trace("====================");
	  }
}
