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
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.SWITCH.aai.uApprove.components.Attribute;
import ch.SWITCH.aai.uApprove.components.ConfigurationManager;
import ch.SWITCH.aai.uApprove.components.UApproveException;
import ch.SWITCH.aai.uApprove.components.TermsOfUseManager;
import ch.SWITCH.aai.uApprove.storage.LogInfo;
import ch.SWITCH.aai.uApprove.storage.UserLogInfo;
import edu.internet2.middleware.shibboleth.common.attribute.provider.SAML2AttributeAuthority;
import edu.internet2.middleware.shibboleth.common.relyingparty.provider.SAMLMDRelyingPartyConfigurationManager;
import edu.internet2.middleware.shibboleth.idp.authn.AuthenticationException;
import edu.internet2.middleware.shibboleth.idp.authn.LoginContext;
import edu.internet2.middleware.shibboleth.idp.authn.PassiveAuthenticationException;
import edu.internet2.middleware.shibboleth.idp.session.Session;
import edu.vt.middleware.crypt.CryptException;
import edu.vt.middleware.crypt.symmetric.AES;

/**
 * Class Plugin:
 * 
 * Copyright (c) 2005-2006 SWITCH - The Swiss Education & Research Network
 */
public class Plugin implements Filter {

  private static final String INITPAR_CONFIG = "Config";
  
  private static final String CONTIDP_REASON_MONITORING_ONLY = "Monitoring only mode";
  private static final String CONTIDP_REASON_BLACKLIST = "Provider is in blacklist";
  private static final String CONTIDP_REASON_GLOBAL_APPROVAL = "User has given global attribute release consent";
  private static final String CONTIDP_REASON_PROVIDER_APPROVAL = "User has given attribute release consent to this provider";
  private static final String CONTIDP_REASON_NOATTRIBUTES = "No attributes will be released to this provider";

  private static final String CONTV_REASON_FIRSTVISIT = "User is unknown, seems to be the first visit";
  private static final String CONTV_REASON_RESETAPPROVAL = "User wants to reset the attribute release consent";
  private static final String CONTV_REASON_TERMSCHANGED = "The terms of use has changed";
  private static final String CONTV_REASON_NEWPROVIDER = "First access from the user to the provider";
  private static final String CONTV_REASON_ATTRCHANGED = "The set set of the attribute, which will be released to this provider has changed";

  private static Logger LOG = LoggerFactory.getLogger(Plugin.class);

  private String sharedSecret;
  private boolean useTerms = false;
  private SAML2AttributeAuthority saml2AA;
  private SAMLMDRelyingPartyConfigurationManager relyingPartyConfigurationManager;
  private String authnContextClassRef = null;

  // data encryption method
  private String encrypt(String value) throws UApproveException {
    AES aes = new AES();
    try {
      aes.setIV("SWITCHaai rules".getBytes());
      aes
          .setPrivateKey(new SecretKeySpec(sharedSecret.getBytes(), 0, 16,
              "AES"));
      ;
      return aes.encryptToBase64(value);
    } catch (CryptException e) {
      LOG.error("Encryptin failed", e);
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
          .toString(), "", "yes", null, null);
      storage.update(userInfo);
      LOG.info("continue2IdP, user was not existent, created one");
    }

