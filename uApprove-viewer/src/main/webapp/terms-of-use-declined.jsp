<%@ page
	import="
   ch.SWITCH.aai.uApprove.viewer.Controller,
   ch.SWITCH.aai.uApprove.components.RelyingParty,
	 java.util.*,
	 javax.servlet.*"%>

<%@ include file="header.jsp"%>


<%

/*
 *------------------------------------------------------------------------------------
 *
 * terms_declined.jsp:
 * ---------------
 *
 *  Copyright (c) 2005-2006 SWITCH - The Swiss Education & Research Network
 * 
 *
 * Purpose: Webpage to remind the user that he declined the terms and therefore
 * 	    he cannot access the resource.
 *
 * 
 * Usage: This page is invoked from the servlet Controller of the package
 *        ch.SWITCH.aai.uApprove.viewer. 
 * 
 *
 * Author: C.Witzig
 * Date: 26.6.2006
 * Addapted by H. Reusser 
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
RelyingParty relyingParty = (RelyingParty) session.getAttribute(Controller.SESKEY_RELYINGPARTY);
Locale locale = (Locale) session.getAttribute(Controller.SESKEY_LOCALE);
ResourceBundle rb = ResourceBundle.getBundle( Controller.RB_TERMS_DECLINED, locale );



out.println( "<p><strong>" + (String) rb.getString("title") + "</strong></p>" );
String[] sKeys = { "txt1", "txt2", "txt3" };
for ( int i = 0; i < sKeys.length; i++ ) {
        String sText = (String) rb.getString( sKeys[i] );
        sText = sText.replaceFirst( "\\?", Controller.getRelyingPartyName(relyingParty, locale) );
        out.println( sText );
}


%>


<%@page import="ch.SWITCH.aai.uApprove.components.RelyingParty"%><div
	class="login-field">
<form name="question" action="Controller">
<div align="left"><input type="submit"
	name="<%= Controller.GETPAR_TERMS_DECLINE_BACK %>"
	value='<%= rb.getString( "label_back") %>'></div>
</form>
</div>

<br>
<center><%= rb.getString( "txt_comment" ) %></center>


<%@ include file="footer.jsp"%>
