package ch.SWITCH.aai.uApprove.viewer;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.SWITCH.aai.uApprove.components.Attribute;
import ch.SWITCH.aai.uApprove.components.ConfigurationManager;
import ch.SWITCH.aai.uApprove.components.Crypt;
import ch.SWITCH.aai.uApprove.components.RelyingParty;
import ch.SWITCH.aai.uApprove.components.TermsOfUseManager;
import ch.SWITCH.aai.uApprove.components.UApproveException;
import ch.SWITCH.aai.uApprove.storage.LogInfo;
import ch.SWITCH.aai.uApprove.storage.UserLogInfo;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * uApprove Controller Servlet
 * 
 * Copyright (c) 2005-2006 SWITCH - The Swiss Education & Research Network
 * 
 * Purpose: Steering class which decides what button the user pressed and then
 * updates/stores the relevant information.
 * 
 * @author: C.Witzig, based on the original code of F.Poroli 8.12.2005
 */
public class Controller extends HttpServlet {

  private static final long serialVersionUID = 1L;

  public static final String INITPAR_CONFIG = "Config";

  private static final String PAGE_TERMS = "/terms-of-use.jsp";
  private static final String PAGE_TERMS_DECLINED = "/terms-of-use-declined.jsp";
  private static final String PAGE_ATTRIBUTES = "/attributes.jsp";
  private static final String PAGE_ATTRIBUTES_DECLINED = "/attributes-declined.jsp";
  private static final String PAGE_RESET = "/reset-approvals.jsp";
  private static final String PAGE_ERROR = "/error.jsp";

  public static final String GETPAR_TERMS_CONFIRM = "terms-confirm";
  public static final String GETPAR_TERMS_AGREE = "terms-agree";
  public static final String GETPAR_TERMS_DECLINE = "terms-decline";
  public static final String GETPAR_TERMS_DECLINE_BACK = "terms-decline-back";
  public static final String GETPAR_ATTRIBUTES_CONFIRM = "attributes-confirm";
  public static final String GETPAR_ATTRIBUTES_GLOBAL_CONSENT = "attributes-global-consent";
  public static final String GETPAR_ATTRIBUTES_DECLINE = "attributes-decline";
  public static final String GETPAR_ATTRIBUTES_DECLINE_BACK = "attributes-decline-back";

  public static final String GETPAR_STANDALONE = "standalone-next-url";
  public static final String GETPAR_RESET_CANCEL = "reset-cancel";
  public static final String GETPAR_RESET_CONFIRM = "reset-confirm";

  public static final String RB_TERMS = "terms-of-use";
  public static final String RB_TERMS_DECLINED = "terms-of-use-declined";
  public static final String RB_ATTRIBUTES = "attributes";
  public static final String RB_ATTRIBUTES_DECLINED = "attributes-declined";
  public static final String RB_RESET = "reset-approvals";

  public static final String SESKEY_PRINCIPAL = "principal";
  public static final String SESKEY_RETURNURL = "returnURL";
  public static final String SESKEY_RELYINGPARTY = "relyingParty";
  public static final String SESKEY_ATTRIBUTES = "attributes";
  public static final String SESKEY_GLOBAL_CONSENT_POSSIBLE = "globalConsentPossible";
  public static final String SESKEY_LOCALE = "locale";
  public static final String SESKEY_ERROR = "error";

  private static Logger LOG = LoggerFactory.getLogger(Controller.class);

  private boolean useTerms = false;
  private Crypt crypt;

  private boolean isGetParSet(HttpServletRequest request, String par) {
    if (request.getParameter(par) == null
        || request.getParameter(par).equals(""))
      return false;
    return true;
  }

