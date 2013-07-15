<%@ include file="header.jsp" %>
<body>
<form method="post" style="padding:10px">
    <div class="box">
            <div class="box_header">
                <img src="<%= request.getContextPath()%>/uApprove/federation-logo.png" alt="" class="federation_logo" >
                <img src="<%= request.getContextPath()%>/uApprove/logo.png" alt="" class="organization_logo">
            </div>
            <p style="margin-top: 70px;">
                <fmt:message key="serviceNameLabel"/><br/>
                <span class="service_name"><idpui:serviceName/></span> <fmt:message key="of"/> <span class="organization_name"><%= organization %></span>
            </p>
            <p style="margin-top: 10px;">
                <fmt:message key="serviceDescriptionLabel"/><br/>
                <span class="service_description"><idpui:serviceDescription/></span><br />
            </p>
            <div id="attributeRelease">
                <table>
                    <thead>
                       <tr>
                          <th colspan="2">
                              <fmt:message key="attributesHeader"/>
                          </th>
                       </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="attribute" items="${attributes}" varStatus="status">
                        <c:choose>
                            <c:when test="${status.count % 2 == 0}">
                                <c:set var="rowStyle" value="background-color:#E4E5E3;"/>
                            </c:when>
                            <c:otherwise>
                                <c:set var="rowStyle" value=""/>
                            </c:otherwise>
                        </c:choose>
                        <tr style="${rowStyle}">
                            <td title="${attribute.description}">
                              ${attribute.name}
                            </td>
                            <td>
                            <c:forEach var="value" items="${attribute.values}">
                                <strong>${fn:replace(value, '$', '<br />')}</strong> <br />
                            </c:forEach>
                            </td>
                        </tr>
                   </c:forEach>
                   </tbody>
                </table>
            </div>

        <div style="float:left;">
            <p>
                    <fmt:message key="confirmationQuestion"/>
            </p>
        <c:if test="${allowGeneralConsent}">
            <p>
                <a href="javascript: void(0)" onclick="document.getElementById('generalConsentDiv').style.display='block'; document.getElementById('triangle').src='triangle2.png'; return false;"><img alt=">" src="triangle1.png" id="triangle" style="padding-right: 6px;"/><fmt:message key="dontShowPageAgain"/></a>
            </p>
            <div id="generalConsentDiv" style="display: none; background-color: #F6F6F6;border: 1px gray solid; padding: 10px; width: 92%;">
                <h3><fmt:message key="globalDataReleaseConsentTitle"/></h3>
                <table>
                        <tr>
                                <td style="vertical-align: top"><input type="checkbox" name="generalConsent" value="true" /></td>
                                <td><fmt:message key="globalDataReleaseConsentCheckbox"/></td>
                        </tr>
                </table>
                <ul>
                        <li><fmt:message key="globalDataReleaseConsentItem1"><fmt:param><idpui:serviceName/></fmt:param></fmt:message></li>
                        <li><fmt:message key="globalDataReleaseConsentItem2"/></li>
                        <li><fmt:message key="globalDataReleaseConsentItem3"/></li>
                </ul>
            </div>    
        </c:if>
     <p style="text-align: center;">
                    <input type="reset" name="noconfirm" value="<fmt:message key="reject"/>" style="margin-right: 30px;" onclick="alert('<fmt:message key="rejectMessage"/>')" />
                    <input type="submit" name="confirm" value="<fmt:message key="accept"/>" />
            </p>

      </div>

    </div>
</form>
</body>
<%@ include file="footer.jsp" %>
