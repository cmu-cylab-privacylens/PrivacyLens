<%@ page import="ch.SWITCH.aai.uApprove.viewer.Controller,
		 java.util.*,
		 javax.servlet.*"%>


<%@ include file="header.jsp" %>


<%

/*
 *------------------------------------------------------------------------------------
 *
 * attributes_declined.jsp:
 * ---------------
 * 
 *  Copyright (c) 2005-2006 SWITCH - The Swiss Education & Research Network
 *
 *
 *
 * Purpose: Webpage to remind the user that he decline the release and therefore
 * 	    he cannot access the resource.
 *
 * 
 * Usage: This page is invoked from the servlet Controller of the package
 *        ch.SWITCH.aai.uApprobe.viewer. 
 * 
 *
 * Author: C.Witzig
 * Date: 26.6.2006
 * 
 *
 *
 * Modifications:
 *---------------
 * 1. fixed bug in extracting resource host if there is no second '/'. Dec 22, 2006
 * 
 * 
 *--------------------------------------------------------------------------------------
 */

%>


<%

String entityId = (String) session.getAttribute(Controller.SESKEY_ENTITYID);
ResourceBundle rb = ResourceBundle.getBundle( Controller.RB_ATTRIBUTES_DECLINED, (Locale) session.getAttribute(Controller.SESKEY_LOCALE) );


out.println( "<p><strong>" + (String) rb.getString("title") + "</strong></p>" );

String[] sKeys = { "txt1", "txt2", "txt3" };
for ( int i = 0; i < sKeys.length; i++ ) {
        String sText = (String) rb.getString( sKeys[i] );
        sText = sText.replaceFirst( "\\?", Controller.getResourceHost(entityId) );
        out.println( sText );
}


%>

	<div class="login-field">
    	<form name="question" action="Controller">
	<div align="left">
	<input type="submit" name="<%= Controller.GETPAR_ATTRIBUTES_DECLINE_BACK %>" value="<%=(String) rb.getString( "label_back") %>">
	</div>
    	</form> 
	</div>

<br>
<center> <%=(String) rb.getString( "txt_comment" ) %> </center>


<%@ include file="footer.jsp" %>