  public void init() throws ServletException {
    try {
      // config init
      ConfigurationManager.initialize(getServletContext().getInitParameter(
          INITPAR_CONFIG));

      // initializing logback
      LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      try {
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        configurator.doConfigure(ConfigurationManager
            .getParam(ConfigurationManager.VIEWER_LOGBACK_CONFIG));
      } catch (JoranException je) {
        throw new UApproveException(je);
      }

      // initialize terms
      String terms = ConfigurationManager.getParam(ConfigurationManager.COMMON_TERMS);
      if (terms != null && !terms.equals("")) {
        try {
          TermsOfUseManager.initalize(terms);
        } catch (Exception e) {
          throw new UApproveException(e);
        }
        useTerms = true;
        LOG.debug("TermsOfUseManager loaded, version=" + TermsOfUseManager.getVersion());
      } else {
        LOG.debug("No TermsOfUseManager gonna be used");
      }

      // init attributeList
      AttributeList.initialize(ConfigurationManager.getParam(ConfigurationManager.VIEWER_ATTRIBUTELIST));
      LOG.debug("Content of attribute list:");
      for (String key : AttributeList.getWhiteList()) {
        LOG.debug("{}", key);
      }
      // storage init
      String storeType = ConfigurationManager
          .getParam(ConfigurationManager.COMMON_STORE_TYPE);
      LogInfo.initialize(storeType);
      LOG.debug("LogInfo (storage) initialized with mode=" + storeType);

    } catch (UApproveException e) {
      LOG.error("uApprove servlet initialization failed", e);
    }

    // initialize secret for decryption
    String sharedSecret = ConfigurationManager.getParam(ConfigurationManager.COMMON_SHARED_SECRET);
    crypt = new Crypt(sharedSecret);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
    
      // initialization
      HttpSession session = request.getSession();
      LogInfo storage = LogInfo.getInstance();
      
      // debugging
      RelyingParty rpdebug = (RelyingParty)session.getAttribute(SESKEY_RELYINGPARTY);
      LOG.trace("RelyingParty before entering reading request, entityId={}",
    		  rpdebug==null?"Not Set":rpdebug.getEntityId());
      
      
      
      /*
      Enumeration<String> e = session.getAttributeNames();
      while (e.hasMoreElements()) {
    	  String key = e.nextElement();
    	  Object value = session.getAttribute(key);
    	  LOG.trace("{} => {}", key, value);
      }
      */
      
      // get principal
      String principal = crypt.decrypt(request
          .getParameter(ConfigurationManager.HTTP_PARAM_PRINCIPAL));
      LOG.debug("principal=" + principal);
      session.setAttribute(SESKEY_PRINCIPAL, principal);

      // get returnURL
      String returnURL = request
          .getParameter(ConfigurationManager.HTTP_PARAM_RETURNURL);
      LOG.debug("returnURL=" + returnURL);
      session.setAttribute(SESKEY_RETURNURL, returnURL);

      // get relying
      LOG.debug("RP decrypted, serialized: {}", crypt.decrypt(request
              .getParameter(ConfigurationManager.HTTP_PARAM_RELYINGPARTY)));

      RelyingParty relyingParty = new RelyingParty(crypt.decrypt(request
          .getParameter(ConfigurationManager.HTTP_PARAM_RELYINGPARTY)));

      LOG.debug("entityId=" + relyingParty.getEntityId());
      session.setAttribute(SESKEY_RELYINGPARTY, relyingParty);
      
      if (request.getParameter(ConfigurationManager.HTTP_PARAM_RESET) != null) {
        // start edit flow
        LOG.info("user want to edit the attribute release approval");
        getServletContext().getRequestDispatcher(PAGE_RESET).forward(request,
            response);
        return;
      } else {
        // Start flow
        // get released attributes
        String serializedAttributesReleased = crypt.decrypt(request
            .getParameter(ConfigurationManager.HTTP_PARAM_ATTRIBUTES));
        LOG.debug("serializedAttributesReleased="
            + serializedAttributesReleased);
        Collection<Attribute> attributesReleased = Attribute
            .unserializeAttributes(serializedAttributesReleased);
        session.setAttribute(SESKEY_ATTRIBUTES, attributesReleased);

        // get globalConsentPossible
        boolean globalConsentPossible = ConfigurationManager
            .makeBoolean(ConfigurationManager
                .getParam(ConfigurationManager.VIEWER_GLOBAL_CONSENT));
        LOG.debug("globalConsentPossible=" + globalConsentPossible);
        session.setAttribute(SESKEY_GLOBAL_CONSENT_POSSIBLE, globalConsentPossible);

        // get the locale
        Locale locale = createLocale(request.getLocale(), ConfigurationManager
            .getParam(ConfigurationManager.VIEWER_USELOCALE));
        LOG.debug("locale=" + locale);
        session.setAttribute(SESKEY_LOCALE, locale);

        // getUserInfo
        UserLogInfo userInfo = storage.getData(principal);
        LOG.debug("userInfo=" + userInfo);
        
        
        if (userInfo == null) {
          if (useTerms) {
            LOG.debug("first visit of the user, redirect to terms page");
            getServletContext().getRequestDispatcher(PAGE_TERMS).forward(request,response);
          } else {
            LOG.debug("first visit of the user, redirect to attributes page");
            getServletContext().getRequestDispatcher(PAGE_ATTRIBUTES).forward(request,response);
          }
          return;
        }

        // has user agreed to the current terms version
        if (useTerms && !userInfo.getTermsVersion().equals(TermsOfUseManager.getVersion())) {
          LOG.info("current terms version are not agreed by user, redirect to the terms page");
          getServletContext().getRequestDispatcher(PAGE_TERMS).forward(request,response);
          return;
        } 
        
        LOG.info("Terms are not used or current terms version are agreed by user, redirect to the attributes page");
        getServletContext().getRequestDispatcher(PAGE_ATTRIBUTES).forward(request,response);
        return;
      }
    } catch (UApproveException e) {
      doError(request, response, e);
    }
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      // initialization
      HttpSession session = request.getSession();
      ConfigurationManager.initialize(getServletContext().getInitParameter(
          INITPAR_CONFIG));

