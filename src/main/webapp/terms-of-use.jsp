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
	   <c:out value="${tou.content}" escapeXml="false"/>
	</div>

    <div>
        <form action="" method="post" >
            <input type="checkbox" id="accept" name="accept" value="true"/>
            <label for="accept"><fmt:message key="accept"/></label>
            <button type="submit"><fmt:message key="confirm"/></button>
        </form>
    </div>
    
</body>
</html>