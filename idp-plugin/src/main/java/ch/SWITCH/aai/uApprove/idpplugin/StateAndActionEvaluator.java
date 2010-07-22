package ch.SWITCH.aai.uApprove.idpplugin;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.SWITCH.aai.uApprove.idpplugin.workaround.Workarounds;

import edu.internet2.middleware.shibboleth.idp.authn.AuthenticationException;
import edu.internet2.middleware.shibboleth.idp.authn.LoginContext;

public class StateAndActionEvaluator {

	private static enum State {NO_LOGIN_CONTEXT, AUTH_FAILURE, NOT_SPECIFIC_AUTH_CTX, PRINCIPAL_AUTHENTICATED, UNKNOWN};
	public static enum Action {PASS_TO_IDP, CHECK_ACCESS, UNKNOWN};
	private final Logger logger = LoggerFactory.getLogger(StateAndActionEvaluator.class);
	private final Dispatcher dispatcher;
	
	public StateAndActionEvaluator(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}
	
	private State evaluateState(HttpServletRequest request, String authnContextClassRef) {    

		LoginContext loginContext = Workarounds.getLoginContext(dispatcher.getServletContext(), request);
		if (loginContext == null) {
			return State.NO_LOGIN_CONTEXT;
		}
		
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
			case AUTH_FAILURE:
				return Action.PASS_TO_IDP;
			case NOT_SPECIFIC_AUTH_CTX:
				return Action.PASS_TO_IDP;
			case PRINCIPAL_AUTHENTICATED:
				return Action.CHECK_ACCESS;
		}
		
		return Action.UNKNOWN;
		
	}


}
