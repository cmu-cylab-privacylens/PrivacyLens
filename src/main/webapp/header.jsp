<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.joda.org/joda/time/tags" prefix="joda" %>
<%@ taglib uri="/plutils" prefix="plutils" %>
<%@ taglib uri="urn:mace:shibboleth:2.0:idp:ui" prefix="idpui" %>

<%@ page contentType="text/html; charset=UTF-8" %>

<!DOCTYPE html>
<html>

    <head>
        <fmt:setLocale value="${locale}"/>
        <fmt:setBundle basename="${bundle}"/>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;">
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/PrivacyLens/jquery-ui.css"/>
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/PrivacyLens/styles.css"/>
        <title><fmt:message key="title"/></title>
        <script src="<%= request.getContextPath()%>/PrivacyLens/jquery.js"></script>
        <script src="<%= request.getContextPath()%>/PrivacyLens/jquery-ui.js"></script>
    </head>