      // initialize storage handler
      String storeType = ConfigurationManager
          .getParam(ConfigurationManager.COMMON_STORE_TYPE);
      LogInfo.initialize(storeType);
      LogInfo storage = LogInfo.getInstance();
      LOG.debug("LogInfo (storage) initialized with mode=" + storeType);

      String principal = (String) session.getAttribute(SESKEY_PRINCIPAL);
      LOG.debug("principal=" + principal);
      if (principal == null || principal.equals(""))
        throw new UApproveException("Principal is not set");

      RelyingParty relyingParty = (RelyingParty) session.getAttribute(SESKEY_RELYINGPARTY);
      LOG.debug("entityId=" + relyingParty.getEntityId());

      UserLogInfo userInfo = storage.getData(principal);
      LOG.debug("userInfo=" + userInfo);

      Collection<Attribute> attributesReleased = (Collection<Attribute>) session.getAttribute(SESKEY_ATTRIBUTES);

      String returnURL = (String) session.getAttribute(SESKEY_RETURNURL);
      LOG.debug("returnURL=" + returnURL);

      if (isGetParSet(request, GETPAR_TERMS_CONFIRM)) {
        LOG.debug("coming from terms confirmed");
        // check if the user agreed the terms
        if (isGetParSet(request, GETPAR_TERMS_AGREE)) {
          LOG.info("user agreed terms, store, redirect to attributes");
          storeUserBasic(userInfo, principal, TermsOfUseManager.getVersion());
          getServletContext().getRequestDispatcher(PAGE_ATTRIBUTES).forward(request,
              response);
          return;
        } else {
          LOG.info("user dont agreed terms, redirect again to terms");
          getServletContext().getRequestDispatcher(PAGE_TERMS).forward(request,
              response);
          return;
        }
      }

      if (isGetParSet(request, GETPAR_TERMS_DECLINE)) {
        LOG.debug("coming from terms declined, redirect to decline page");
        getServletContext().getRequestDispatcher(PAGE_TERMS_DECLINED).forward(
            request, response);
        return;
      }

      if (isGetParSet(request, GETPAR_TERMS_DECLINE_BACK)) {
        LOG.debug("coming from terms declined back, redirect to terms page");
        getServletContext().getRequestDispatcher(PAGE_TERMS).forward(request,
            response);
        return;
      }

      if (isGetParSet(request, GETPAR_ATTRIBUTES_CONFIRM)) {
        LOG.info("user gave attribute release consent, store, redirect to returnURL="
            + response.encodeRedirectURL(returnURL));
        
        String termsVersion = useTerms ? TermsOfUseManager.getVersion() : "";
        String attributes = Attribute.serializeAttributeIDs(attributesReleased);
        LOG.debug("store: principal=" + principal + " entityId=" + relyingParty.getEntityId()
            + " attributes=" + attributes + " terms=" + termsVersion + " globalConsent="
            + isGetParSet(request, GETPAR_ATTRIBUTES_GLOBAL_CONSENT));
        storeUser(userInfo, principal, termsVersion, relyingParty.getEntityId(), attributes,
            isGetParSet(request, GETPAR_ATTRIBUTES_GLOBAL_CONSENT));
        response.sendRedirect(response.encodeRedirectURL(returnURL));
        return;
      }

      if (isGetParSet(request, GETPAR_ATTRIBUTES_DECLINE)) {
        LOG.debug("coming from attributes declined, redirect to decline page");
        getServletContext().getRequestDispatcher(PAGE_ATTRIBUTES_DECLINED).forward(
            request, response);
        return;
      }

      if (isGetParSet(request, GETPAR_ATTRIBUTES_DECLINE_BACK)) {
        LOG.info("coming from attributes declined back, redirect to attributes page");
        getServletContext().getRequestDispatcher(PAGE_ATTRIBUTES).forward(request,
            response);
        return;
      }

