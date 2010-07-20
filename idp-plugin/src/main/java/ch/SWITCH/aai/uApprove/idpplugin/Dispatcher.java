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
import edu.internet2.middleware.shibboleth.idp.session.Session;
import edu.internet2.middleware.shibboleth.idp.util.HttpServletHelper;

public class Dispatcher {

	private final Logger logger = LoggerFactory.getLogger(Crypt.class);
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
		  LoginContext loginContext = HttpServletHelper.getLoginContext(HttpServletHelper.getStorageService(servletContext), servletContext, request);	  
		  return loginContext;
	}
	
	public Session getIdPSession(HttpServletRequest request) {
		  Session idpSession = HttpServletHelper.getUserSession(request);
		  return idpSession;
	}
	
	private void transferLoginContextToIdP(HttpServletRequest request, HttpServletResponse response) {
		LoginContext loginContext = getLoginContext(request);
		logger.debug("Transfer LoginContext to IdP");
		HttpServletHelper.unbindLoginContext(HttpServletHelper.getStorageService(servletContext), servletContext, request, response);
		request.setAttribute(HttpServletHelper.LOGIN_CTX_KEY_NAME, null);
		HttpServletHelper.bindLoginContext(loginContext, request);
	}
	
	private void transferLoginContextToViewer(HttpServletRequest request, HttpServletResponse response) {
		LoginContext loginContext = getLoginContext(request);
		logger.debug("Transfer LoginContext to uApprove");
		HttpServletHelper.bindLoginContext(loginContext, HttpServletHelper.getStorageService(servletContext), servletContext, request, response);	  
	}

	public void dispatchToIdP(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws UApproveException {
		transferLoginContextToIdP(request, response);
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

	 	String referer = request.getRequestURL().toString();   
	 	String postForm = buildPostForm(context.resetConsent(), referer, context.getPrincipal(), context.getRelyingParty(), context.getAttributesReleased());
	    response.setContentType("text/html");
	    
	    transferLoginContextToViewer(request, response);
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
