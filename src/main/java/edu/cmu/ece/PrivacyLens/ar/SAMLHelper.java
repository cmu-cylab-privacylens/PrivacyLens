/*
 * Copyright (c) 2011, SWITCH
 * Copyright (c) 2013, Carnegie Mellon University
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of SWITCH nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SWITCH BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.cmu.ece.PrivacyLens.ar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.metadata.AttributeConsumingService;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.ServiceDescription;
import org.opensaml.saml2.metadata.ServiceName;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.ece.PrivacyLens.AttributeUtils;
import edu.internet2.middleware.shibboleth.common.attribute.AttributeRequestException;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.SAML2AttributeAuthority;
import edu.internet2.middleware.shibboleth.common.attribute.provider.ScopedAttributeValue;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolver;
import edu.internet2.middleware.shibboleth.common.profile.provider.BaseSAMLProfileRequestContext;
import edu.internet2.middleware.shibboleth.common.relyingparty.RelyingPartyConfiguration;
import edu.internet2.middleware.shibboleth.common.relyingparty.provider.SAMLMDRelyingPartyConfigurationManager;
import edu.internet2.middleware.shibboleth.common.session.Session;

/**
 * SAML Helper.
 */
public class SAMLHelper {

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(SAMLHelper.class);

    /** The attribute authority. */
    private SAML2AttributeAuthority attributeAuthority;

    /** The relying party configuration manager. */
    private SAMLMDRelyingPartyConfigurationManager relyingPartyConfigurationManager;

    /** The metadata provider. */
    private MetadataProvider metadataProvider;

    /** The attribute processor. */
    private AttributeProcessor attributeProcessor;

    /** The attribute resolver. */
    private AttributeResolver attributeResolver;

    /**
     * Sets the attribute authority.
     * 
     * @param attributeAuthority The attributeAuthority to set.
     */
    public void setAttributeAuthority(final SAML2AttributeAuthority attributeAuthority) {
        this.attributeAuthority = attributeAuthority;
    }

    /**
     * Sets the relying party configuration manager.
     * 
     * @param relyingPartyConfigurationManager The relyingPartyConfigurationManager to set.
     */
    public void setRelyingPartyConfigurationManager(
            final SAMLMDRelyingPartyConfigurationManager relyingPartyConfigurationManager) {
        this.relyingPartyConfigurationManager = relyingPartyConfigurationManager;
    }

    /**
     * Sets the attribute processor.
     * 
     * @param attributeProcessor The attributeProcessor to set.
     */
    public void setAttributeProcessor(final AttributeProcessor attributeProcessor) {
        this.attributeProcessor = attributeProcessor;
    }

    /**
     * Sets the attribute resolver.
     * 
     * @param attributeResolver The attributeResolver to set.
     */
    public void setAttributeResolver(final AttributeResolver attributeResolver) {
        this.attributeResolver = attributeResolver;
    }

    /**
     * Initializes the SAML helper.
     */
    public void initialize() {
        Validate.notNull(attributeAuthority, "Attribute Authority not set.");
        Validate.notNull(attributeResolver, "Attribute Resolver not set.");
        Validate.notNull(relyingPartyConfigurationManager, "Relying Party Configuration Manager not set.");
        metadataProvider = relyingPartyConfigurationManager.getMetadataProvider();
        Validate.notNull(metadataProvider, "Metadata Provider not set.");
        Validate.notNull(attributeProcessor, "Attribute Processor not set.");
    }

