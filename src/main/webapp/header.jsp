<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="urn:mace:shibboleth:2.0:idp:ui" prefix="idpui" %>

<%@ page contentType="text/html; charset=UTF-8" %>

<!DOCTYPE html>
<html>

	<head>
	    <fmt:setLocale value="${locale}"/>
	    <fmt:setBundle basename="${bundle}"/>
	    
	    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/uApprove/styles.css"/>
	    <title><fmt:message key="title"/></title>
	</head>
