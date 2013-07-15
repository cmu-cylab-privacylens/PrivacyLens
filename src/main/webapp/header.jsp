<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="urn:mace:shibboleth:2.0:idp:ui" prefix="idpui" %>
<%@ page import="edu.internet2.middleware.shibboleth.common.relyingparty.RelyingPartyConfigurationManager" %>
<%@ page import="edu.internet2.middleware.shibboleth.idp.authn.LoginContext" %>
<%@ page import="edu.internet2.middleware.shibboleth.idp.util.HttpServletHelper" %>
<%@ page import="java.util.Locale" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.opensaml.saml2.metadata.EntityDescriptor" %>
<%@ page import="org.opensaml.saml2.metadata.OrganizationDisplayName" %>
<%@ page import="org.opensaml.saml2.metadata.OrganizationURL" %>
<%@ page import="org.opensaml.util.storage.StorageService" %>

<%
   // Get organisation data
   StorageService storageService = HttpServletHelper.getStorageService(application);
   LoginContext loginContext = HttpServletHelper.getLoginContext(storageService,application, request);
   RelyingPartyConfigurationManager rpConfigMngr = HttpServletHelper.getRelyingPartyConfigurationManager(application);
   EntityDescriptor metadata = HttpServletHelper.getRelyingPartyMetadata(loginContext.getRelyingPartyId(), rpConfigMngr);
   
   String organization = "";
   if (metadata.getOrganization() != null && metadata.getOrganization().getDisplayNames() != null){
     String localizedOrganizationDisplayName = null;
     String localizedOrganizationURL = null;

     // try to find localized name
     Locale locale = (Locale)request.getAttribute("locale");
     if (locale != null) {
       for (OrganizationDisplayName element : metadata.getOrganization().getDisplayNames()) {
         if (StringUtils.equals(element.getName().getLanguage(), locale.getLanguage())) {
           localizedOrganizationDisplayName = element.getName().getLocalString();
           break;
         }
       }
       for (OrganizationURL element : metadata.getOrganization().getURLs()) {
         if (StringUtils.equals(element.getURL().getLanguage(), locale.getLanguage())) {
           localizedOrganizationURL = element.getURL().getLocalString();
           break;
         }
       }
     }

     // fallback values if matching localization not available
     if (localizedOrganizationDisplayName == null && metadata.getOrganization().getDisplayNames().get(0) != null){
         localizedOrganizationDisplayName = metadata.getOrganization().getDisplayNames().get(0).getName().getLocalString();
     }
     if (localizedOrganizationURL == null && metadata.getOrganization().getURLs().get(0) != null){
       localizedOrganizationURL = metadata.getOrganization().getURLs().get(0).getURL().getLocalString();
     }

     if (localizedOrganizationDisplayName != null){
       if (localizedOrganizationURL != null) {
         organization += "<a href='" + localizedOrganizationURL + "' target='_blank'>";
         organization += localizedOrganizationDisplayName;
         organization += "</a>";
       } else {
         organization += "<strong>";
         organization += localizedOrganizationDisplayName;
         organization += "</strong>";
       }
     }
   }

/*
   if (metadata.getOrganization() != null && metadata.getOrganization().getDisplayNames().get(0) != null){
      organization += " of ";
      if (metadata.getOrganization().getURLs() != null){
         organization += "<a href='" + metadata.getOrganization().getURLs().get(0).getURL().getLocalString() + "' target='_blank'>";
         organization += metadata.getOrganization().getDisplayNames().get(0).getName().getLocalString();
         organization += "</a>";
      } else {
        organization += "<strong>";
        organization += metadata.getOrganization().getDisplayNames().get(0).getName().getLocalString();
        organization += "</strong>";
      }
   }
*/

%>

<%@ page contentType="text/html; charset=UTF-8" %>

<!DOCTYPE html>
<html>

    <head>
        <fmt:setLocale value="${locale}"/>
        <fmt:setBundle basename="${bundle}"/>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;">
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/uApprove/styles.css"/>
        <title><fmt:message key="title"/></title>

    </head>
