/*
 * Copyright (c) 2011, SWITCH
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

package ch.SWITCH.aai.uApprove.mf;

import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.opensaml.common.SAMLObject;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.metadata.AttributeConsumingService;
import org.opensaml.saml2.metadata.RequestedAttribute;
import org.opensaml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSBase64Binary;
import org.opensaml.xml.schema.XSBoolean;
import org.opensaml.xml.schema.XSDateTime;
import org.opensaml.xml.schema.XSInteger;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.XSURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.encoding.AttributeEncoder;
import edu.internet2.middleware.shibboleth.common.attribute.encoding.SAML1AttributeEncoder;
import edu.internet2.middleware.shibboleth.common.attribute.encoding.SAML2AttributeEncoder;
import edu.internet2.middleware.shibboleth.common.attribute.filtering.provider.FilterProcessingException;
import edu.internet2.middleware.shibboleth.common.attribute.filtering.provider.ShibbolethFilteringContext;
import edu.internet2.middleware.shibboleth.common.attribute.filtering.provider.match.basic.AbstractMatchFunctor;

/**
 * TODO.
 */
public class AttributeInMetadataMatchFunctor extends AbstractMatchFunctor {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AttributeInMetadataMatchFunctor.class);

    /** Whether optionally requested attributes should be matched. */
    private boolean onlyIfRequired;

    /**
     * Gets whether optionally requested attributes should be matched.
     * 
     * @return Whether optionally requested attributes should be matched.
     */
    public boolean isOnlyIfRequired() {
        return onlyIfRequired;
    }

    /**
     * Sets whether optionally requested attributes should be matched.
     * 
     * @param onlyIfRequired Whether optionally requested attributes should be matched.
     */
    public void setOnlyIfRequired(final boolean onlyIfRequired) {
        this.onlyIfRequired = onlyIfRequired;
    }

    /** {@inheritDoc} */
    protected boolean doEvaluatePolicyRequirement(final ShibbolethFilteringContext filterContext)
            throws FilterProcessingException {
        throw new FilterProcessingException("This match functor is not supported in policy requirements");
    }

    /** {@inheritDoc} */
    protected boolean doEvaluateValue(final ShibbolethFilteringContext filterContext, final String attributeId,
            final Object attributeValue) throws FilterProcessingException {

        // TODO: this is a fair bit of work to do for every value, we might consider exposing
        // the "active" AttributeConsumingService object on the attribute request context.
        // I guess the intent was that filterContext.getAttributeRequestContext().getRequestedAttributesIds()
        // would be used. We could do that, but it wouldn't support value filtering, and I don't know how
        // easy it is for the request context code to get access to the the full set of attributes and
        // encoders from the resolver prior to resolution.

        // Check for SP role.
        final RoleDescriptor role = filterContext.getAttributeRequestContext().getPeerEntityRoleMetadata();
        if (!(role instanceof SPSSODescriptor)) {
            log.debug("attribute requester's metadata role does not contain attribute requirements");
            return false;
        }

        // If the request is an AuthnRequest, check for an AttributeConsumingServiceIndex.
        AttributeConsumingService service = null;
        final SAMLObject inbound = filterContext.getAttributeRequestContext().getInboundSAMLMessage();
        if (inbound != null && inbound instanceof AuthnRequest) {
            final Integer i = ((AuthnRequest) inbound).getAttributeConsumingServiceIndex();
            if (i != null) {
                final List<AttributeConsumingService> services =
                        ((SPSSODescriptor) role).getAttributeConsumingServices();
                for (final AttributeConsumingService s : services) {
                    if (s.getIndex() == i) {
                        service = s;
                        break;
                    }
                }
                if (service == null) {
                    log.warn("incoming AuthnRequest's AttributeConsumingServiceIndex did not match the peer's metadata");
                    return false;
                }
            }
        }

        if (service == null) {
            service = ((SPSSODescriptor) role).getDefaultAttributeConsumingService();
        }
        if (service == null) {
            log.debug("the peer's metadata did not contain an AttributeConsumingService descriptor");
            return false;
        }

        log.debug("using AttributeConsumingService descriptor with index {}", service.getIndex());

        @SuppressWarnings("rawtypes") final BaseAttribute attr =
                filterContext.getUnfilteredAttributes().get(attributeId);
        if (attr != null && attr.getValues() != null) {
            @SuppressWarnings("rawtypes") final List encoders = attr.getEncoders();
            @SuppressWarnings("rawtypes") final Iterator i = encoders.iterator();
            while (i.hasNext()) {
                @SuppressWarnings("rawtypes") final AttributeEncoder encoder = (AttributeEncoder) i.next();
                final RequestedAttribute requested = findInMetadata(service, encoder);
                if (requested != null) {
                    if (onlyIfRequired && !requested.isRequired()) {
                        log.debug("attribute {} requested in metadata, but was not required", attributeId);
                        return false;
                    }
                    log.debug("found attribute {} requested in metadata", attributeId);
                    final List<XMLObject> vals = requested.getAttributeValues();
                    if (vals == null || vals.isEmpty()) {
                        return true;
                    }
                    final String v = attributeValue.toString();
                    for (final XMLObject xmlObj : vals) {
                        if (match(xmlObj, v)) {
                            return true;
                        }
                    }
                    log.debug("attribute {} found in metadata, but value not among those requested", attributeId);
                    return false;
                }
            }
            log.debug("attribute {} not found in metadata", attributeId);
        }

        return false;
    }

    /**
     * TODO.
     * 
     * @param service TODO.
     * @param encoder TODO.
     * @return TODO.
     */
    private RequestedAttribute findInMetadata(final AttributeConsumingService service,
            @SuppressWarnings("rawtypes") final AttributeEncoder encoder) {
        // I think it would be cleaner to expose a "protocol" property on the
        // encoder interface to match up against the outbound protocol than to
        // do a switch here based on casting/detecting the encoder type based
        // on the protocol string and ending up with a maintenance issue, so
        // for now, I'm just looking for any matching Attribute name.
        // Still have to cast because of where the name qualifier is.
        // We could add an abstracted qualifier accessor on the encoder base
        // interface, and implement it via getNamespace / getNameFormat also.
        final List<RequestedAttribute> requested = service.getRequestAttributes();
        for (final RequestedAttribute attr : requested) {
            if (attr.getName().equals(encoder.getAttributeName())) {
                String qualifier = null;
                if (encoder instanceof SAML2AttributeEncoder) {
                    qualifier = ((SAML2AttributeEncoder) encoder).getNameFormat();
                } else if (encoder instanceof SAML1AttributeEncoder) {
                    qualifier = ((SAML1AttributeEncoder) encoder).getNamespace();
                }
                final String nameFormat = attr.getNameFormat();
                if (qualifier == null || nameFormat == null || nameFormat.equals(Attribute.UNSPECIFIED)
                        || qualifier.equals(nameFormat)) {
                    return attr;
                }
            }
        }
        return null;
    }

    /**
     * TODO.
     * 
     * @param xmlObj TODO.
     * @param attributeValue TODO.
     * @return TODO
     */
    private boolean match(final XMLObject xmlObj, final String attributeValue) {
        // This is a substitute for a decoder layer that can generate
        // internal comparable value objects out of AttributeValue elements.
        // Short of that, some kind of pluggable comparison object with
        // knowledge of the XML syntax and the internal attribute objects
        // would be needed.
        String toMatch = null;
        if (xmlObj instanceof XSString) {
            toMatch = ((XSString) xmlObj).getValue();
        } else if (xmlObj instanceof XSURI) {
            toMatch = ((XSURI) xmlObj).getValue();
        } else if (xmlObj instanceof XSBoolean) {
            toMatch = ((XSBoolean) xmlObj).getValue().getValue() ? "1" : "0";
        } else if (xmlObj instanceof XSInteger) {
            toMatch = ((XSInteger) xmlObj).getValue().toString();
        } else if (xmlObj instanceof XSDateTime) {
            final DateTime dt = ((XSDateTime) xmlObj).getValue();
            if (dt != null) {
                toMatch = ((XSDateTime) xmlObj).getDateTimeFormatter().print(dt);
            }
        } else if (xmlObj instanceof XSBase64Binary) {
            toMatch = ((XSBase64Binary) xmlObj).getValue();
        } else if (xmlObj instanceof XSAny) {
            final XSAny wc = (XSAny) xmlObj;
            if (wc.getUnknownAttributes().isEmpty() && wc.getUnknownXMLObjects().isEmpty()) {
                toMatch = wc.getTextContent();
            }
        }
        if (toMatch != null) {
            return toMatch.equals(attributeValue);
        }
        log.warn("unrecognized XMLObject type, unable to match as a string to candidate value");
        return false;
    }
}
