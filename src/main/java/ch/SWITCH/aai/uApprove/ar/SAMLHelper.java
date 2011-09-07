/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.SWITCH.aai.uApprove.ar;

import java.util.ArrayList;
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

import ch.SWITCH.aai.uApprove.UApproveException;
import edu.internet2.middleware.shibboleth.common.attribute.AttributeRequestException;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.SAML2AttributeAuthority;
import edu.internet2.middleware.shibboleth.common.attribute.provider.ScopedAttributeValue;
import edu.internet2.middleware.shibboleth.common.profile.provider.BaseSAMLProfileRequestContext;
import edu.internet2.middleware.shibboleth.common.relyingparty.RelyingPartyConfiguration;
import edu.internet2.middleware.shibboleth.common.relyingparty.provider.SAMLMDRelyingPartyConfigurationManager;
import edu.internet2.middleware.shibboleth.common.session.Session;

/**
 *
 */
public class SAMLHelper {

    /** Class logger. */
    private final Logger logger = LoggerFactory.getLogger(SAMLHelper.class);

    private SAML2AttributeAuthority attributeAuthority;

    private SAMLMDRelyingPartyConfigurationManager relyingPartyConfigurationManager;

    private MetadataProvider metadataProvider;

    private AttributeProcessor attributeProcessor;

    /**
     * @param attributeAuthority The attributeAuthority to set.
     */
    public void setAttributeAuthority(final SAML2AttributeAuthority attributeAuthority) {
        this.attributeAuthority = attributeAuthority;
    }

    /**
     * @param relyingPartyConfigurationManager The relyingPartyConfigurationManager to set.
     */
    public void setRelyingPartyConfigurationManager(
            final SAMLMDRelyingPartyConfigurationManager relyingPartyConfigurationManager) {
        this.relyingPartyConfigurationManager = relyingPartyConfigurationManager;
    }

    /**
     * @param attributeProcessor The attributeProcessor to set.
     */
    public void setAttributeProcessor(final AttributeProcessor attributeProcessor) {
        this.attributeProcessor = attributeProcessor;
    }

    public void initialize() {
        Validate.notNull(attributeAuthority, "Attribute Authority not set.");
        Validate.notNull(relyingPartyConfigurationManager, "Relying Party Configuration Manager not set.");
        metadataProvider = relyingPartyConfigurationManager.getMetadataProvider();
        Validate.notNull(metadataProvider, "Metadata Provider not set.");
        Validate.notNull(attributeProcessor, "Attribute Processor not set.");
    }

    public List<Attribute> getAttributes(final String principalName, final String relyingPartyId, final Locale locale,
            final Session session) {
        @SuppressWarnings("rawtypes") final BaseSAMLProfileRequestContext requestCtx =
                buildRequestContext(principalName, relyingPartyId, session);

        @SuppressWarnings("rawtypes") Map<String, BaseAttribute> baseAttributes = null;
        try {
            baseAttributes = attributeAuthority.getAttributes(requestCtx);
        } catch (final AttributeRequestException e) {
            throw new UApproveException("Error while retrieving attributes for " + principalName, e);
        }

        final List<Attribute> attributes = new ArrayList<Attribute>();
        for (final BaseAttribute<?> baseAttribute : baseAttributes.values()) {
            final List<String> attributeValues = new ArrayList<String>();
            for (final Object valueObj : baseAttribute.getValues()) {
                if (valueObj instanceof NameID) {
                    final NameID nameIdObject = (NameID) valueObj;
                    attributeValues.add(nameIdObject.getValue());
                } else if (valueObj instanceof ScopedAttributeValue) {
                    final ScopedAttributeValue scopedAttr = (ScopedAttributeValue) valueObj;
                    attributeValues.add(scopedAttr.getValue() + "@" + scopedAttr.getScope());
                } else {
                    attributeValues.add(String.valueOf(valueObj));
                }
            }
            logger.trace("Attribute: {} {}", baseAttribute.getId(), attributeValues);

            attributes.add(new Attribute(baseAttribute.getId(), baseAttribute.getDisplayNames().get(locale),
                    baseAttribute.getDisplayDescriptions().get(locale), attributeValues));
        }

        attributeProcessor.removeBlacklistedAttributes(attributes);
        attributeProcessor.sortAttributes(attributes);
        return attributes;
    }

    private BaseSAMLProfileRequestContext<?, ?, ?, ?> buildRequestContext(final String principalName,
            final String relyingPartyId, final Session session) {

        final RelyingPartyConfiguration relyingPartyConfiguration =
                relyingPartyConfigurationManager.getRelyingPartyConfiguration(relyingPartyId);
        final String providerId = relyingPartyConfiguration.getProviderId();

        EntityDescriptor localEntityMetadata = null;
        try {
            localEntityMetadata = metadataProvider.getEntityDescriptor(providerId);
        } catch (final MetadataProviderException e) {
            throw new UApproveException("Error while retrieving metadata descriptor for " + providerId, e);
        }

        EntityDescriptor peerEntityMetadata = null;
        try {
            peerEntityMetadata = metadataProvider.getEntityDescriptor(relyingPartyId);
        } catch (final MetadataProviderException e) {
            throw new UApproveException("Error while retrieving metadata descriptor for " + relyingPartyId, e);
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

    public RelyingParty getRelyingParty(final String relyingPartyId, final Locale locale) {
        EntityDescriptor entityDescriptor = null;
        try {
            entityDescriptor = metadataProvider.getEntityDescriptor(relyingPartyId);
        } catch (final MetadataProviderException e) {
            throw new UApproveException("Error while retrieving metadata descriptor for " + relyingPartyId, e);
        }
        final AttributeConsumingService attrService = getAttributeConsumingService(entityDescriptor);
        if (attrService == null) {
            logger.debug("No attribute consuming service found for entityId {}.", relyingPartyId);
            return new RelyingParty(relyingPartyId);
        } else {
            String localizedName = null;
            for (final ServiceName element : attrService.getNames()) {
                if (StringUtils.equals(element.getName().getLanguage(), locale.getLanguage())) {
                    localizedName = element.getName().getLocalString();
                    break;
                }
            }

            String localizedDescription = null;
            for (final ServiceDescription element : attrService.getDescriptions()) {
                if (StringUtils.equals(element.getDescription().getLanguage(), locale.getLanguage())) {
                    localizedDescription = element.getDescription().getLocalString();
                    break;
                }
            }
            return new RelyingParty(relyingPartyId, localizedName, localizedDescription);
        }

    }

    private AttributeConsumingService getAttributeConsumingService(final EntityDescriptor entityDescriptor) {
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
