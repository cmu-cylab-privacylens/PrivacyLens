<%@ page import="
  ch.SWITCH.aai.uApprove.viewer.Controller,
  ch.SWITCH.aai.uApprove.components.TermsOfUseManager,
  java.util.Locale,
  java.util.ResourceBundle"%>

<%@ include file="header.jsp"%>


<%
  /*
 *------------------------------------------------------------------------------------
 *
 * terms.jsp:
 * ----------
 *
 *  Copyright (c) 2005-2006 SWITCH - The Swiss Education & Research Network
 *
 *
 * Purpose: Webpage to show the terms of use, which the user has to accept.
 * 
 * 
 * Usage: This page is invoked from the servlet Controller of the package
 *        ch.SWITCH.aai.uApprove.viewer
 * 
 *
 * 
 * Author: C.Witzig
 * Date: 24.6.2006
 * 
 *
 *
 * Modifications:
 *---------------
 * 1. fixed bug in extracting resource host if there is no second '/'. Dec 22, 2006
 * 
 *--------------------------------------------------------------------------------------
 */


ResourceBundle rb = ResourceBundle.getBundle( Controller.RB_TERMS, (Locale)session.getAttribute(Controller.SESKEY_LOCALE) );
%>
<p><strong> <%=(String) rb.getString("title")%> </strong>
</p>

<p>
<textarea id="terms" readonly="readonly">
<%=TermsOfUseManager.getTermsText()%>
</textarea></p>

<!--- --------------------- Ask for user consent ------------------- -->

<div class="login-field">
<form name="question" action="Controller">
<p><input type="checkbox" name="<%=Controller.GETPAR_TERMS_AGREE%>"> <%= (String) rb.getString( "txt_expl_terms" ) %>
</p>
<div align="right">
<p><input type="submit"
	name="<%=Controller.GETPAR_TERMS_DECLINE%>"
	value="<%=(String) rb.getString( "label_decline") %>">
	<input
	type="submit" name="<%= Controller.GETPAR_TERMS_CONFIRM %>"
	value="<%=(String) rb.getString( "label_confirm") %>"></p>
</div>
</form>
</div>

<br>
<center><%= (String) rb.getString( "txt_comment" ) %></center>

<%@ include file="footer.jsp"%>