    if (ConfigurationManager.makeBoolean(ConfigurationManager
        .getParam(ConfigurationManager.ARPFILTER_LOG_PROVIDER_ACCESS))) {
      if (approvalGiven) {
        storage.updateArpProviderAccess(username, providerId, globalARA);
        LOG
            .info("continue2IdP: provider access logged with attribute release approval");
      } else {
        storage.updateArpProviderAccessWithNoARA(username, providerId);
        LOG
            .info("continue2IdP: provider access logged without attribute release approval");
      }
    } else {
      LOG.info("continue2IdP: provider access not logged");
    }
    return;
  }

  // method to continue to the arpviewer web application (leave IdP)
  private String post2ArpViewer(String reason, boolean editSettings,
      String returnURL, String username, String providerId,
      Collection<Attribute> attributesReleased, HttpServletRequest request, LoginContext loginCtx) throws
      UApproveException, PassiveAuthenticationException {
    LOG.info("continue2ArpViewer, reason: " + reason);
    LOG.debug("continue2ArpViewer: returnURL=" + returnURL);
    LOG.debug("Saving LoginContext to HTTP Session for further use");
    request.getSession().setAttribute(LoginContext.LOGIN_CONTEXT_KEY, loginCtx);
    // action URL format:
    // https://host.domain.com/arpviewer/Controller/?returnUrl=https://idp.domain.com/Authn/RemoteUser&editsettings=true;
    
    boolean isPassive = loginCtx.isPassiveAuthRequired();
    boolean isPassiveSupported = ConfigurationManager.makeBoolean(ConfigurationManager.getParam(ConfigurationManager.ARPFILTER_ISPASSIVE_SUPPORT));
    LOG.debug("isPassive={}, isPassiveSupport={}", isPassive, isPassiveSupported);
    
    if (isPassive && isPassiveSupported) {
      throw new PassiveAuthenticationException("Passive authentication is required, Arpviewer has to support it, but can not, because user interaction is required");
    }
    
    // action URL format:
    // https://host.domain.com/arpviewer/Controller/?returnUrl=https://idp.domain.com/Authn/RemoteUser&editsettings=true;
    String action = ConfigurationManager
        .getParam(ConfigurationManager.ARPFILTER_ARPVIEWER_DEPLOYPATH);
    action += "?" + ConfigurationManager.HTTP_PARAM_RETURNURL + "=";
    try {
      action += URLEncoder.encode(returnURL, "utf-8");
    } catch (UnsupportedEncodingException e) {
      LOG.error("Encoding returnURL",e);
      throw new UApproveException(e);
    }
    action += editSettings ? "&" + ConfigurationManager.HTTP_PARAM_EDIT + "=true"
        : "";
    LOG.debug("continue2ArpViewer: action=" + action);

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
        + ConfigurationManager.HTTP_PARAM_USERNAME
        + "\" value=\""
        + encrypt(username)
        + "\"/>"
        + "       <input type=\"hidden\" name=\""
        + ConfigurationManager.HTTP_PARAM_PROVIDERID
        + "\" value=\""
        + encrypt(providerId)
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
    LOG.trace("continue2ArpViewer: mode=arpViewer postForm={}", postForm);
    return postForm;
  }

  public void init(FilterConfig filterConfig) {
    try {
      // initialize arp configuration
      ConfigurationManager
          .initialize(filterConfig.getInitParameter(INITPAR_CONFIG));
      
      // initialize arp terms
      String terms = ConfigurationManager.getParam(ConfigurationManager.COMMON_ARP_TERMS);
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

      // initialize arp blacklist
      SPBlacklistManager.initialize(ConfigurationManager
          .getParam(ConfigurationManager.ARPFILTER_ARP_BLACKLIST));

      // get the attribute authority and RelyingPartyConfigurationManager from the shibboleth idp context
      
      // extra flexibility, addicted to Chad
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
        LOG.info("ArpFilter is only working on this specific authnContextClassRef {}", authnContextClassRef);
      }
      
    } catch (UApproveException e) {
      LOG.error("Unable to initialize ArpFilter", e);
      return;
    }
  }

  public void destroy() {
    LOG.info("destroyed");
  }

  public void doFilter(ServletRequest servletRequest,
      ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
    try {
      
      LOG.debug("ArpFilter entry on path {}",((HttpServletRequest) servletRequest).getServletPath());
      
      // only work on HTTPRequests / responses
      if (!(servletRequest instanceof HttpServletRequest)
          && !(servletResponse instanceof HttpServletResponse)) {
        filterChain.doFilter(servletRequest, servletResponse);
        LOG.error("ArpFilter only work on HTTPRequests / responses");
        return;
      }
      
      HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
      // search logincontext in httpsession
      LoginContext loginCtx = (LoginContext) httpServletRequest
          .getSession().getAttribute(LoginContext.LOGIN_CONTEXT_KEY);
      if (loginCtx != null) {
        //loginCtx put here by arpfilter, remove it now
        LOG.debug("Returning from ArpViewer? Transferring LoginContext back to Request Scope");
        httpServletRequest.setAttribute(LoginContext.LOGIN_CONTEXT_KEY, loginCtx);
        httpServletRequest.getSession().removeAttribute(LoginContext.LOGIN_CONTEXT_KEY);
      } else {
        // search logincontext in request scope
        loginCtx = (LoginContext) httpServletRequest.getAttribute(LoginContext.LOGIN_CONTEXT_KEY);
      }
      
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
        LOG.debug("Skip ArpFilter on authN request with authentication method {}", loginCtx.getAuthenticationMethod());
        filterChain.doFilter(servletRequest, servletResponse);
        return;
      }
      
      // initialize storage handler (stupid, but true)
      String storeType = ConfigurationManager
          .getParam(ConfigurationManager.COMMON_STORE_TYPE);
      LogInfo.initialize(storeType);
      LogInfo storage = LogInfo.getInstance();
      LOG.debug("LogInfo (storage) initialized with mode=" + storeType);

      // set the content type of the response to html, needed by POST
      ((HttpServletResponse) servletResponse).setContentType("text/html");

      // get the username
      String username = null;
      // try from IdP Session
      Session idpSession = (Session) ((HttpServletRequest) servletRequest).getAttribute(Session.HTTP_SESSION_BINDING_ATTRIBUTE);
      LOG.trace("IdP Session is {}", idpSession);
      if (idpSession != null) {
        LOG.trace("IdP Session principal name is {}", idpSession.getPrincipalName());
        LOG.trace("IdP Session subject is {}", idpSession.getSubject());
        username = idpSession.getPrincipalName();
      }

      if (username==null || username.equals(""))
         throw new UApproveException("No username found, assure authentication");
      
      LOG.debug("doFilter: username=" + username);
      
      // get the providerId from shib context
      String providerId = loginCtx.getRelyingPartyId();
      LOG.debug("doFilter: providerId={}", providerId);
      
      LOG.info("ArpFilter touched for {} ==> {}", username, providerId);

      // get the attributes released for user and provider id;
      Collection<Attribute> attributesReleased = AttributeDumper.getAttributes(
          username, providerId);

      // get referer
      String referer = ((HttpServletRequest) servletRequest).getRequestURL()
          .toString();
      LOG.debug("doFilter: referer=" + referer);

      // getUserInfo
      UserLogInfo userInfo = storage.getData(username);
      LOG.debug("userInfo=" + userInfo);

      // set global ARA flag
      boolean globalARA = (userInfo != null && ConfigurationManager
          .makeBoolean(userInfo.getGlobal()));
      LOG.debug("globalARA=" + globalARA);

      // set edit settings flag
      boolean editSettings = (((HttpServletRequest) servletRequest)
          .getParameter(ConfigurationManager.HTTP_PARAM_EDIT) != null);
      LOG.debug("editSettings=" + editSettings);

      // We have all the information, lets run the business logic

      // check monitoring mode only
      if (ConfigurationManager.makeBoolean(ConfigurationManager
          .getParam(ConfigurationManager.ARPFILTER_MONITORING_ONLY))) {
        continue2IdP(CONTIDP_REASON_MONITORING_ONLY, storage, userInfo,
            username, providerId, globalARA, false);
        filterChain.doFilter(servletRequest, servletResponse);
        return;
      }

      // providerId in black list
      if (SPBlacklistManager.containsItem(providerId)) {
        continue2IdP(CONTIDP_REASON_BLACKLIST, storage, userInfo, username,
            providerId, globalARA, false);
        filterChain.doFilter(servletRequest, servletResponse);
        return;
      }

      if (userInfo == null) {
        ((HttpServletResponse) servletResponse).getWriter().write(
            post2ArpViewer(CONTAV_REASON_FIRSTVISIT, editSettings, referer,
                username, providerId, attributesReleased, httpServletRequest, loginCtx));
        return;
      }
      // check if the user wants to reset the settings
      if (editSettings) {
        ((HttpServletResponse) servletResponse).getWriter().write(
            post2ArpViewer(CONTAV_REASON_EDITAPPROVAL, editSettings, referer,
                username, providerId, attributesReleased, httpServletRequest, loginCtx));
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
          ((HttpServletResponse) servletResponse).getWriter().write(
              post2ArpViewer(CONTAV_REASON_TERMSCHANGED, editSettings, referer,
                  username, providerId, attributesReleased, httpServletRequest, loginCtx));
          return;
        }
      }

      // check if user has given global approval
      LOG.debug("doFilter: user has given global approval="
          + userInfo.getGlobal());
      if (ConfigurationManager.makeBoolean(userInfo.getGlobal())) {
        continue2IdP(CONTIDP_REASON_GLOBAL_APPROVAL, storage, userInfo,
            username, providerId, globalARA, true);
        filterChain.doFilter(servletRequest, servletResponse);
        return;
      }

      // get attributes which will be released
      String attributes = Attribute
          .serializeAttributeIDs(attributesReleased);
      LOG.debug("doFilter: attributes to be released=" + attributes);

      // check if there are any attributes
      if (attributes == null || attributes.equals("")) {
        continue2IdP(CONTIDP_REASON_NOATTRIBUTES, storage, userInfo, username,
            providerId, globalARA, false);
        filterChain.doFilter(servletRequest, servletResponse);
        return;
      }

      // check if it is users first visited to this provider
      LOG.debug("doFilter: providerId=" + providerId);
      LOG.debug("doFilter: user contains providerId="
          + userInfo.containsProviderId(providerId));
      if (providerId != null && !userInfo.containsProviderId(providerId)) {
        ((HttpServletResponse) servletResponse).getWriter().write(
            post2ArpViewer(CONTAV_REASON_NEWPROVIDER, editSettings, referer,
                username, providerId, attributesReleased, httpServletRequest, loginCtx));
        return;
      }

      // check if the there are attributes, which not are already approved for
      // release
      LOG.debug("doFilter: attributes already approved="
          + userInfo.getAttributesForProviderId(providerId));
      LOG.debug("doFilter: attributes to be released  =" + attributes);
      if (!Attribute.checkCompleteArp(userInfo
          .getAttributesForProviderId(providerId), attributes)) {
        ((HttpServletResponse) servletResponse).getWriter().write(
            post2ArpViewer(CONTAV_REASON_ATTRCHANGED, editSettings, referer,
                username, providerId, attributesReleased, httpServletRequest, loginCtx));
        return;
      }

      // the user has already given attribute release approval to this provider
      continue2IdP(CONTIDP_REASON_PROVIDER_APPROVAL, storage, userInfo,
          username, providerId, globalARA, true);
      filterChain.doFilter(servletRequest, servletResponse);
      return;

    } catch (UApproveException e) {
        doError((HttpServletResponse)servletResponse, e);
        return;
    } catch (AuthenticationException e) {
      LOG.error("AuthenticationException", e);
      LoginContext loginContext = (LoginContext) ((HttpServletRequest) servletRequest).getSession().getAttribute(LoginContext.LOGIN_CONTEXT_KEY);
      loginContext.setAuthenticationFailure(e);
      loginContext.setPrincipalAuthenticated(false);
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

  }
  
  private void doError(HttpServletResponse response, UApproveException e) throws IOException {
    LOG.error("Arpfilter error",e);
    
    response.setContentType("text/html");
    String htmlErrorMessage = "<span style=\"font:bold 16px monospace;color:red;\">ARPFILTER ERROR</span><br><br><br>";
    htmlErrorMessage += "<span style=\"font:bold 12px monospace;color:black;\">Message:</span><br><br>";
    htmlErrorMessage += "<tt>" + e.getMessage() +"</tt><br><br><br><br>";
    htmlErrorMessage += "<tt>----------------------------------------------</tt><br>";
    htmlErrorMessage += "<tt>Please try again or contact your administrator</tt>";
    response.getWriter().write(htmlErrorMessage);
    
  }
  
}