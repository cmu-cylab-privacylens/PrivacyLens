package ch.SWITCH.aai.uApprove.idpplugin.workaround;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.shibboleth.idp.authn.LoginContext;
import edu.internet2.middleware.shibboleth.idp.session.Session;
import edu.internet2.middleware.shibboleth.idp.util.HttpServletHelper;


public class Workarounds {
	
	private static final Logger logger = LoggerFactory.getLogger(Workarounds.class);

	public static HttpServletResponse wrapResponse(HttpServletResponse wrappedResponse) {
		return new CookieAwareResponse(wrappedResponse);
	}
	
	public static LoginContext getLoginContext(ServletContext servletContext, HttpServletRequest request) {		
		
		
		// workaround remove cookie from request, although response not yet sent
		HttpServletRequest filteredRequest = removeLoginContextCookie(request);	
		LoginContext loginContext = HttpServletHelper.getLoginContext(HttpServletHelper.getStorageService(servletContext), servletContext, filteredRequest);	  
		logger.trace("Filtered LoginContext {} retrieved", loginContext);
		return loginContext;
	}
	
	private static HttpServletRequest removeLoginContextCookie(HttpServletRequest request) {
	
		int count = request.getAttribute("_uApprove_request_seen") == null ? 0 : (Integer) request.getAttribute("_uApprove_request_seen");		
		logger.debug("Seen this request {} times", count);	
		request.setAttribute("_uApprove_request_seen", count+1);
		
		
		//logger.trace("Process request {}", request);
		logLoginContext(HttpServletHelper.getLoginContext(request));
		logCookies(request);
		//logger.trace("Attributes names {}", request.getAttributeNames());
		
		/*if (count == 0)
			return request;*/
		
		if (request.getAttribute(HttpServletHelper.LOGIN_CTX_KEY_NAME) == null) {
			HttpServletRequest filteredRequest = new FilteredCookieRequest(request);
			logger.trace("Replace request by {}", filteredRequest);
			return filteredRequest;
		}
		
		return request;
	}
		
	private static void logLoginContext(LoginContext loginContext) {
		logger.trace("=== LoginContext ===");
	  	if (loginContext == null) {
	  		logger.trace("NULL");
		} else {
			logger.trace("LoginContext: {}", loginContext);
			logger.trace("SessionID:    {}", loginContext.getSessionID());
			logger.trace("Principal:    {}", loginContext.getPrincipalName());	
			logger.trace("RelyingParty: {}", loginContext.getRelyingPartyId());
		}
	  	logger.trace("====================");
	  }
	
	private static void logIdPSession(Session idpSession) {
		logger.trace("=== IdP Session ===");
			if (idpSession == null) {
				logger.trace("NULL");
			} else {
				logger.trace("SessionID:    {}", idpSession.getSessionID());
				logger.trace("Principal:    {}", idpSession. getPrincipalName());
				/*
				logger.trace("ServiceInformation:");
				for (String key: idpSession.getServicesInformation().keySet()) {
					ServiceInformation info = idpSession.getServicesInformation().get(key);
					logger.trace("  Service {}: entityId = {}", key, info.getEntityID());
				}
				*/
			}
		logger.trace("====================");
	  }
	
	private static void logCookies(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		logger.trace("=== Cookies ===");
		if (cookies == null) {
			logger.trace("NULL");
		} else {
			for (Cookie cookie : cookies) {
				if (cookie.getName().startsWith("_idp_")) {
					logger.trace("{}", cookie.getName());
				}
			}
		}
		logger.trace("====================");
	}
}
