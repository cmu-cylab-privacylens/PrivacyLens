<%@ include file="header.jsp" %>
<body>
<form method="post" name="adminForm" style="padding:10px">
<input type="hidden" id="choice" name="choice" />
<input type="hidden" id="section" name="section" />

<script>
function handleConsentButton(id) {
/*
$.get('<%= request.getContextPath() %>/uApprove/AttributeReleaseAjaxServlet',function(responseJson) {
});
*/
var select = $('#item'+id);
var input = $('#input'+id);
var span = $('#span'+id);

var newState = !(input.attr("value") == 1);
span.css("text-decoration", (newState ? "none" : "line-through"));
input.attr("value", newState ? "1" : "0");
select.attr("src","<%= request.getContextPath() %>/uApprove/" + (newState ? "" : "not_") + "sending.png");
 }

 </script>

<script>
function formSubmit(section, choice) {
  document.getElementById('section').value=section;
  document.getElementById('choice').value=choice;
  document.adminForm.submit();
}
</script>
    <div class="box">
            <div class="box_header">
                <img src="<%= request.getContextPath()%>/uApprove/federation-logo.png" alt="" class="federation_logo" >
                <img src="<%= request.getContextPath()%>/uApprove/logo.png" alt="" class="organization_logo">
            </div>
<p style="margin-top: 70px;">
<span class="service_name">
Logged in to <b>${loginEvent.serviceName}</b> on <joda:format value="${loginEvent.date}" pattern="yyyy-MM-dd HH:mm" />
</span><br /><br />
Items sent:<br />
<div id="loginEventDetail">
<c:forEach var="attribute" items="${loginEventDetail.attributes}" varStatus="status">
<c:choose>
<c:when test="${attribute.id == 'eduPersonEntitlement' }">
${attribute.description}<br />
</c:when>
<c:otherwise>
<c:set var="avString" value="" />
         <c:forEach var="value" items="${attribute.values}">
<c:set var="avString" value="${avString}${fn:replace(value, '$', ',')}" />
                            </c:forEach>
${attribute.description}: "${avString}"<br />
</c:otherwise>
</c:choose>
</c:forEach>
<br />
</div>
Next time you access ${loginEvent.serviceName}, ${idpOrganization} should:<br/>
<input type="radio" name="forceShowInterface" value="yes" id="btn1" checked/>Ask whether and what items to send to ${loginEvent.serviceName}.<br/>
<input type="radio" name="forceShowInterface" value="no" id="btn2" />Send the following items automatically, but remind you that they are being sent.<br/>
<script>
$('#btn1').click(function() {
  $('#attributeDetail').hide();
});
$('#btn2').click(function() {
    $('#attributeDetail').show();
/*
    if ($('#attributeDetail').is(':hidden')) {
      $('#btnShowHide').text('Show');
    } else {
      $('#btnShowHide').text('Hide');
    }
*/
});
</script>
<div id="attributeDetail" style="display:none;">
<div style="margin-left: 1em;">
                    <c:forEach var="attributeBean" items="${attributeBeans}" varStatus="status">
${attributeBean.html}
<!-- <div style="clear:both"></div> -->
</c:forEach><br/>
</div>
${idpOrganization} will remind you what items are being sent...<br/>
Every
<select name="reminderInterval" id="reminderInterval">
<option value="1">single time</option>
<option value="2">2 times</option>
<option value="3">3 times</option>
<option value="4">4 times</option>
<option value="5">5 times</option>
<option value="10">10 times</option>
<option value="15">15 times</option>
</select>
you log into ${loginEvent.serviceName}.<br/>
</div>
<script>
var button;
if (${forceShow}) {
  button = document.adminForm.btn1;
} else {
  button = document.adminForm.btn2;
}
button.checked = true;
button.click();

var selector = document.adminForm.reminderInterval;
for (i = 0; i < selector.options.length; i++) {
  if (selector.options[i].value == ${reminderInterval}) {
    selector.options[i].selected = true;
    break;
  }
}
</script>

<p style="text-align: center;">
<input type="submit" name="save" value="<fmt:message key="litSave"/>" />
<input type="submit" name="back" value="<fmt:message key="litBack"/>" />
<!--
                    <input type="close" id="explainLink" name="explain" value="<fmt:message key="litExplain"/>" onClick="$('#explain').dialog('open');" />
-->
</p>
      </div>
</form>
</body>
<%@ include file="footer.jsp" %>
