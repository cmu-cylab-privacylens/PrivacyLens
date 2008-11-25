package ch.SWITCH.aai.uApprove.viewer;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;


import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.SWITCH.aai.uApprove.components.ConfigurationManager;
import ch.SWITCH.aai.uApprove.components.UApproveException;
import ch.SWITCH.aai.uApprove.components.TermsOfUseManager;
import ch.SWITCH.aai.uApprove.components.Attribute;
import ch.SWITCH.aai.uApprove.storage.LogInfo;
import ch.SWITCH.aai.uApprove.storage.UserLogInfo;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import edu.vt.middleware.crypt.CryptException;
import edu.vt.middleware.crypt.symmetric.AES;

/**
 * Arpviewer Controller Servlet
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

  private static final String PAGE_TERMS = "/terms.jsp";
  private static final String PAGE_TERMSDECLINED = "/terms_declined.jsp";
  private static final String PAGE_ARP = "/arp.jsp";
  private static final String PAGE_ARPDECLINED = "/arp_declined.jsp";
  private static final String PAGE_EDIT = "/useredit.jsp";
  private static final String PAGE_ERROR = "/error.jsp";

  public static final String GETPAR_TERMS_CONFIRM = "terms_confirm";
  public static final String GETPAR_TERMS_AGREE = "iagreeterms";
  public static final String GETPAR_TERMS_DECLINE = "terms_decline";
  public static final String GETPAR_TERMS_DECLINE_BACK = "terms_declined_back";
  public static final String GETPAR_ARP_CONFIRM = "arp_confirm";
  public static final String GETPAR_ARP_AGREEGLOBAL = "iagreeglobal";
  public static final String GETPAR_ARP_DECLINE = "arp_decline";
  public static final String GETPAR_ARP_DECLINE_BACK = "arp_declined_back";

  public static final String GETPAR_STANDALONE = "standalone_next_url";
  public static final String GETPAR_EDITCANCEL = "CancelUseredit";
  public static final String GETPAR_EDITCONFIRM = "ConfirmUseredit";

  public static final String RB_TERMS = "resources_terms";
  public static final String RB_TERMS_DECLINED = "resources_terms_declined";
  public static final String RB_ARP = "resources_arp";
  public static final String RB_ARP_DECLINED = "resources_arp_declined";
  public static final String RB_USEREDIT = "resources_useredit";

  public static final String SESKEY_USERNAME = "username";
  public static final String SESKEY_RETURNURL = "returnURL";
  public static final String SESKEY_PROVIDERID = "providerId";
  public static final String SESKEY_ATTRIBUTES = "attributes";
  public static final String SESKEY_GLOBALARP_DISABLED = "globalarp";
  public static final String SESKEY_LOCALE = "locale";
  public static final String SESKEY_ERROR = "error";

  private static Logger LOG = LoggerFactory.getLogger(Controller.class);

  private String sharedSecret;
  private boolean useTerms = false;

  private boolean isGetParSet(HttpServletRequest request, String par) {
    if (request.getParameter(par) == null
        || request.getParameter(par).equals(""))
      return false;
    return true;
  }

  private String decrypt(String value) throws UApproveException {
    AES aes = new AES();
    try {
      aes.setIV("SWITCHaai rules".getBytes());
      aes
          .setPrivateKey(new SecretKeySpec(sharedSecret.getBytes(), 0, 16,
              "AES"));
      return new String(aes.decryptFromBase64(value));
    } catch (CryptException e) {
      LOG.error("Decryption failed", e);
      throw new UApproveException(e);
    }
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
        lc.shutdownAndReset();
        configurator.doConfigure(ConfigurationManager
            .getParam(ConfigurationManager.ARPVIEWER_LOGBACK_CONFIG));
      } catch (JoranException je) {
        throw new UApproveException(je);
      }

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

      // init attributeList
      AttributeList.initialize(ConfigurationManager.getParam(ConfigurationManager.ARPVIEWER_ATTRIBUTELIST));
      if (LOG.isDebugEnabled()) {
        LOG.debug("Content of attribute list:");
        for (String key : AttributeList.getWhiteList()) {
          LOG.debug("{}", key);
        }
      }
      // storage init
      String storeType = ConfigurationManager
          .getParam(ConfigurationManager.COMMON_STORE_TYPE);
      LogInfo.initialize(storeType);
      LOG.debug("LogInfo (storage) initialized with mode=" + storeType);

    } catch (UApproveException e) {
      LOG.error("ArpViewer servlet initialization failed", e);
    }

    // initialize secret for decryption
    sharedSecret = ConfigurationManager
        .getParam(ConfigurationManager.COMMON_SHARED_SECRET);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      LOG.info("POST received");

      // initialization
      HttpSession session = request.getSession();
      LogInfo storage = LogInfo.getInstance();

      // get username
      String username = decrypt(request
          .getParameter(ConfigurationManager.HTTP_PARAM_USERNAME));
      LOG.debug("username=" + username);
      session.setAttribute(SESKEY_USERNAME, username);

      // get returnURL
      String returnURL = request
          .getParameter(ConfigurationManager.HTTP_PARAM_RETURNURL);
      LOG.debug("returnURL=" + returnURL);
      session.setAttribute(SESKEY_RETURNURL, returnURL);

      if (request.getParameter(ConfigurationManager.HTTP_PARAM_EDIT) != null) {
        // start edit flow
        LOG.info("user want to edit the attribute release approval");
        getServletContext().getRequestDispatcher(PAGE_EDIT).forward(request,
            response);
        return;
      } else {
        // Start arp flow
        LOG.info("start arp viewer flow");
        // get providerId
        String providerId = decrypt(request
            .getParameter(ConfigurationManager.HTTP_PARAM_PROVIDERID));
        LOG.debug("providerId=" + providerId);
        session.setAttribute(SESKEY_PROVIDERID, providerId);

        // get released attributes
        String serializedAttributesReleased = decrypt(request
            .getParameter(ConfigurationManager.HTTP_PARAM_ATTRIBUTES));
        LOG.debug("serializedAttributesReleased="
            + serializedAttributesReleased);
        Collection<Attribute> attributesReleased = Attribute
            .unserializeAttributes(serializedAttributesReleased);
        session.setAttribute(SESKEY_ATTRIBUTES, attributesReleased);

        // get globalArpDisabled
        boolean globalArpDisabled = ConfigurationManager
            .makeBoolean(ConfigurationManager
                .getParam(ConfigurationManager.ARPVIEWER_GLOBAL_ARP_DISABLED));
        LOG.debug("globalArpDisabled=" + globalArpDisabled);
        session.setAttribute(SESKEY_GLOBALARP_DISABLED, globalArpDisabled);

        // get the locale
        Locale locale = createLocale(request.getLocale(), ConfigurationManager
            .getParam(ConfigurationManager.ARPVIEWER_USE_LOCAL));
        LOG.debug("locale=" + locale);
        session.setAttribute(SESKEY_LOCALE, locale);

        // getUserInfo
        UserLogInfo userInfo = storage.getData(username);
        LOG.debug("userInfo=" + userInfo);

        if (userInfo == null) {
          if (useTerms) {
            LOG.debug("first visit of the user, redirect to terms page");
            getServletContext().getRequestDispatcher(PAGE_TERMS).forward(request,response);
          } else {
            LOG.debug("first visit of the user, redirect to arp page");
            getServletContext().getRequestDispatcher(PAGE_ARP).forward(request,response);
          }
          return;
        }

        // has user agreed to the current terms version
        if (useTerms && !userInfo.getTermsVersion().equals(TermsOfUseManager.getVersion())) {
          LOG.debug("current terms version are not agreed by user, redirect to the terms page");
          getServletContext().getRequestDispatcher(PAGE_TERMS).forward(request,response);
          return;
        } 
        
        LOG.debug("Terms are not used or current terms version are agreed by user, redirect to the arp page");
        getServletContext().getRequestDispatcher(PAGE_ARP).forward(request,response);
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
      LOG.info("GET received");
      ConfigurationManager.initialize(getServletContext().getInitParameter(
          INITPAR_CONFIG));

      // initialize storage handler
      String storeType = ConfigurationManager
          .getParam(ConfigurationManager.COMMON_STORE_TYPE);
      LogInfo.initialize(storeType);
      LogInfo storage = LogInfo.getInstance();
      LOG.debug("LogInfo (storage) initialized with mode=" + storeType);

      String username = (String) session.getAttribute(SESKEY_USERNAME);
      LOG.debug("username=" + username);
      if (username == null || username.equals(""))
        throw new UApproveException("Username is not set");

      String providerId = (String) session.getAttribute(SESKEY_PROVIDERID);
      LOG.debug("providerId=" + providerId);

      UserLogInfo userInfo = storage.getData(username);
      LOG.debug("userInfo=" + userInfo);

      Collection<Attribute> attributesReleased = (Collection<Attribute>) session
          .getAttribute(SESKEY_ATTRIBUTES);

      String returnURL = (String) session.getAttribute(SESKEY_RETURNURL);
      LOG.debug("returnURL=" + returnURL);

      if (isGetParSet(request, GETPAR_TERMS_CONFIRM)) {
        LOG.info("coming from terms confirmed");
        // check if the user agreed the terms
        if (isGetParSet(request, GETPAR_TERMS_AGREE)) {
          LOG.info("user agreed terms, store, redirect to arp");
          storeUserBasic(userInfo, username, TermsOfUseManager.getVersion());
          getServletContext().getRequestDispatcher(PAGE_ARP).forward(request,
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
        LOG.info("coming from terms declined, redirect to decline page");
        getServletContext().getRequestDispatcher(PAGE_TERMSDECLINED).forward(
            request, response);
        return;
      }

      if (isGetParSet(request, GETPAR_TERMS_DECLINE_BACK)) {
        LOG.info("coming from terms declined back, redirect to terms page");
        getServletContext().getRequestDispatcher(PAGE_TERMS).forward(request,
            response);
        return;
      }

      if (isGetParSet(request, GETPAR_ARP_CONFIRM)) {
        LOG.info("user agreed arp, store, redirect to returnURL="
            + response.encodeRedirectURL(returnURL));
        
        String termsVersion = useTerms ? TermsOfUseManager.getVersion() : "";
        String arp = Attribute.serializeAttributeIDs(attributesReleased);
        LOG.debug("store: username=" + username + " providerId=" + providerId
            + " arp=" + arp + " terms=" + termsVersion + " global="
            + isGetParSet(request, GETPAR_ARP_AGREEGLOBAL));
        storeUser(userInfo, username, termsVersion, providerId, arp,
            isGetParSet(request, GETPAR_ARP_AGREEGLOBAL));
        response.sendRedirect(response.encodeRedirectURL(returnURL));
        return;
      }

      if (isGetParSet(request, GETPAR_ARP_DECLINE)) {
        LOG.info("coming from arp declined, redirect to decline page");
        getServletContext().getRequestDispatcher(PAGE_ARPDECLINED).forward(
            request, response);
        return;
      }

      if (isGetParSet(request, GETPAR_ARP_DECLINE_BACK)) {
        LOG.info("coming from arp declined back, redirect to arp page");
        getServletContext().getRequestDispatcher(PAGE_ARP).forward(request,
            response);
        return;
      }

      // coming here is not ok
      throw new UApproveException("ArpViewer flow error");
    } catch (UApproveException e) {
      doError(request, response, e);
    }
  }

  private void storeUser(UserLogInfo userInfo, String username,
      String termsVersion, String providerId, String attributesReleased,
      boolean globalApproval) throws UApproveException {
    
    LOG.debug("storeUser");
    LogInfo storage = LogInfo.getInstance();
    if (userInfo == null) {
      LOG.debug("create new user");
      
      userInfo = storage.addUserLogInfoData(username, "1.0", new Date()
          .toString(), termsVersion, "no", providerId, attributesReleased);
    } else {
       userInfo.setOndate(new Date().toString());
       userInfo.setTermsVersion(termsVersion);
       userInfo.setGlobal(globalApproval ? "yes" : "no");
       userInfo.addProviderId(providerId, attributesReleased);
    }
    storage.update(userInfo, providerId);
  }

  private void storeUserBasic(UserLogInfo userInfo, String username,
      String termsVersion) throws UApproveException {
    LOG.debug("storeUserBasic");
    LogInfo storage = LogInfo.getInstance();
    
    if (userInfo == null) {
      LOG.debug("create new user");
      userInfo = storage.addUserLogInfoData(username, "1.0", new Date()
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
  public static String getResourceHost(String providerId) {
    int i1 = providerId.indexOf("//") + 2;
    int i2 = providerId.indexOf("/", i1);
    return i2 > 0 ? providerId.substring(0, providerId.indexOf("/", providerId
        .indexOf("//") + 2)) : providerId;
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
  

  public static void doError(HttpServletRequest request,
      HttpServletResponse response, UApproveException e) throws ServletException,
      IOException {
    HttpSession session = request.getSession();
    session.setAttribute(SESKEY_ERROR, e);
    LOG.error("Arpviewer Error", e);
    session.getServletContext().getRequestDispatcher(PAGE_ERROR).forward(
        request, response);
  }

}