    /**
     * Resolves the attributes.
     * 
     * @param principalName The principal name.
     * @param relyingPartyId The relying party id.
     * @param locale The locale.
     * @param session The session.
     * @return Returns a list of attributes.
     */
    public List<Attribute> resolveAttributes(final String principalName, final String relyingPartyId,
            final Locale locale, final Session session) {
        logger.trace("[SAMLHelper] resolveAttributes");
        @SuppressWarnings("rawtypes") final BaseSAMLProfileRequestContext requestCtx =
                buildRequestContext(principalName, relyingPartyId, session);

        Collection<?> requestedAttributes = requestCtx.getRequestedAttributesIds();
        if (requestedAttributes == null || requestedAttributes.isEmpty()) {
            logger.trace("No attributes are requested, but IdP is making all available");
            // let requestedAttributes not be null
            requestedAttributes = new ArrayList();
        } else {
            requestedAttributes = Arrays.asList(requestedAttributes);
        }

        @SuppressWarnings("rawtypes") Map<String, BaseAttribute> baseAttributes = null;
        try {
            baseAttributes = attributeResolver.resolveAttributes(requestCtx);
            // getting attributes from the attribute authority means pulling them through the filter
            // baseAttributes = attributeAuthority.getAttributes(requestCtx);
        } catch (final AttributeRequestException e) {
            logger.error("Error while retrieving attributes for {}.", principalName, e);
            throw new IllegalStateException(e);
        }

        final List<Attribute> attributes = new ArrayList<Attribute>();
        for (final BaseAttribute<?> baseAttribute : baseAttributes.values()) {

            // consider requested == required
            boolean required = requestedAttributes.contains(baseAttribute.getId());

            // XXX demo make eppn required
            if (baseAttribute.getId().equals("eduPersonPrincipalName")) {
                required = true;
            }

            final List<String> attributeValues = new ArrayList<String>();
            for (final Object valueObj : baseAttribute.getValues()) {
                if (valueObj instanceof NameID) {
                    final NameID nameIdObject = (NameID) valueObj;
                    attributeValues.add(nameIdObject.getValue());
                } else if (valueObj instanceof ScopedAttributeValue) {
                    final ScopedAttributeValue scopedAttr = (ScopedAttributeValue) valueObj;
                    attributeValues.add(scopedAttr.getValue() + "@" + scopedAttr.getScope());
                } else if (valueObj != null) {
                    attributeValues.add(String.valueOf(valueObj));
                }
            }

            logger.trace("Attribute: {} {}, localized names {}, localized descriptions {}.", new Object[] {
                    baseAttribute.getId(), attributeValues, baseAttribute.getDisplayNames().keySet(),
                    baseAttribute.getDisplayDescriptions().keySet(),});

            final String id = baseAttribute.getId();
            String name = baseAttribute.getDisplayNames().get(locale);
            String description = baseAttribute.getDisplayDescriptions().get(locale);

            // check for unset values
            // in ShibbolethAttributeResolver around line 258, if the SP doesn't request attributes, all will be
            // selected for handing out.
            // apparently the descriptions of the attribute are to be provided by an (optional)
            // AttributeConsumingService on
            // the SP (?)
            // but if it is not there, then the descriptions will be null.
            // check if attributes are requested:
            // resolutionContext.getAttributeRequestContext.getRequestedAttributeIds == null || isEmpty

            if (name == null || name.isEmpty()) {
                name = AttributeUtils.getName(id);
            }

            if (description == null || description.isEmpty()) {
                description = AttributeUtils.getDescription(id);
            }

            attributes.add(new Attribute(id, name, description, attributeValues, required));

            // attributes.add(AttributeUtils.genAttribute(baseAttribute, locale));
        }

        logger.trace("{} attributes resolved.", attributes.size());
        attributeProcessor.removeBlacklistedAttributes(attributes);
        logger.trace("{} attributes retained after removing blacklisted attributes.", attributes.size());
        for (final Attribute attribute : attributes) {
            logger.trace("Raw values for attribute {}: {}.", attribute.getId(), attribute.getValues());
            attributeProcessor.removeEmptyValues(attribute);
            attributeProcessor.removeDuplicateValues(attribute);
            logger.trace("Cleaned values for attribute {}: {}.", attribute.getId(), attribute.getValues());
        }
        attributeProcessor.removeEmptyAttributes(attributes);
        logger.trace("{} attributes retained after removing empty attributes.", attributes.size());
        attributeProcessor.sortAttributes(attributes);
        return attributes;
    }

