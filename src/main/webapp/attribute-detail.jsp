<%@ include file="header.jsp" %>
<body>
<script>
function handleConsentButton(id) {
$.get('<%= request.getContextPath() %>/uApprove/AttributeReleaseAjaxServlet',function(responseJson) {
});
var select = $('#item'+id);
var input = $('#input'+id);
var span = $('#span'+id);

var newState = !(input.attr("value") == 1);
span.css("text-decoration", (newState ? "none" : "line-through"));
input.attr("value", newState ? "1" : "0");
select.attr("src","<%= request.getContextPath() %>/uApprove/" + (newState ? "" : "not_") + "sending.png");
 }
 
 </script>
<form method="post" style="padding:10px">
    <div class="box">
            <div class="box_header">
                <img src="<%= request.getContextPath()%>/uApprove/federation-logo.png" alt="" class="federation_logo" >
                <img src="<%= request.getContextPath()%>/uApprove/logo.png" alt="" class="organization_logo">
            </div>
            <p style="margin-top: 70px;">
                <span class="service_name">
                <!-- CMU's calendar is asking for -->
<fmt:message key="arRequest"><fmt:param>${service}</fmt:param><fmt:param>${idpOrganization}</fmt:param></fmt:message>
</span>
            </p>

            <div id="attributeDetail">
                    <c:forEach var="attributeBean" items="${attributeBeans}" varStatus="status">
${attributeBean.html}
<!-- <div style="clear:both"></div> -->
</c:forEach><br/> 


                ${requirementStatement }<br/>
                <br/>
<b><fmt:message key="arAllowQuestion"><fmt:param>${service}</fmt:param></fmt:message></b><br/>
                
            </div>
<script>
$(function() {
  $("#explain").dialog({
    autoOpen: false,
    modal: true,
    buttons: {
      "Close": function() {
        $(this).dialog("close");
      },
    },
  });
});
</script>
<div id="explain" title="Explanation">
<p>
You are attempting to reach ${service}.  Before this can happen,
${service} asks ${idpOrganization} to send it some information about you.  This
information is listed on the page you just came from (click "close" to
see the page again).
</p>
<p>
Some of the information is required, and ${service} will not
function properly without it.  Some information may be optional.  In
this case, you can choose whether to send the information to ${service}
by using the toggle switches on the page you just came from.
To learn more about each type of information, including how ${service}
will use it, please click the
<img src="<%= request.getContextPath()%>/uApprove/info.png" style="vertical-align:middle" />
icons on the page you just came from.
</p>
<p>
Clicking "Yes" on the previous page will take you to ${service},
and will send to ${service} any information marked "will send."
Clicking "No" on the previous page will take you to your
${idpOrganization} login history.
</p>
<p>
<a href="mailto:${adminMail}">Click here</a> to contact an administrator if you have further
questions.
</p>
</div>
                 <p style="text-align: center;">
                    <input type="submit" name="yes" value="<fmt:message key="litYes"/>" />
                    <input type="submit" name="no" value="<fmt:message key="litNo"/>" />
                    <input type="submit" id="settingsLink" name="history" value="<fmt:message key="litSettingsHistory"/>" />
                    <input type="foobar" id="explainLink" name="explain" value="<fmt:message key="litExplain"/>" onClick="$('#explain').dialog('open');" />
            </p>

      </div>
</form>
</body>
<%@ include file="footer.jsp" %>
