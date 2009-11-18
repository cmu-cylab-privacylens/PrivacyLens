<%@ page import="ch.SWITCH.aai.uApprove.viewer.Controller,
     ch.SWITCH.aai.uApprove.storage.LogInfo,
     ch.SWITCH.aai.uApprove.storage.UserLogInfo,
     ch.SWITCH.aai.uApprove.components.ConfigurationManager,
     ch.SWITCH.aai.uApprove.components.RelyingParty,
     ch.SWITCH.aai.uApprove.components.UApproveException,
     org.slf4j.LoggerFactory,
     org.slf4j.Logger,
     java.util.Locale,
     java.util.ResourceBundle"%>



<%@ include file="header.jsp" %>

<%

/*
 *--------------------------------------------------------------------------
 * reset-approvals.jsp:
 * --------------
 * 
 *  Copyright (c) 2005-2006 SWITCH - The Swiss Education & Research Network
 *
 * 
 * Purpose: JSP page to allow a user to edit his/her own 
 *          parameters.
 *
 * Note: currently we do only allow to user to set/unset the global
 *       release of his/her attributes. Much more could be added here!
 * 
 * 
 * Author: C.Witzig
 * Date: 2.3.2006
 *
 *
 *
 *
 * Modifications:
 * --------------
 * Added parameter standalone_next_url which allows to execute the user_edit.jsp
 *       without being integrated into the login procedure with CAS.
 *       Usage: simply redirect the browser to 
 *       useredit.jsp?standalone_next_url=<url to redirect to>
 *       Suggestion by P.Heritier, UniFR
 *       chw 19.9.2006
 *
 *----------------------------------------------------------------------------- 
 */
 %>
 <%!
String sessionalize(HttpSession session, String key, String value) throws UApproveException {
   value = (value != null && !value.equals("")) ? value : (String) session.getAttribute(key);
   session.setAttribute(key, value);
   if (value != null) return value; else throw new UApproveException(key+" is null");
 }
 %>
 <%
 try {
	Logger LOG = LoggerFactory.getLogger("reset-approvals.jsp");
	
	LOG.debug("configs="+getServletContext().getInitParameter(Controller.INITPAR_CONFIG));
	ConfigurationManager.initialize(getServletContext().getInitParameter(Controller.INITPAR_CONFIG));
	LogInfo.initialize(ConfigurationManager.getParam(ConfigurationManager.COMMON_STORE_TYPE));
	LogInfo storage = LogInfo.getInstance();
	
	String returnURL;
	String principal;
	String entityId;
	String hiddenField = "<input type=\"hidden\" name=\"";
	if (request.getParameter(Controller.GETPAR_STANDALONE) != null ) {
	   LOG.debug("standalone call");
	   // assure that the username is set in a appropiate way
	   principal = request.getRemoteUser();
	   returnURL = request.getParameter(Controller.GETPAR_STANDALONE);
	   hiddenField += Controller.GETPAR_STANDALONE;
	 } else {
	   LOG.debug("inline call");
	   principal = (String) session.getAttribute(Controller.SESKEY_PRINCIPAL);
	   returnURL = (String) session.getAttribute(Controller.SESKEY_RETURNURL);
	   entityId = ((RelyingParty) session.getAttribute(Controller.SESKEY_RELYINGPARTY)).getEntityId();
	   hiddenField += ConfigurationManager.HTTP_PARAM_RETURNURL;
  }
  hiddenField += "\" value=\""+returnURL+"\">";

 //start debug
 LOG.debug("returnURL="+returnURL);
 LOG.debug("principal="+principal);
  // end debug


	// get the language dependent text
	Locale locale =  Controller.createLocale(request.getLocale(), ConfigurationManager
      .getParam(ConfigurationManager.VIEWER_USELOCALE));
  
 ResourceBundle rb = ResourceBundle.getBundle( Controller.RB_RESET, locale);
	
	// retrieve the user info

	 if (principal==null || principal.equals(""))
    throw new UApproveException("Username is not set, can't reset attribute release approval");
	
	UserLogInfo userLogInfo = storage.getData( principal );
	
	if (userLogInfo==null) {
	    //throw new UApproveException("User "+username+" unknown, can't reset attribute release approval");
	    response.sendRedirect( response.encodeURL( returnURL ) );
	}
	
	// Is this the callback?
	// If so, adjust the global release if the user pressed confirm.
	// Next emove the flag from the return url and redirect to the resource
	if ( request.getParameter(Controller.GETPAR_RESET_CONFIRM) != null || request.getParameter(Controller.GETPAR_RESET_CANCEL) != null ) {
		if ( request.getParameter(Controller.GETPAR_RESET_CONFIRM) != null ) {
			userLogInfo.setGlobal( "no" );
		  userLogInfo.clearRelease(); /* ALL */
		  //userLogInfo.clearRelease(entityId); /* ONLY FOR THIS PROVIDER */
		  storage.update( userLogInfo );
		}
		response.sendRedirect( response.encodeURL( returnURL ) );
	}

%>
<p><strong> <%= (String) rb.getString("title") %> </strong></p>

    	<p>  <%= (String) rb.getString("explanation") %> </p>

	<div class="form-fields">
  <form name="question" action="reset-approvals.jsp">
		<div align="right">
		      <%=hiddenField%>
        	<input type="submit" name="<%=Controller.GETPAR_RESET_CANCEL%>" value="<%=(String) rb.getString( "lb_cancel") %>">
        	<input type="submit" name="<%=Controller.GETPAR_RESET_CONFIRM%>" value="<%=(String) rb.getString( "lb_confirm") %>">
		</div>
   </form>
	</div>
<%
 } catch (UApproveException e){
   Controller.doError(request, response, e);
 }
%>
<%@ include file="footer.jsp" %>