    /**
     * Builds a profile request context.
     * 
     * @param principalName The principal name.
     * @param relyingPartyId The relying party id.
     * @param session The session.
     * @return Returns a profile request context.
     */
    private BaseSAMLProfileRequestContext<?, ?, ?, ?> buildRequestContext(final String principalName,
            final String relyingPartyId, final Session session) {
        logger.trace("[SAMLHelper] buildRequestContext");

        final RelyingPartyConfiguration relyingPartyConfiguration =
                relyingPartyConfigurationManager.getRelyingPartyConfiguration(relyingPartyId);
        final String providerId = relyingPartyConfiguration.getProviderId();

        EntityDescriptor localEntityMetadata = null;
        try {
            localEntityMetadata = metadataProvider.getEntityDescriptor(providerId);
        } catch (final MetadataProviderException e) {
            logger.error("Error while retrieving metadata descriptor for {}", providerId, e);
            throw new IllegalStateException(e);
        }

        EntityDescriptor peerEntityMetadata = null;
        try {
            peerEntityMetadata = metadataProvider.getEntityDescriptor(relyingPartyId);
        } catch (final MetadataProviderException e) {
            logger.error("Error while retrieving metadata descriptor for {}.", relyingPartyId, e);
            throw new IllegalStateException(e);
        }

        if (localEntityMetadata == null) {
            final String logMessage =
                    "No metadata available for Identity Provider. Please ensure that the Identity Provider ("
                            + providerId + ") has configured its own metadata";
            logger.error(logMessage);
            throw new IllegalStateException(logMessage);
        }

        if (peerEntityMetadata == null) {
            final String logMessage = "No metadata available for relying party " + relyingPartyId;
            logger.warn(logMessage);
        }

        @SuppressWarnings("rawtypes") final BaseSAMLProfileRequestContext<?, ?, ?, ?> requestCtx =
                new BaseSAMLProfileRequestContext();
        requestCtx.setRelyingPartyConfiguration(relyingPartyConfiguration);
        requestCtx.setInboundMessageIssuer(relyingPartyId);
        requestCtx.setOutboundMessageIssuer(providerId);
        requestCtx.setPrincipalName(principalName);
        requestCtx.setUserSession(session);
        requestCtx.setLocalEntityId(providerId);
        requestCtx.setPeerEntityId(relyingPartyId);
        requestCtx.setMetadataProvider(metadataProvider);
        requestCtx.setLocalEntityMetadata(localEntityMetadata);
        requestCtx.setLocalEntityRoleMetadata(localEntityMetadata.getIDPSSODescriptor(SAMLConstants.SAML20P_NS));
        requestCtx.setPeerEntityMetadata(peerEntityMetadata);
        requestCtx.setPeerEntityRoleMetadata(peerEntityMetadata.getSPSSODescriptor(SAMLConstants.SAML20P_NS));

        return requestCtx;
    }

    /**
     * Reads the relying party. Apparently the (optional?) AttributeConsumingService can specify descriptions and
     * required attribute information
     * 
     * 
     * @param relyingPartyId The relying party id.
     * @param locale The locale.
     * @return Returns a relying party.
     */
    public RelyingParty readRelyingParty(final String relyingPartyId, final Locale locale) {
        logger.trace("[SAMLHelper] readRelyingParty");
        EntityDescriptor entityDescriptor = null;
        try {
            entityDescriptor = metadataProvider.getEntityDescriptor(relyingPartyId);
            if (entityDescriptor == null) {
                logger.trace("No entity descriptor found for entityId {}.", relyingPartyId);
                return new RelyingParty(relyingPartyId);
            }
        } catch (final MetadataProviderException e) {
            logger.error("Error while retrieving metadata descriptor for {}.", relyingPartyId, e);
            throw new IllegalStateException(e);
        }
        final AttributeConsumingService attributeConsumingService = getAttributeConsumingService(entityDescriptor);
        if (attributeConsumingService == null) {
            logger.trace("No attribute consuming service found for entityId {}.", relyingPartyId);
            return new RelyingParty(relyingPartyId);
        } else {
            logger.trace("Relying party: {}, localized names {}, localized descriptions {}.", new Object[] {
                    relyingPartyId, attributeConsumingService.getNames(), attributeConsumingService.getDescriptions(),});

            String localizedName = null;
            for (final ServiceName element : attributeConsumingService.getNames()) {
                if (StringUtils.equals(element.getName().getLanguage(), locale.getLanguage())) {
                    localizedName = element.getName().getLocalString();
                    break;
                }
            }

            String localizedDescription = null;
            for (final ServiceDescription element : attributeConsumingService.getDescriptions()) {
                if (StringUtils.equals(element.getDescription().getLanguage(), locale.getLanguage())) {
                    localizedDescription = element.getDescription().getLocalString();
                    break;
                }
            }
            return new RelyingParty(relyingPartyId, localizedName, localizedDescription);
        }

    }

    /**
     * Gets the attribute consuming service.
     * 
     * @param entityDescriptor The entity descriptor.
     * @return Returns the attribute consuming service
     */
    private AttributeConsumingService getAttributeConsumingService(final EntityDescriptor entityDescriptor) {
        logger.trace("[SAMLHelper] getAttributeConsumingService");
        final String[] protocols = {SAMLConstants.SAML20P_NS, SAMLConstants.SAML11P_NS, SAMLConstants.SAML10P_NS};
        AttributeConsumingService result = null;
        List<AttributeConsumingService> list;
        for (final String protocol : protocols) {
            final SPSSODescriptor spSSODescriptor = entityDescriptor.getSPSSODescriptor(protocol);
            if (spSSODescriptor == null) {
                continue;
            }
            result = spSSODescriptor.getDefaultAttributeConsumingService();
            if (result != null) {
                logger.trace("DefaultAttributeConsumingService found.");
                return result;
            }
            list = spSSODescriptor.getAttributeConsumingServices();
            if (list != null && !list.isEmpty()) {
                logger.trace("AttributeConsumingService with index 0 found.");
                return list.get(0);
            }
        }
        return result;
    }

}
