<%@ page import="ch.SWITCH.aai.arpviewer.Controller,
     ch.SWITCH.aai.common.model.LogInfo,
     ch.SWITCH.aai.common.model.UserLogInfo,
     ch.SWITCH.aai.common.ConfigurationManager,
     ch.SWITCH.aai.common.UApproveException,
     org.slf4j.LoggerFactory,
     org.slf4j.Logger,
     java.util.Locale,
     java.util.ResourceBundle"%>



<%@ include file="header.jsp" %>

<%

/*
 *--------------------------------------------------------------------------
 * useredit.jsp:
 * --------------
 * 
 *  Copyright (c) 2005-2006 SWITCH - The Swiss Education & Research Network
 *
 * 
 * Purpose: JSP page to allow a user to edit his/her own 
 *          ARP parameters.
 *
 * Note: currently we do only allow to user to set/unset the global
 *       arp release of his/her attributes. Much more could be added here!
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
Logger LOG = LoggerFactory.getLogger("useredit.jsp");

LOG.debug("arpconfigs="+getServletContext().getInitParameter(Controller.INITPAR_CONFIG));
ConfigurationManager.initialize(getServletContext().getInitParameter(Controller.INITPAR_CONFIG));
LogInfo.initialize(ConfigurationManager.getParam(ConfigurationManager.COMMON_STORE_TYPE));
LogInfo storage = LogInfo.getInstance();

String returnURL;
String username;
String providerId;
String hiddenField = "<input type=\"hidden\" name=\"";
 if (request.getParameter(Controller.GETPAR_STANDALONE) != null ) {
   LOG.info("standalone call");
   // assure that the username is set in a appropiate way
   username = request.getRemoteUser();
   returnURL = request.getParameter(Controller.GETPAR_STANDALONE);
   hiddenField += Controller.GETPAR_STANDALONE;
 } else {
   LOG.info("inline call");
   username = (String) session.getAttribute(Controller.SESKEY_USERNAME);
   returnURL = (String) session.getAttribute(Controller.SESKEY_RETURNURL);
   providerId = (String) session.getAttribute(Controller.SESKEY_PROVIDERID);
   hiddenField += ConfigurationManager.HTTP_PARAM_RETURNURL;
 }
hiddenField += "\" value=\""+returnURL+"\">";

 //start debug
 LOG.debug("returnURL="+returnURL);
 LOG.debug("username="+username);
  // end debug


	// get the language dependent text
	Locale locale =  Controller.createLocale(request.getLocale(), ConfigurationManager
      .getParam(ConfigurationManager.ARPVIEWER_USE_LOCAL));
  
 ResourceBundle rb = ResourceBundle.getBundle( Controller.RB_USEREDIT, locale);
	
	// retrieve the user info

	 if (username==null || username.equals(""))
    throw new UApproveException("Username is not set, can't reset attribute release approval");
	
	UserLogInfo userLogInfo = storage.getData( username );
	
	if (userLogInfo==null) {
	    //throw new UApproveException("User "+username+" unknown, can't reset attribute release approval");
	    response.sendRedirect( response.encodeURL( returnURL ) );
	}
	
	// Is this the callback?
	// If so, adjust the global arp release if the user pressed confirm.
	// Next emove the flag from the return url and redirect to the resource
	if ( request.getParameter(Controller.GETPAR_EDITCONFIRM) != null || request.getParameter(Controller.GETPAR_EDITCANCEL) != null ) {
		if ( request.getParameter(Controller.GETPAR_EDITCONFIRM) != null ) {
			userLogInfo.setGlobal( "no" );
		  userLogInfo.clearArpRelease(); /* ALL */
		  //userLogInfo.clearArpRelease(providerId); /* ONLY FOR THIS PROVIDER */
		  storage.update( userLogInfo );
		}
		response.sendRedirect( response.encodeURL( returnURL ) );
	}

%>
<p><strong> <%= (String) rb.getString("title") %> </strong></p>

    	<p>  <%= (String) rb.getString("explanation") %> </p>

	<div class="form-fields">
    	<form name="question" action="useredit.jsp">
		<div align="right">
		      <%=hiddenField%>
        	<input type="submit" name="<%=Controller.GETPAR_EDITCANCEL%>" value="<%=(String) rb.getString( "lb_cancel") %>">
        	<input type="submit" name="<%=Controller.GETPAR_EDITCONFIRM%>" value="<%=(String) rb.getString( "lb_confirm") %>">
		</div>
    	</form>
	</div>
<%
 } catch (UApproveException e){
   Controller.doError(request, response, e);
 }
%>
<%@ include file="footer.jsp" %>
