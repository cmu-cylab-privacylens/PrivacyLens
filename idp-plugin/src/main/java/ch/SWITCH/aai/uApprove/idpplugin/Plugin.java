package ch.SWITCH.aai.uApprove.idpplugin;

import java.io.IOException;
import java.util.Date;

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
import ch.SWITCH.aai.uApprove.components.TermsOfUseManager;
import ch.SWITCH.aai.uApprove.components.UApproveException;
import ch.SWITCH.aai.uApprove.idpplugin.UApproveContextBuilder.UApproveContext;
import ch.SWITCH.aai.uApprove.storage.LogInfo;
import ch.SWITCH.aai.uApprove.storage.UserLogInfo;

/**
 * Class Plugin:
 *
 * Copyright (c) 2005-2006 SWITCH - The Swiss Education & Research Network
 */
public class Plugin implements Filter {

  private static final String INITPAR_CONFIG = "Config";
  private static Logger logger = LoggerFactory.getLogger(Plugin.class);

  private boolean useTerms = false;

  private Dispatcher dispatcher;
  private StateAndActionEvaluator evaluator;
  private UApproveContextBuilder contextBuilder;
  
  private String authnContextClassRef;

  
  public void init(FilterConfig filterConfig) throws ServletException {
	  // initialize configuration
      ConfigurationManager.initialize(filterConfig.getInitParameter(INITPAR_CONFIG));
      
      // initialize dispatcher
      dispatcher = new Dispatcher(filterConfig.getServletContext());
      
      // initialize state and action evaluator
      evaluator = new StateAndActionEvaluator(dispatcher);

      // initialize state and action evaluator
      contextBuilder = new UApproveContextBuilder(dispatcher);
      
      // initialize terms of use
      String terms = ConfigurationManager.getParam(ConfigurationManager.COMMON_TERMS);
      if (terms != null && !terms.equals("")) {
        try {
          TermsOfUseManager.initalize(terms);
        } catch (Exception e) {
          throw new UApproveException(e);
        }
        useTerms = true;
        logger.info("Terms of use version {} loaded", TermsOfUseManager.getVersion());
      } else {
    	  logger.info("No terms of use are configured");
      }

      // initialize sp blacklist
      SPBlacklistManager.initialize(ConfigurationManager.getParam(ConfigurationManager.PLUGIN_SP_BLACKLIST));

    
      // check if the filter only should work on a specified authnContextClassRef
      String accr = filterConfig.getInitParameter("authnContextClassRef");
      if (accr != null && !accr.equals("")) {
        authnContextClassRef = accr;
        logger.info("uApprove is only enabled on authnContextClassRef {}", authnContextClassRef);
      }
  }

  public void destroy() {}

