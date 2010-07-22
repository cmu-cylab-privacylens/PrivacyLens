package ch.SWITCH.aai.uApprove.idpplugin.workaround;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.shibboleth.idp.util.HttpServletHelper;

public class CookieFilterableRequest extends HttpServletRequestWrapper {

	private static final Logger logger = LoggerFactory.getLogger(CookieFilterableRequest.class);
	
	public CookieFilterableRequest(HttpServletRequest request) {
		super(request);
		logger.trace("wrapped FilteredCookieRequest created");
    }
	
	public Cookie[] getCookies() {
		Cookie[] cookies = super.getCookies();
		if (cookies == null) {
			logger.trace("no cookies found in wrapped request");
			return null;
		}
			
		List<Cookie> filteredCookies = new ArrayList<Cookie>();			
		for (Cookie cookie: cookies) {
			if (!cookie.getName().equalsIgnoreCase(HttpServletHelper.LOGIN_CTX_KEY_NAME)) {
				filteredCookies.add(cookie);
			}
		}
		
		if (filteredCookies.size() == cookies.length) {
			logger.trace("No cookies filtered out from request");
			return cookies;
		}
		
		logger.debug("Cookie {} filtered out fom request", HttpServletHelper.LOGIN_CTX_KEY_NAME);
		
		cookies = new Cookie[filteredCookies.size()]; 
		for (int i = 0; i < filteredCookies.size(); i++) {
			cookies[i] = filteredCookies.get(i);
		}
		
		return cookies;		
	}
}
