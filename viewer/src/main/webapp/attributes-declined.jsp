<%@ page import="ch.SWITCH.aai.arpviewer.Controller,
		 java.util.*,
		 javax.servlet.*"%>


<%@ include file="header.jsp" %>


<%

/*
 *------------------------------------------------------------------------------------
 *
 * arp_declined.jsp:
 * ---------------
 * 
 *  Copyright (c) 2005-2006 SWITCH - The Swiss Education & Research Network
 *
 *
 *
 * Purpose: Webpage to remind the user that he decline the arp and therefore
 * 	    he cannot access the resource.
 *
 * 
 * Usage: This page is invoked from the servlet Controller of the package
 *        ch.SWITCH.aai.arpviewer. 
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

String providerId = (String) session.getAttribute(Controller.SESKEY_PROVIDERID);
ResourceBundle rb = ResourceBundle.getBundle( Controller.RB_ARP_DECLINED, (Locale) session.getAttribute(Controller.SESKEY_LOCALE) );


out.println( "<p><strong>" + (String) rb.getString("title") + "</strong></p>" );

String[] sKeys = { "txt1", "txt2", "txt3" };
for ( int i = 0; i < sKeys.length; i++ ) {
        String sText = (String) rb.getString( sKeys[i] );
        sText = sText.replaceFirst( "\\?", Controller.getResourceHost(providerId) );
        out.println( sText );
}


%>

	<div class="login-field">
    	<form name="question" action="Controller">
	<div align="left">
	<input type="submit" name="<%= Controller.GETPAR_ARP_DECLINE_BACK %>" value="<%=(String) rb.getString( "label_back") %>">
	</div>
    	</form>
	</div>

<br>
<center> <%=(String) rb.getString( "txt_comment" ) %> </center>


<%@ include file="footer.jsp" %>
