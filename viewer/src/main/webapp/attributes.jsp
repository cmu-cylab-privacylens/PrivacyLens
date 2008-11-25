<%@ page import="
  ch.SWITCH.aai.arpviewer.Controller,
  ch.SWITCH.aai.common.Attribute,
  ch.SWITCH.aai.arpviewer.AttributeList,
  java.util.Map,
  java.util.List,
  java.util.Set,
  java.util.ArrayList,
  java.util.Collection,
  java.util.Iterator,
  java.util.Locale,
  java.util.ResourceBundle"
  %>


<%@ include file="header.jsp" %>


<%
  /*
 *------------------------------------------------------------------------------------
 *
 * arp.jsp:
 * ----------
 *
 * Copyright (c) 2005-2006 SWITCH - The Swiss Education & Research Network
 *
 *
 * Purpose: Webpage to show the user which attributes will be transfered to a resource.
 *          The user can accept or deny. In the latter case, he/she cannot of course 
 *          access the resource. 
 * 
 * 
 * Usage: This page is invoked from the servlet Controller of the package
 *        ch.SWITCH.aai.arpviewer. 
 * 
 * Note: 
 * This JSP page uses the classes AttributeDumper and AttributeDescription to display the
 * attributes.
 * 
 * Author: C.Witzig
 * Date: 2.3.2006
 * 
 *
 *
 * Modifications:
 *---------------
 * 1. fixed bug in extracting resource host if there is no second '/'. Dec 22, 2006
 * 2. changed replace with replaceAll in convertAttributeValue such that it compiles with 1.4.2
 *    (courtesy of Jon Warwick, University of Cambridge)
 *    ChW June 18, 2007
 * 
 * 
 *--------------------------------------------------------------------------------------
 */
%>


<%
  String providerId = (String) session.getAttribute(Controller.SESKEY_PROVIDERID);
  boolean globalArpDisabled = (Boolean) session.getAttribute(Controller.SESKEY_GLOBALARP_DISABLED);
  Collection<Attribute> attributes = (Collection<Attribute>) session.getAttribute(Controller.SESKEY_ATTRIBUTES);
  Locale locale = (Locale) session.getAttribute(Controller.SESKEY_LOCALE);
 
  

 
  
	ResourceBundle rb = ResourceBundle.getBundle( Controller.RB_ARP, locale);
	    	out.println( "<p><strong>" + (String) rb.getString("title") + "</strong></p>" );



	// --------------------- Attribute Release table  ------------------- 

	String sTxtExpl = (String) rb.getString( "txt_explanation" );
	sTxtExpl = sTxtExpl.replaceFirst( "\\?", "<tt>" + Controller.getResourceHost(providerId) + "</tt>" ); 
	out.println("<p>" + sTxtExpl + "</p>" );
%>




<!--- ------------------ table with the attributes ------------------ -->
<script type="text/javascript" src="js/wz_tooltip.js"></script>

<center>
<table cellpadding="0" cellspacing="0">
								<tr>
									<td>
<table width="570" id="card" cellpadding="0" cellspacing="0">
<tr>
<td id="idcardTitle" colspan="2">Digital ID Card</td>
</tr>

<%
	// iterate over the attributes
int i = 0;
List<String> drawed = new ArrayList<String>();
for (String attrId : AttributeList.getWhiteList()) {
  for (Attribute attribute: attributes) {
    if (attribute.attributeID.equals(attrId)) {
      Collection<String> values = attribute.attributeValues;  
      if ( values == null || values.isEmpty() )
        continue;
      String valuesHtml = "";
      for (String value : values) 
        valuesHtml += value + "<br />";
    %> 
    <tr bgcolor="<%= i++ % 2 == 0 ? "white" : "#eeeeee" %>" >
    <td class="attr-name"
      onmouseover="Tip('<%=Controller.resolveDisplayDesc(attribute, locale)%>')" onmouseout="UnTip()"
    ><%=Controller.resolveDisplayName(attribute, locale)%></td>
    <td class="attr-value"><%= valuesHtml.replace("$","<br />") %></td>
    </tr>
    <%
      drawed.add(attribute.attributeID);
    }
  }
}
for (Attribute attribute: attributes) {
  if (AttributeList.isBlackListed(attribute.attributeID) || drawed.contains(attribute.attributeID))
    continue;
 
  Collection<String> values = attribute.attributeValues;  
  if ( values == null || values.isEmpty() )
    continue;
  String valuesHtml = "";
  for (String value : values) 
    valuesHtml += value + "<br />";
%> 
<tr bgcolor="<%= i++ % 2 == 0 ? "white" : "#eeeeee" %>" >
    <td class="attr-name"
      onmouseover="Tip('<%=Controller.resolveDisplayDesc(attribute, locale)%>')" onmouseout="UnTip()"
    ><%=Controller.resolveDisplayName(attribute, locale)%></td>
<td class="attr-value"><%= valuesHtml.replace("$","<br />") %></td>
</tr>
<%
}

%>
</table>
</td>
									<td class="card-shadow" valign="top"><div id="noshadow-tr"></div></td>
								</tr>
								<tr>
									<td class="card-shadow" align="left"><div id="noshadow-bl"></div></td>
									<td class="card-shadow">
									</td>
								</tr>
							</table>

</center>

<!--- --------------------- Ask for user consent ------------------- -->

<%
	out.println("<p>" + (String) rb.getString( "txt_cross_boxes" ) + "</p>" );
%> 

	<div class="login-field">
    	<form name="question" action="Controller">
		<% if ( !globalArpDisabled ) { %>
	       		<p>  
        		<input type="checkbox" name="<%=Controller.GETPAR_ARP_AGREEGLOBAL%>" value="on"> <%=rb.getString( "txt_agree_global_arp" ) %>
			</p>
		<% } %>
		<div align="right">
		<p>
	       	<input type="submit" name="<%= Controller.GETPAR_ARP_DECLINE %>" value="<%=rb.getString( "label_decline") %>">
        	<input type="submit" name="<%= Controller.GETPAR_ARP_CONFIRM %>" value="<%=rb.getString( "label_confirm") %>">
    		</p>
		</div>
    	</form>
	</div>

<br>
<center> <%= rb.getString( "txt_comment" ) %> </center>

<%


%>


<%@ include file="footer.jsp" %>