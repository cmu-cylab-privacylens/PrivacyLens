package ch.SWITCH.aai.uApprove.idpplugin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.SWITCH.aai.uApprove.components.Attribute;
import ch.SWITCH.aai.uApprove.components.ConfigurationManager;
import ch.SWITCH.aai.uApprove.components.TermsOfUseManager;
import ch.SWITCH.aai.uApprove.components.UApproveException;
import ch.SWITCH.aai.uApprove.storage.LogInfo;
import ch.SWITCH.aai.uApprove.storage.UserLogInfo;
import edu.internet2.middleware.shibboleth.common.attribute.provider.SAML2AttributeAuthority;
import edu.internet2.middleware.shibboleth.common.relyingparty.provider.SAMLMDRelyingPartyConfigurationManager;
import edu.internet2.middleware.shibboleth.idp.authn.AuthenticationException;
import edu.internet2.middleware.shibboleth.idp.authn.LoginContext;
import edu.internet2.middleware.shibboleth.idp.authn.PassiveAuthenticationException;
import edu.internet2.middleware.shibboleth.idp.session.Session;
import edu.internet2.middleware.shibboleth.idp.util.HttpServletHelper;
import edu.vt.middleware.crypt.CryptException;
import edu.vt.middleware.crypt.symmetric.AES;
import edu.vt.middleware.crypt.symmetric.SymmetricAlgorithm;
import edu.vt.middleware.crypt.util.Base64Converter;

/**
 * Class Plugin:
 *
 * Copyright (c) 2005-2006 SWITCH - The Swiss Education & Research Network
 */
public class Plugin implements Filter {

  private static final String INITPAR_CONFIG = "Config";

  private static final String CONTIDP_REASON_MONITORING_ONLY = "Monitoring only mode";
  private static final String CONTIDP_REASON_BLACKLIST = "Provider is in blacklist";
  private static final String CONTIDP_REASON_GLOBAL_CONSENT = "User has given global attribute release consent";
  private static final String CONTIDP_REASON_PROVIDER_APPROVAL = "User has given attribute release consent to this provider";
  private static final String CONTIDP_REASON_NOATTRIBUTES = "No attributes will be released to this provider";

  private static final String CONTV_REASON_FIRSTVISIT = "User is unknown, seems to be the first visit";
  private static final String CONTV_REASON_RESETCONSENT = "User wants to reset the attribute release consent";
  private static final String CONTV_REASON_TERMSCHANGED = "The terms of use has changed";
  private static final String CONTV_REASON_NEWPROVIDER = "First access from the user to the provider";
  private static final String CONTV_REASON_ATTRCHANGED = "The set set of the attribute, which will be released to this provider has changed";

  private static Logger LOG = LoggerFactory.getLogger(Plugin.class);

  private String sharedSecret;
  private boolean useTerms = false;
  private SAML2AttributeAuthority saml2AA;
  private SAMLMDRelyingPartyConfigurationManager relyingPartyConfigurationManager;
  private String authnContextClassRef = null;
  private ServletContext servletContext = null;
  
  // data encryption method
  private String encrypt(String value) throws UApproveException {
    final SymmetricAlgorithm alg = new AES();
    alg.setIV("uApprove initial vector".substring(0, 16).getBytes());
	alg.setKey(new SecretKeySpec(sharedSecret.getBytes(), 0, 16, AES.ALGORITHM));
	try {
      alg.initEncrypt();
      return alg.encrypt(value.getBytes(), new Base64Converter());
    } catch (CryptException e) {
      LOG.error("Encryption failed", e);
      throw new UApproveException(e);
    }
  }

  // method to continue to the identity provider (skip filter)
  private void continue2IdP(String reason, LogInfo storage,
    UserLogInfo userInfo, String username, String providerId,
    boolean globalARA, boolean approvalGiven) throws UApproveException {
    LOG.info("continue2Idp, reason: " + reason);

    // create user hack
    if (userInfo == null) {
      userInfo = storage.addUserLogInfoData(username, "1.0", new Date()
          .toString(), "", "no", providerId, null);
      storage.update(userInfo);
      LOG.info("continue2IdP, user was not existent, created one");
    }

    if (ConfigurationManager.makeBoolean(ConfigurationManager
        .getParam(ConfigurationManager.PLUGIN_LOG_PROVIDER_ACCESS))) {
      if (approvalGiven) {
        storage.updateProviderAccess(username, providerId, globalARA);
        LOG
            .info("continue2IdP: provider access logged with attribute release approval");
      } else {
        storage.updateProviderAccessWithNoARA(username, providerId);
        LOG
            .info("continue2IdP: provider access logged without attribute release approval");
      }
    } else {
      LOG.info("continue2IdP: provider access not logged");
    }
    
    return;
  }

