<%@ include file="header.jsp" %>
<body>
    <div class="box">
        <div style="float:right;">
            <img src="<%= request.getContextPath()%>/uApprove/logo.png" alt=""/>
        </div>
        <div style="float:left;">
            <p title="${relyingParty.description}">
                <fmt:message key="releaseTo"> <fmt:param value="${relyingParty.name}"/> </fmt:message>
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
		                        <strong>${value}</strong> <br />
		                    </c:forEach>
		                    </td>
		                </tr>
		           </c:forEach>
		           </tbody>
	            </table>
            </div>

            <div>
			    <div id="attributeRelease-consent">
	                <form action="" method="post" style="padding:10px">
	                    <div style="float:left;">
				        <c:if test="${allowGeneralConsent}">
				            <input type="checkbox" id="generalConsent" name="generalConsent" value="true"/>
				            <label for="generalConsent"><fmt:message key="generalConsent"/></label>
				        </c:if>
	                    </div>
	                    <div style="float:right;">
	                        <button type="submit"><fmt:message key="confirm"/></button>
			            </div>
			            <div style="clear:both;"></div>
			        </form>
			    </div>
		    </div>    
        </div>
    </div>
</body>
<%@ include file="footer.jsp" %>