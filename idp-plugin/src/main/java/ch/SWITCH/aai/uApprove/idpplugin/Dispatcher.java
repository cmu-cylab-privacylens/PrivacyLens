package ch.SWITCH.aai.uApprove.idpplugin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.SWITCH.aai.uApprove.components.Attribute;
import ch.SWITCH.aai.uApprove.components.ConfigurationManager;
import ch.SWITCH.aai.uApprove.components.Crypt;
import ch.SWITCH.aai.uApprove.components.RelyingParty;
import ch.SWITCH.aai.uApprove.components.UApproveException;
import ch.SWITCH.aai.uApprove.idpplugin.UApproveContextBuilder.UApproveContext;
import edu.internet2.middleware.shibboleth.idp.authn.LoginContext;
import edu.internet2.middleware.shibboleth.idp.util.HttpServletHelper;

public class Dispatcher {

	public final static String UAPPROVE_RETURN_INDICATOR_PARAMETER = "uApproveReturn";
	public final static String UAPPROVE_RESET_INDICATOR_PARAMETER = "uApproveReset";
	
	private final Logger logger = LoggerFactory.getLogger(Dispatcher.class);
	private final Crypt crypt;
	private final ServletContext servletContext;
	boolean isPassiveSupported;
	
	public Dispatcher(ServletContext servletContext) throws UApproveException  {
		this.servletContext = servletContext;
		isPassiveSupported  = ConfigurationManager.makeBoolean(ConfigurationManager.getParam(ConfigurationManager.PLUGIN_ISPASSIVE_SUPPORT));
		String sharedSecret = ConfigurationManager.getParam(ConfigurationManager.COMMON_SHARED_SECRET);
		crypt = new Crypt(sharedSecret);
	}
	
	public ServletContext getServletContext() {
		return servletContext;
	}
	
	public LoginContext getLoginContext(HttpServletRequest request) {		
		LoginContext loginContext = HttpServletHelper.getLoginContext(request);
		logger.trace("Retrieve LoginContext from request {} ", loginContext);
		return loginContext;
	}
	
	public void restoreLoginContext(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("Retrieve LoginContext from storage and unbind");
		LoginContext loginContext = HttpServletHelper.getLoginContext(HttpServletHelper.getStorageService(servletContext), servletContext, request);
		HttpServletHelper.unbindLoginContext(HttpServletHelper.getStorageService(servletContext), servletContext, request, response);
		logger.debug("Restore LoginContext '{}' to request", loginContext);
		HttpServletHelper.bindLoginContext(loginContext, request);
	}
	
	public void storeLoginContext(HttpServletRequest request, HttpServletResponse response) {
		LoginContext loginContext = getLoginContext(request);
		logger.debug("Store LoginContext persistent");
		HttpServletHelper.bindLoginContext(loginContext, HttpServletHelper.getStorageService(servletContext), servletContext, request, response);
	}
	
	public void dispatchToIdP(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws UApproveException {
		try {
			logger.debug("Dispatch to IdP");
			filterChain.doFilter(request, response);
		} catch (Exception e) {
			logger.error("Error dispatching to IdP", e);
			throw new UApproveException(e);
		}
	}
	
	public void dispatchToViewer(HttpServletRequest request, HttpServletResponse response, UApproveContext context) throws  UApproveException {
		boolean isPassive = getLoginContext(request).isPassiveAuthRequired();
		
		if (isPassive && isPassiveSupported) {
			throw new UApproveException("Passive authentication is required, uApprove does support it, but can not, because user interaction is required");
		}
	 	
	 	String returnURL = request.getRequestURL().append("?"+UAPPROVE_RETURN_INDICATOR_PARAMETER).toString();
	 	
	 	if (context.resetConsent()) {
	 		returnURL += "&" + UAPPROVE_RESET_INDICATOR_PARAMETER;
	 	}
	 	
	 	logger.debug("Builded returnURL is {}", returnURL);
	 	
	 	String postForm = buildPostForm(context.resetConsent(), returnURL, context.getPrincipal(), context.getRelyingParty(), context.getAttributesReleased());
	    response.setContentType("text/html");
	    
	    storeLoginContext(request, response);
	 	try {
	 		logger.debug("Dispatch to uApprove");
			response.getWriter().write(postForm);
		} catch (IOException e) {
			throw new UApproveException(e);
		}
	}

	// method to continue to the uApprove web application (leave IdP)
	private String buildPostForm(boolean resetConsent,
			String returnURL, String principal, RelyingParty relyingParty,
			Collection<Attribute> attributesReleased)
			throws UApproveException {

		// action URL format:
		// https://host.domain.com/uApprove/Controller/?returnUrl=https://idp.domain.com/Authn/RemoteUser&editsettings=true;
		String action = ConfigurationManager.getParam(ConfigurationManager.PLUGIN_VIEWER_DEPLOYPATH);
		action += "?" + ConfigurationManager.HTTP_PARAM_RETURNURL + "=";
		try {
			action += URLEncoder.encode(returnURL, "utf-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("Encoding returnURL", e);
			throw new UApproveException(e);
		}
		action += resetConsent ? "&" + ConfigurationManager.HTTP_PARAM_RESET + "=true" : "";

		String postForm = "<html>"
				+ "   <body onload=\"document.forms[0].submit()\">"
				+ "     <noscript>"
				+ "       <p>"
				+ "         <strong>Note:</strong> Since your browser does not support JavaScript,"
				+ "         you must press the Continue button once to proceed."
				+ "       </p>" + "     </noscript>" + "     <form action=\""
				+ action
				+ "\" method=\"post\">"
				+ "       <input type=\"hidden\" name=\""
				+ ConfigurationManager.HTTP_PARAM_PRINCIPAL
				+ "\" value=\""
				+ crypt.encrypt(principal)
				+ "\"/>"
				+ "       <input type=\"hidden\" name=\""
				+ ConfigurationManager.HTTP_PARAM_RELYINGPARTY
				+ "\" value=\""
				+ crypt.encrypt(relyingParty.serialize())
				+ "\"/>"
				+ "       <input type=\"hidden\" name=\""
				+ ConfigurationManager.HTTP_PARAM_ATTRIBUTES
				+ "\" value=\""
				+ crypt.encrypt(Attribute
						.serializeAttributes(attributesReleased))
				+ "\"/>"
				+ "       <noscript>"
				+ "           <input type=\"submit\" value=\"Continue\"/>"
				+ "       </noscript>"
				+ "     </form>"
				+ "   </body>"
				+ " </html>";
		return postForm;
	}

}