  // method to continue to the uApprove web application (leave IdP)
  private String post2Viewer(String reason, boolean resetConsent,
      String returnURL, String principal, String entityId,
      Collection<Attribute> attributesReleased, HttpServletRequest request, LoginContext loginCtx) throws
      UApproveException, PassiveAuthenticationException {
    LOG.info("Continue to uApprove viewer, reason: " + reason);
    LOG.debug(" returnURL=" + returnURL);
 
    boolean isPassive = loginCtx.isPassiveAuthRequired();
    boolean isPassiveSupported = ConfigurationManager.makeBoolean(ConfigurationManager.getParam(ConfigurationManager.PLUGIN_ISPASSIVE_SUPPORT));
    LOG.debug("isPassive={}, isPassiveSupport={}", isPassive, isPassiveSupported);

    if (isPassive && isPassiveSupported) {
      throw new PassiveAuthenticationException("Passive authentication is required, uApprove does support it, but can not, because user interaction is required");
    }

    // action URL format:
    // https://host.domain.com/uApprove/Controller/?returnUrl=https://idp.domain.com/Authn/RemoteUser&editsettings=true;
    String action = ConfigurationManager
        .getParam(ConfigurationManager.PLUGIN_VIEWER_DEPLOYPATH);
    action += "?" + ConfigurationManager.HTTP_PARAM_RETURNURL + "=";
    try {
      action += URLEncoder.encode(returnURL, "utf-8");
    } catch (UnsupportedEncodingException e) {
      LOG.error("Encoding returnURL",e);
      throw new UApproveException(e);
    }
    action += resetConsent ? "&" + ConfigurationManager.HTTP_PARAM_RESET + "=true"
        : "";
    LOG.debug("  action=" + action);

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
        + encrypt(principal)
        + "\"/>"
        + "       <input type=\"hidden\" name=\""
        + ConfigurationManager.HTTP_PARAM_PROVIDERID
        + "\" value=\""
        + encrypt(entityId)
        + "\"/>"
        + "       <input type=\"hidden\" name=\""
        + ConfigurationManager.HTTP_PARAM_ATTRIBUTES
        + "\" value=\""
        + encrypt(Attribute
            .serializeAttributes(attributesReleased))
        + "\"/>"
        + "       <noscript>"
        + "           <input type=\"submit\" value=\"Continue\"/>"
        + "       </noscript>" + "     </form>" + "   </body>" + " </html>";
    LOG.trace("continue2Viewer: mode=Viewer postForm={}", postForm);
    
    return postForm;
  }
  
  private void transferLoginContext2IdP(LoginContext loginCtx, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
	  HttpServletHelper.unbindLoginContext(HttpServletHelper.getStorageService(servletContext), servletContext, httpServletRequest, httpServletResponse);
	  HttpServletHelper.bindLoginContext(loginCtx, httpServletRequest);
  }
  
  private void transferLoginContext2uApprove(LoginContext loginCtx, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
	  HttpServletHelper.bindLoginContext(loginCtx, HttpServletHelper.getStorageService(servletContext), servletContext, httpServletRequest, httpServletResponse);
  }
  
  private LoginContext retrieveLoginContext(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
	  return HttpServletHelper.getLoginContext(HttpServletHelper.getStorageService(servletContext), servletContext, httpServletRequest);
  }

