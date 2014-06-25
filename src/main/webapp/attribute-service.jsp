<%@ include file="header.jsp" %>
<body>
<form method="post" name="adminForm" style="padding:10px">
<input type="hidden" id="choice" name="choice" />
<input type="hidden" id="section" name="section" />

<script>
function formSubmit(section, choice) {
  document.getElementById('section').value=section;
  document.getElementById('choice').value=choice;
  document.adminForm.submit();
}
</script>
    <div class="box">
            <div class="box_header">
                <img src="<%= request.getContextPath()%>/PrivacyLens/federation-logo.png" alt="" class="federation_logo" >
                <img src="<%= request.getContextPath()%>/PrivacyLens/logo.png" alt="" class="organization_logo">
            </div>
<p style="margin-top: 70px;">
<span class="user_name">
<b>${userName}'s</b> logins to <b>${relyingParty}</b> via <b>${idpName}</b><br/>
</span>

            <div id="recentLogins">
<p><fmt:message key="recentLogins"/><br/>
<div id="recentLoginsList">
<c:forEach var="lastLoginEvent" items="${lastLoginEvents}" varStatus="status">
<a href="#" id="loginevent1" onclick="formSubmit('loginEvent', '${lastLoginEvent.loginEventId}');">${lastLoginEvent.dateTimeString} ago</a><br/>
</c:forEach>
</p>
</div>
</div>
<p style="text-align: center;">
<input type="submit" name="back" value="<fmt:message key="litBack"/>" />
<!--
                    <input type="close" id="explainLink" name="explain" value="<fmt:message key="litExplain"/>" onClick="$('#explain').dialog('open');" />
-->
</p>
      </div>
</form>
</body>
<%@ include file="footer.jsp" %>