  public void doFilter(ServletRequest servletRequest,
	ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
      
	HttpServletRequest request;
	HttpServletResponse response;
	try {
		request = (HttpServletRequest) servletRequest;
    	response = (HttpServletResponse) servletResponse;
	} catch (ClassCastException e) {
    	logger.warn("uApprove only works on HTTP requests / responses");
    	filterChain.doFilter(servletRequest, servletResponse);
		return;
      }
      
      try {
		  switch (evaluator.evaluateAction(request, authnContextClassRef)) {
		  	case PASS_TO_IDP:
		  		dispatcher.dispatchToIdP(request, response, filterChain);
		  		return;
		  	case CHECK_ACCESS:
		  		checkAccess(request, response, filterChain);
		  		return;
		  }
      } catch (UApproveException e) {
      	logger.error("uApprove error", e);
      	throw new ServletException(e);
      }
  }
  
  
  private void checkAccess(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws UApproveException {
	  
	UApproveContext context = contextBuilder.buildContext(request);
	logger.info("uApprove access: {}", context);
	
    // initialize storage handler (stupid, but true)
    String storeType = ConfigurationManager.getParam(ConfigurationManager.COMMON_STORE_TYPE);
    LogInfo.initialize(storeType);
    LogInfo storage = LogInfo.getInstance();
	
    // getUserInfo
    UserLogInfo userInfo = storage.getData(context.getPrincipal());
    
	// create user, if not existent
	if (userInfo == null) {
		userInfo = storage.addUserLogInfoData(context.getPrincipal(), "1.0", new Date().toString(), "", "no", context.getRelyingParty().getEntityId(), null);
		storage.update(userInfo);
	}
    
    // set global ARA flag
    boolean globalConsentGiven = ConfigurationManager.makeBoolean(userInfo.getGlobal());

	// check monitoring mode only
 	if (ConfigurationManager.makeBoolean(ConfigurationManager.getParam(ConfigurationManager.PLUGIN_MONITORING_ONLY))) {
 		logger.debug("monitoring mode only");
 		logAccess(storage, context, globalConsentGiven, false);
 		dispatcher.dispatchToIdP(request, response, filterChain);
        return;
	}
 	
    // check if relying party is in black list
    if (SPBlacklistManager.containsItem(context.getRelyingParty().getEntityId())) {
 		logger.debug("relying party is in black list");
 		logAccess(storage, context, globalConsentGiven, false);
 		dispatcher.dispatchToIdP(request, response, filterChain);
 		return;
    }

      // check if the user wants to reset the settings
      if (context.resetConsent()) {
    	  logger.debug("user wants to reset the settings");
    	  dispatcher.dispatchToViewer(request, response, context);
        return;
      }

      // check if the terms changed
      if (useTerms) {
        String termsVersion = TermsOfUseManager.getVersion();
        String userTermsVersion = userInfo.getTermsVersion();
        if (!userTermsVersion.equalsIgnoreCase(termsVersion)) {
        	logger.debug("user has not agreed to terms of use version {}", termsVersion);
        	dispatcher.dispatchToViewer(request, response, context);
        }
        return;
      }
     
      // check if user has given global approval
      if (ConfigurationManager.makeBoolean(userInfo.getGlobal())) {
   		logger.debug("user has given global approval");
 		logAccess(storage, context, globalConsentGiven, false);
 		dispatcher.dispatchToIdP(request, response, filterChain);
        return;
      }

      // get attributes which will be released
      String attributes = Attribute.serializeAttributeIDs(context.getAttributesReleased());

      // check if it is users first visit to relying party
      // TODO is this check needed?
      if (!userInfo.containsProviderId(context.getRelyingParty().getEntityId())) {
      	logger.debug("users first visit to relying party {}", context.getRelyingParty());
    	dispatcher.dispatchToViewer(request, response, context);
    	return;
      }
      
      // check if the there are ant attributes, which not are already approved for release to relying party 
      if (!Attribute.compareAttributeRelease(userInfo.getAttributesForProviderId(context.getRelyingParty().getEntityId()), attributes)) {
        logger.debug("attributes, which not are already approved for release to relying party {}", context.getRelyingParty());
      	dispatcher.dispatchToViewer(request, response, context);
      	return;
      }
    
      // the user has already given attribute release approval to this relying party
      logger.debug("user has already given attribute release approval to this relying party {}", context.getRelyingParty());
      logAccess(storage, context, globalConsentGiven, false);
      dispatcher.dispatchToIdP(request, response, filterChain);
      return;

  }
  
  private void logAccess(LogInfo storage, UApproveContext context, boolean globalARA, boolean approvalGiven) throws UApproveException {		
	// Is provider access logging enabled
    if (ConfigurationManager.makeBoolean(ConfigurationManager.getParam(ConfigurationManager.PLUGIN_LOG_PROVIDER_ACCESS))) {
    	if (approvalGiven) {
            storage.updateProviderAccess(context.getPrincipal(), context.getRelyingParty().getEntityId(), globalARA);
    	} else {
            storage.updateProviderAccessWithNoARA(context.getPrincipal(), context.getRelyingParty().getEntityId());
    	}
    }
		
  }

}