  public void init(FilterConfig filterConfig) {
    try {
    
      // get servlet context
      servletContext = filterConfig.getServletContext();	  
      if (servletContext == null) {
        throw new UApproveException("No servlet context");
      }
		  
      // initialize configuration
      ConfigurationManager
          .initialize(filterConfig.getInitParameter(INITPAR_CONFIG));

      // initialize terms
      String terms = ConfigurationManager.getParam(ConfigurationManager.COMMON_TERMS);
      if (terms != null && !terms.equals("")) {
        try {
          TermsOfUseManager.initalize(terms);
        } catch (Exception e) {
          throw new UApproveException(e);
        }
        useTerms = true;
        LOG.info("TermsOfUseManager loaded, version=" + TermsOfUseManager.getVersion());
      } else {
        LOG.info("No TermsOfUseManager gonna be used");
      }

      // initialize sp blacklist
      SPBlacklistManager.initialize(ConfigurationManager
          .getParam(ConfigurationManager.PLUGIN_SP_BLACKLIST));

      // get the attribute authority and RelyingPartyConfigurationManager from the shibboleth idp context

      // extra flexibility, devoted to Chad
      String saml2AAId = filterConfig.getInitParameter("saml2AAId");
      if (saml2AAId == null || saml2AAId.equals("")) {
        saml2AAId = "shibboleth.SAML2AttributeAuthority";
      }

      String relyingPartyConfigurationManagerId = filterConfig.getInitParameter("relyingPartyConfigurationManagerId");
      if (relyingPartyConfigurationManagerId == null || relyingPartyConfigurationManagerId.equals("")) {
        relyingPartyConfigurationManagerId = "shibboleth.RelyingPartyConfigurationManager";
      }

      if ((saml2AA = (SAML2AttributeAuthority) filterConfig.getServletContext().getAttribute(saml2AAId)) == null)
        throw new UApproveException("Unable to load SAML2 AA from shibboleth idp context");

      if ((relyingPartyConfigurationManager = (SAMLMDRelyingPartyConfigurationManager) filterConfig.getServletContext().getAttribute(relyingPartyConfigurationManagerId)) == null)
        throw new UApproveException("Unable to load RelyingPartyConfigurationManager from shibboleth idp context");

      // initialize AttributeDumper
      AttributeDumper.initialize(saml2AA, relyingPartyConfigurationManager);

      // initialize secret for encryption
      sharedSecret = ConfigurationManager.getParam(ConfigurationManager.COMMON_SHARED_SECRET);

      // check if the filter only should work on a specified authnContextClassRef
      String accr = filterConfig.getInitParameter("authnContextClassRef");
      if (accr != null && !accr.equals("")) {
        authnContextClassRef = accr;
        LOG.info("IdP plugin is only working on this specific authnContextClassRef {}", authnContextClassRef);
      }

    } catch (UApproveException e) {
      LOG.error("Unable to initialize uApprove IdP plugin", e);
      return;
    }
  }

  public void destroy() {
    LOG.info("destroyed");
  }

