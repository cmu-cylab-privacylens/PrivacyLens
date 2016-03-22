<%@ include file="header.jsp" %>
<body>
<form action="${flowExecutionUrl}&_eventId=proceed" method="post" style="padding:10px">
<input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
    <div class="box">
            <div class="box_header">
<%@ include file="logos.jsp" %>
            </div>
            <p style="margin-top: 70px;">
                <span class="service_name">
                <!-- CMU's calendar is asking for -->
<fmt:message key="arPassAttributes"><fmt:param><strong>${service}</strong></fmt:param></fmt:message>
</span>
            </p>

            <div id="attributeDetail">
${service} will be sent your ${attributeList}.
<!--
                    <c:forEach var="attribute" items="${attributes}" varStatus="status">
                    ${status.first ? '' : (status.last ? ' and ' : ' ')}
${attribute.description }: 
                           <c:forEach var="value" items="${attribute.values}">
                                <strong>${fn:replace(value, '$', '<br />')}</strong> <br />
                            </c:forEach>
<br/>
                   </c:forEach>
-->

            </div>
     <p style="text-align: center;">
                    <input type="submit" name="yes" value="<fmt:message key="litOK"/>" />
                    <input type="submit" name="no" value="<fmt:message key="litChange"/>" />

            </p>

      </div>

    </div>
</form>
</body>
<%@ include file="footer.jsp" %>
