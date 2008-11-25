<%@page import="ch.SWITCH.aai.arpviewer.Controller"%>
<%@page import="ch.SWITCH.aai.common.UApproveException"%>
<%@ include file="header.jsp" %>


<%
 UApproveException e = (UApproveException) session.getAttribute(Controller.SESKEY_ERROR);
 String errorMessage = e.getMessage();
%>

<h1 style="color:red;"> Error </h1>
<p>
<strong> Message:</strong> <br/><br/>
<tt><%=errorMessage%></tt>
</p>

<%@ include file="footer.jsp" %>