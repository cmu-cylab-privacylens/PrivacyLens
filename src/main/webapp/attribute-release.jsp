<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ page contentType="text/html; charset=UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <fmt:setLocale value="${locale}"/>
    <fmt:setBundle basename="${bundle}"/>
    
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/uApprove.css"/> 
    <title><fmt:message key="title"/></title>
</head>

<body>

    <div>
        <a class="tooltip" href="">
            <fmt:message key="releaseTo"> <fmt:param value="${relyingParty.name}"/> </fmt:message>
            <c:if test="${not empty relyingParty.description}"> <span>${relyingParty.description}</span> </c:if>
        </a>
    </div>
    
    <div>
	    <table>
		    <thead>
		       <tr>
		          <td colspan="2">
		              <fmt:message key="attributesHeader"/>
		          </td>
		       </tr>
		    </thead>
		    <tbody>
		    <c:forEach var="attribute" items="${attributes}">
		        <tr>
		            <td>
		               <a class="tooltip" href="">${attribute.name}
		                  <c:if test="${not empty attribute.description}"> <span>${attribute.description}</span> </c:if>
		               </a>
		            </td>
		            <td>
		            <c:forEach var="value" items="${attribute.values}">
		                ${value} <br />
		            </c:forEach>
		            </td>
		        </tr>
	       </c:forEach>
	       </tbody>
	    </table>
    </div>
    
    <div>
        <form action="" method="post">
        <c:if test="${allowGeneralConsent}">
            <input type="checkbox" id="generalConsent" name="generalConsent" value="true"/>
            <label for="generalConsent"><fmt:message key="generalConsent"/></label>
        </c:if>
            <button type="submit"><fmt:message key="confirm"/></button>
        </form>
    </div>    
</body>

</html>