  public void doFilter(ServletRequest servletRequest,
      ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
    try {

      LOG.trace("IdP plugin entry on path {}",((HttpServletRequest) servletRequest).getServletPath());

      // only work on HTTPRequests / responses
      if (!(servletRequest instanceof HttpServletRequest)
          && !(servletResponse instanceof HttpServletResponse)) {
        filterChain.doFilter(servletRequest, servletResponse);
        LOG.error("IdP plugin only work on HTTPRequests / responses");
        return;
      }

      HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
      HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

      LoginContext loginCtx = retrieveLoginContext(httpServletRequest, httpServletResponse);
      LOG.trace("LoginContext loaded: {}", loginCtx != null);
      
      if (loginCtx == null) {
        LOG.debug("LoginContext not found, this must be the first visit to profile servlet");
        filterChain.doFilter(servletRequest, servletResponse);
        return;
      }

      // check login context for errors
      AuthenticationException authenticationFailure = loginCtx.getAuthenticationFailure();
      if (authenticationFailure != null) {
        LOG.info("Authentication failure detected ({}) transfering back to IdP to handle it", authenticationFailure.getMessage());
        filterChain.doFilter(servletRequest, servletResponse);
        return;
      }

      // check if the filter only should work on a specified authnContextClassRef
      LOG.trace("LoginContext authenticationMethod{}",loginCtx.getAuthenticationMethod());
      if (authnContextClassRef != null && !authnContextClassRef.equals(loginCtx.getAuthenticationMethod())) {
        LOG.debug("Skip IdP plugin on authN request with authentication method {}", loginCtx.getAuthenticationMethod());
        filterChain.doFilter(servletRequest, servletResponse);
        return;
      }

      // set the content type of the response to html, needed by POST
      ((HttpServletResponse) servletResponse).setContentType("text/html");

      // get the principal
      String principal = null;
 
      // try from IdP Session
      Session idpSession = HttpServletHelper.getUserSession(httpServletRequest);

      LOG.trace("IdP Session is {}", idpSession);
      if (idpSession != null) {
        LOG.trace("IdP Session principal name is {}", idpSession.getPrincipalName());
        LOG.trace("IdP Session subject is {}", idpSession.getSubject());
        principal = idpSession.getPrincipalName();
    
      } if (loginCtx != null) {
    	  LOG.trace("LoginContext principal is {}", loginCtx.getPrincipalName());
    	  principal = loginCtx.getPrincipalName();
      }

      if (principal==null || principal.equals(""))
         throw new UApproveException("No principal found, assure authentication");

      LOG.debug("doFilter: principal=" + principal);

      // get the entityId from shib context
      String entityId = loginCtx.getRelyingPartyId();
      LOG.debug("doFilter: entityId={}", entityId);

      LOG.info("uApprove touched for {} ==> {}", principal, entityId);

      // get the attributes released for user and provider id;
      Collection<Attribute> attributesReleased = AttributeDumper.getAttributes(
          principal, entityId);

      // get referer
      String referer = httpServletRequest.getRequestURL()
          .toString();
      LOG.debug("doFilter: referer={}", referer);

      // initialize storage handler (stupid, but true)
      String storeType = ConfigurationManager
          .getParam(ConfigurationManager.COMMON_STORE_TYPE);
      LogInfo.initialize(storeType);
      LogInfo storage = LogInfo.getInstance();
      LOG.debug("LogInfo (storage) initialized with mode=" + storeType);
      
      // getUserInfo
      UserLogInfo userInfo = storage.getData(principal);
      LOG.debug("userInfo=" + userInfo);

      // set global ARA flag
      boolean globalConsentGiven = (userInfo != null && ConfigurationManager
          .makeBoolean(userInfo.getGlobal()));
      LOG.debug("globalConsentGiven=" + globalConsentGiven);

      // set edit settings flag
      boolean resetConsent = (httpServletRequest
          .getParameter(ConfigurationManager.HTTP_PARAM_RESET) != null);
      LOG.debug("resetConsent=" + resetConsent);

      // We have all the information, lets run the business logic

      // check monitoring mode only
      if (ConfigurationManager.makeBoolean(ConfigurationManager
          .getParam(ConfigurationManager.PLUGIN_MONITORING_ONLY))) {
        continue2IdP(CONTIDP_REASON_MONITORING_ONLY, storage, userInfo,
            principal, entityId, globalConsentGiven, false);
        
        transferLoginContext2IdP(loginCtx, httpServletRequest, httpServletResponse);
        filterChain.doFilter(servletRequest, servletResponse);
        return;
      }

      // providerId in black list
      if (SPBlacklistManager.containsItem(entityId)) {
        continue2IdP(CONTIDP_REASON_BLACKLIST, storage, userInfo, principal,
            entityId, globalConsentGiven, false);
        transferLoginContext2IdP(loginCtx, httpServletRequest, httpServletResponse);
        filterChain.doFilter(servletRequest, servletResponse);
        return;
      }

      if (userInfo == null) {
    	transferLoginContext2uApprove(loginCtx, httpServletRequest, httpServletResponse);
        httpServletResponse.getWriter().write(
            post2Viewer(CONTV_REASON_FIRSTVISIT, resetConsent, referer,
                principal, entityId, attributesReleased, httpServletRequest, loginCtx));
        return;
      }
      // check if the user wants to reset the settings
      if (resetConsent) {
    	transferLoginContext2uApprove(loginCtx, httpServletRequest, httpServletResponse);
        httpServletResponse.getWriter().write(
            post2Viewer(CONTV_REASON_RESETCONSENT, resetConsent, referer,
                principal, entityId, attributesReleased, httpServletRequest, loginCtx));
        return;
      }

      // check if the terms changed
      if (useTerms) {
        String termsVersion = TermsOfUseManager.getVersion();
        String userTermsVersion = userInfo.getTermsVersion();
        LOG.debug("doFilter: terms changed termsVersion=" + termsVersion);
        LOG.debug("doFilter: terms changed userTermsVersion=" + userTermsVersion);

        if (termsVersion != null
            && !userTermsVersion.equalsIgnoreCase(termsVersion)) {
          transferLoginContext2uApprove(loginCtx, httpServletRequest, httpServletResponse);
          httpServletResponse.getWriter().write(
              post2Viewer(CONTV_REASON_TERMSCHANGED, resetConsent, referer,
                  principal, entityId, attributesReleased, httpServletRequest, loginCtx));
          return;
        }
      }

      // check if user has given global approval
      LOG.debug("doFilter: user has given global approval="
          + userInfo.getGlobal());
      if (ConfigurationManager.makeBoolean(userInfo.getGlobal())) {
        continue2IdP(CONTIDP_REASON_GLOBAL_CONSENT, storage, userInfo,
            principal, entityId, globalConsentGiven, true);
        transferLoginContext2IdP(loginCtx, httpServletRequest, httpServletResponse);
        filterChain.doFilter(servletRequest, servletResponse);
        return;
      }

      // get attributes which will be released
      String attributes = Attribute
          .serializeAttributeIDs(attributesReleased);
      LOG.debug("doFilter: attributes to be released=" + attributes);

      // check if there are any attributes
      if (attributes == null || attributes.equals("")) {
        continue2IdP(CONTIDP_REASON_NOATTRIBUTES, storage, userInfo, principal,
            entityId, globalConsentGiven, false);
        transferLoginContext2IdP(loginCtx, httpServletRequest, httpServletResponse);
        filterChain.doFilter(servletRequest, servletResponse);
        return;
      }

      // check if it is users first visited to this provider
      LOG.debug("doFilter: entityId=" + entityId);
      LOG.debug("doFilter: user contains entityId="
          + userInfo.containsProviderId(entityId));
      if (entityId != null && !userInfo.containsProviderId(entityId)) {
    	  transferLoginContext2uApprove(loginCtx, httpServletRequest, httpServletResponse);
    	  httpServletResponse.getWriter().write(
            post2Viewer(CONTV_REASON_NEWPROVIDER, resetConsent, referer,
                principal, entityId, attributesReleased, httpServletRequest, loginCtx));
        return;
      }

      // check if the there are attributes, which not are already approved for
      // release
      LOG.debug("doFilter: attributes already approved="
          + userInfo.getAttributesForProviderId(entityId));
      LOG.debug("doFilter: attributes to be released  =" + attributes);
      if (!Attribute.compareAttributeRelease(userInfo
          .getAttributesForProviderId(entityId), attributes)) {
    	  transferLoginContext2uApprove(loginCtx, httpServletRequest, httpServletResponse);
    	  httpServletResponse.getWriter().write(
            post2Viewer(CONTV_REASON_ATTRCHANGED, resetConsent, referer,
                principal, entityId, attributesReleased, httpServletRequest, loginCtx));
        return;
      }

      // the user has already given attribute release approval to this provider
      continue2IdP(CONTIDP_REASON_PROVIDER_APPROVAL, storage, userInfo,
          principal, entityId, globalConsentGiven, true);
      transferLoginContext2IdP(loginCtx, httpServletRequest, httpServletResponse);
      filterChain.doFilter(servletRequest, servletResponse);
      return;

    } catch (UApproveException e) {
        doError((HttpServletResponse)servletResponse, e);
        return;
    } catch (AuthenticationException e) {
      LOG.error("AuthenticationException", e);
      LoginContext loginContext = retrieveLoginContext((HttpServletRequest)servletRequest, (HttpServletResponse)servletResponse);
      loginContext.setAuthenticationFailure(e);
      loginContext.setPrincipalAuthenticated(false);
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

  }

  private void doError(HttpServletResponse response, UApproveException e) throws IOException {
    LOG.error("uApprove error",e);

    response.setContentType("text/html");
    String htmlErrorMessage = "<span style=\"font:bold 16px monospace;color:red;\">uApprove error</span><br><br><br>";
    htmlErrorMessage += "<span style=\"font:bold 12px monospace;color:black;\">Message:</span><br><br>";
    htmlErrorMessage += "<tt>" + e.getMessage() +"</tt><br><br><br><br>";
    htmlErrorMessage += "<tt>----------------------------------------------</tt><br>";
    htmlErrorMessage += "<tt>Please try again or contact your administrator</tt>";
    response.getWriter().write(htmlErrorMessage);

  }

}