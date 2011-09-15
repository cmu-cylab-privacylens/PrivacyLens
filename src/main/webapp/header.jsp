<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ page contentType="text/html; charset=UTF-8" %>

<!DOCTYPE html>
<html>

	<head>
	    <fmt:setLocale value="${locale}"/>
	    <fmt:setBundle basename="${bundle}"/>
	    
	    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/uApprove/styles.css"/> 
	    <!--[if lte IE 8]>
	    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/uApprove/styles-patch-ie8.css"/>
	    <![endif]-->
	    <!--[if lte IE 7]>
	    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/uApprove/styles-patch.css"/>
	    <![endif]-->
	    <title><fmt:message key="title"/></title>
	</head>