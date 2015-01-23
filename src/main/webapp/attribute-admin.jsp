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
<%@ include file="logos.jsp" %>
            </div>
<p style="margin-top: 70px;">
<span class="user_name">
<b>${userName}'s</b> logins via <b>${idpName}</b><br/>
</span>

            <div id="recentLogins">
<p><fmt:message key="recentLogins"/><br/>
<div id="recentLoginsList">
<c:forEach var="lastLoginEvent" items="${lastLoginEvents}" varStatus="status">
${lastLoginEvent.dateTimeString} ago: <a href="#" id="loginevent1" onclick="formSubmit('loginEvent', '${lastLoginEvent.loginEventId}');">${lastLoginEvent.service}</a><br/>
</c:forEach>
</p>
</div>
</div>
<div id="servicesUsed">
<p><fmt:message key="servicesUsed"/><br/>
<div id="servicesUsedList">
<c:forEach var="relyingParty" items="${relyingPartiesList}" varStatus="status">
<a href="#" id="service${status.count}" onclick="formSubmit('service', '${plutils:escapeJS(relyingParty)}');">${relyingParty}</a><br/>
</c:forEach>
</p>
</div>
</div>
<p style="text-align: center;">
<!--
<input type="submit" name="help" value="<fmt:message key="litHelp"/>" />
                    <input type="button" name="close" value="<fmt:message key="litClose"/>" onClick="self.close();" />
                    <input type="close" id="explainLink" name="explain" value="<fmt:message key="litExplain"/>" onClick="$('#explain').dialog('open');" />
-->
</p>
      </div>
</form>
</body>
<%@ include file="footer.jsp" %>
