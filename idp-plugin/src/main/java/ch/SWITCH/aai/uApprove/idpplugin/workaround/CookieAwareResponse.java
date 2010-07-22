package ch.SWITCH.aai.uApprove.idpplugin.workaround;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.opensaml.xml.util.DatatypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.shibboleth.idp.util.HttpServletHelper;

public class CookieAwareResponse extends HttpServletResponseWrapper {
	
	private static final Logger logger = LoggerFactory.getLogger(CookieAwareResponse.class);
	private boolean loginContextCookieInvalidated = false;

	public CookieAwareResponse(HttpServletResponse response) {
		super(response);
	}
	
	
	public void addCookie(Cookie cookie) {	
		if (DatatypeHelper.safeEquals(cookie.getName(), HttpServletHelper.LOGIN_CTX_KEY_NAME)) {
			if (cookie.getMaxAge() > 0) {
				logger.trace("Cookie {} added", cookie.getName());
				loginContextCookieInvalidated = false;
			} else {
				logger.trace("Cookie {} removed", cookie.getName());
				loginContextCookieInvalidated = true;
			}
		}

		super.addCookie(cookie);
	}

	public boolean isLoginContextCookieInvalidated() {
		return loginContextCookieInvalidated;
	}
	
}
