<%@page import="ch.SWITCH.aai.uApprove.viewer.Controller"%>
<%@page import="ch.SWITCH.aai.uApprove.components.UApproveException"%>
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