package ch.SWITCH.aai.uApprove.idpplugin.util;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.shibboleth.idp.authn.LoginContext;
import edu.internet2.middleware.shibboleth.idp.session.Session;
import edu.internet2.middleware.shibboleth.idp.util.HttpServletHelper;

public class RequestLoggingFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
	private ServletContext context;
	
	public void destroy() {}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		
		String uri = httpRequest.getRequestURI() + "?" +httpRequest.getQueryString();
		Cookie lcCookie = HttpServletHelper.getCookie(httpRequest, HttpServletHelper.LOGIN_CTX_KEY_NAME);
		LoginContext loginContext = HttpServletHelper.getLoginContext(HttpServletHelper.getStorageService(context), context, httpRequest);
				
		logger.debug("Request to IdP\n" +
				"  URI         : " + uri + "\n" +
				"  lcCookie    : " + cookieToString(lcCookie) + "\n" +
				"  loginContext: " + lcToString(loginContext)			
		);

		// Add here logging statements of your choice by using the log* methods listed below
		
		chain.doFilter(request, response);
	}
	
	private String cookieToString(Cookie cookie) {
		return cookie == null ? "NULL" : cookie.getName() + ":" + cookie.getValue();
	}
	
	private String lcToString(LoginContext lc) {
		if (lc == null)
			return "NULL";
		
		String principal = lc.isPrincipalAuthenticated() ? lc.getPrincipalName() : "NULL";
		return "LoginContext[Principal: "+principal+"|Provider: "+lc.getRelyingPartyId();
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		context = filterConfig.getServletContext();
	}
	
	public static void logLoginContext(LoginContext loginContext) {
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
	
	public static void logIdPSession(Session idpSession) {
		logger.trace("=== IdP Session ===");
			if (idpSession == null) {
				logger.trace("NULL");
			} else {
				logger.trace("SessionID:    {}", idpSession.getSessionID());
				logger.trace("Principal:    {}", idpSession.getPrincipalName());
			}
		logger.trace("====================");
	  }
	
	public static void logCookies(HttpServletRequest request) {
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
		logger.trace("===============");
	}
	
	@SuppressWarnings("unchecked")
	public static void logRequestParameter(HttpServletRequest request) {
		Map<String, String[]> parameters = request.getParameterMap();
		logger.trace("=== Request parameters ===");
		if (parameters == null) {
			logger.trace("NULL");
		} else {
			for (String key : parameters.keySet()) {
				logger.trace("{}:{}", key, parameters.get(key));
			}
		}
		logger.trace("==========================");
	}

}
