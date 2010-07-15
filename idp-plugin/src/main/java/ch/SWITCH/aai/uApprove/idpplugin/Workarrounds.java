package ch.SWITCH.aai.uApprove.idpplugin;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import edu.internet2.middleware.shibboleth.idp.util.HttpServletHelper;


public class Workarrounds {
	
	private static Logger LOG = LoggerFactory.getLogger(Workarrounds.class);
	
	public static HttpServletRequest removeLoginContextCookie(HttpServletRequest request) {
		return new FilteredCookieRequest(request);
	}
	
	private static class FilteredCookieRequest extends HttpServletRequestWrapper {
		
		private static Logger LOG = LoggerFactory.getLogger(FilteredCookieRequest.class);
		
		public FilteredCookieRequest(HttpServletRequest request) {
			super(request);
	    }
		
		public Cookie[] getCookies() {
			LOG.debug("overwritten");
			Cookie[] cookies = super.getCookies();
			if (cookies == null)
				return null;
		    
			for (Cookie cookie: cookies) {
				if (cookie.getName().equalsIgnoreCase(HttpServletHelper.IDP_SESSION_COOKIE))
					return cookies;
			}
			
			List<Cookie> filteredCookies = new ArrayList<Cookie>();
			
			for (Cookie cookie: cookies) {
				if (!cookie.getName().equalsIgnoreCase(HttpServletHelper.LOGIN_CTX_KEY_NAME)) {
					filteredCookies.add(cookie);
				}
			}
			
			return (Cookie[]) filteredCookies.toArray();		
		}
		
	}
}