      // coming here is not ok
      throw new UApproveException("uApprove viewer flow error");
    } catch (UApproveException e) {
      doError(request, response, e);
    }
  }

  private void storeUser(UserLogInfo userInfo, String principal,
      String termsVersion, String entityId, String attributesReleased,
      boolean globalConsent) throws UApproveException {
    
    LOG.debug("storeUser");
    LogInfo storage = LogInfo.getInstance();
    if (userInfo == null) {
      LOG.debug("create new user");
      
      userInfo = storage.addUserLogInfoData(principal, "1.0", new Date()
          .toString(), termsVersion, "no", entityId, attributesReleased);
    } else {
       userInfo.setOndate(new Date().toString());
       userInfo.setTermsVersion(termsVersion);
       userInfo.setGlobal(globalConsent ? "yes" : "no");
       userInfo.addProviderId(entityId, attributesReleased);
    }
    storage.update(userInfo, entityId);
  }

  private void storeUserBasic(UserLogInfo userInfo, String principal,
      String termsVersion) throws UApproveException {
    LOG.debug("storeUserBasic");
    LogInfo storage = LogInfo.getInstance();
    
    if (userInfo == null) {
      LOG.debug("create new user");
      userInfo = storage.addUserLogInfoData(principal, "1.0", new Date()
          .toString(), termsVersion, "no", null, null);
    } else {
      userInfo.setTermsVersion(termsVersion);
      userInfo.setOndate(new Date().toString());
    }
    storage.update(userInfo);
  }
 

  public static Locale createLocale(Locale requestLocale, String enforced) {

    if (enforced != null && !enforced.equals(""))
      return new Locale(enforced.substring(0, enforced.indexOf("_")), enforced
          .substring(enforced.indexOf("_") + 1));

    if (requestLocale != null)
      return requestLocale;

    return Locale.ENGLISH;
  }

  // parse resource (sp) host
  private static String getResourceHost(String entityId) {
    int i1 = entityId.indexOf("//");
    int i2 = entityId.indexOf("/", i1+2);
    LOG.debug("entityId received = \"" + entityId + "\"");

    // return just the sp.example.org component out of https://sp.example.org/shibboleth
    if ( i2 >= 0 )
       entityId = entityId.substring(i1 + 2, i2);
    else if ( i1 >= 0 )
       entityId = entityId.substring(i1 + 2);

    // return just the sp.example.org component out of urn:mace:federation.org:sp.example.org
    if (entityId.indexOf(':')>=0) {
    	entityId = entityId.substring(entityId.lastIndexOf(':')+1);
    }
    LOG.debug("hostname extracted = \"" + entityId + "\"");

    return entityId;
  }
  
  public static String getRelyingPartyName(RelyingParty relyingParty, Locale locale) {
	  
	  Map <String, String> names =  relyingParty.getRpNames();
	  String name = names.get(locale.getLanguage());
	  if (name != null)
		  return name;
	  name = names.get(Locale.ENGLISH.getLanguage());
	  if (name != null)
		  return name;
	  
	  return getResourceHost(relyingParty.getEntityId());
  }
  
  public static String getRelyingPartyDesc(RelyingParty relyingParty, Locale locale) {
	  Map <String, String> descs =  relyingParty.getRpDescriptions();
	  String desc = descs.get(locale.getLanguage());
	  if (desc != null)
		  return desc;
	  desc = descs.get(Locale.ENGLISH.getLanguage());
	  if (desc != null)
		  return desc;
	  
	  return "";
  }
  
  // resolves displayName due to locale
  public static String resolveDisplayName(Attribute attribute, Locale locale) {
    String name = attribute.attributeNames.get(locale.getLanguage());
    if (name == null)
      name = attribute.attributeNames.get(Locale.ENGLISH.getLanguage());
    if (name == null)
       name = attribute.attributeID;
    return name;
  }
  
  // resolves displayDescription due to locale
  public static String resolveDisplayDesc(Attribute attribute, Locale locale) {
    String desc = attribute.attributeDescriptions.get(locale.getLanguage());
    if (desc == null)
      desc = attribute.attributeDescriptions.get(Locale.ENGLISH.getLanguage());
    if (desc == null)
      desc = "";
    return desc;
  }
  

  public static void doError(HttpServletRequest request, HttpServletResponse response, UApproveException e) throws ServletException, IOException {
    HttpSession session = request.getSession();
    session.setAttribute(SESKEY_ERROR, e);
    LOG.error("uApprove Error", e);
    session.getServletContext().getRequestDispatcher(PAGE_ERROR).forward(request, response);
  }

}
