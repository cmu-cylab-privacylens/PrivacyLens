/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2016 Carnegie Mellon University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the names of the copyright holders nor the
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

package edu.cmu.ece.privacylens.consent.logic;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

import edu.cmu.ece.privacylens.Consent;
import edu.cmu.ece.privacylens.IdPHelper;
import edu.cmu.ece.privacylens.Oracle;
import edu.cmu.ece.privacylens.consent.flow.ConsentFlowDescriptor;
import edu.cmu.ece.privacylens.context.AttributeReleaseContext;
import edu.cmu.ece.privacylens.context.ConsentContext;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.consent.logic.impl.AttributeValuesHashFunction;
import net.shibboleth.idp.consent.logic.impl.FlowDescriptorLookupFunction;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * Function that returns a map of consent objects representing consent to attribute release. Each consent object
 * represents consent to an attribute. The id of each consent object is an attribute id, and the value of each consent
 * object is a hash of the attribute's values. A consent object is created for every consentable attribute in the
 * attribute release context.
 */
public class AttributeReleaseConsentFunction implements Function<ProfileRequestContext, Map<String, Consent>> {

    /** Strategy used to find the {@link ConsentContext} from the {@link ProfileRequestContext}. */
    @Nonnull private Function<ProfileRequestContext, ConsentContext> consentContextLookupStrategy;

    /** Consent flow descriptor lookup strategy. */
    @Nonnull private Function<ProfileRequestContext, ConsentFlowDescriptor> consentFlowDescriptorLookupStrategy;

    /** Strategy used to find the {@link AttributeReleaseContext} from the {@link ProfileRequestContext}. */
    @Nonnull private Function<ProfileRequestContext, AttributeReleaseContext> attributeReleaseContextLookupStrategy;

    /** Function used to compute the hash of an attribute's values. */
    @Nonnull private Function<Collection<IdPAttributeValue<?>>, String> attributeValuesHashFunction;

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory
            .getLogger("AttributeReleaseConsentFunction");

    /** Constructor. */
    public AttributeReleaseConsentFunction() {
        consentContextLookupStrategy = new ChildContextLookup<>(ConsentContext.class, false);
        consentFlowDescriptorLookupStrategy =
                new FlowDescriptorLookupFunction<>(ConsentFlowDescriptor.class);
        attributeReleaseContextLookupStrategy = new ChildContextLookup<>(AttributeReleaseContext.class, false);
        attributeValuesHashFunction = new AttributeValuesHashFunction();
    }

    /**
     * Set the consent context lookup strategy.
     *
     * @param strategy the consent context lookup strategy
     */
    public void
            setConsentContextLookupStrategy(@Nonnull final Function<ProfileRequestContext, ConsentContext> strategy) {
        consentContextLookupStrategy = Constraint.isNotNull(strategy, "Consent context lookup strategy cannot be null");
    }

    /**
     * Set the consent flow descriptor lookup strategy.
     *
     * @param strategy the consent flow descriptor lookup strategy
     */
    public void setConsentFlowDescriptorLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, ConsentFlowDescriptor> strategy) {
        consentFlowDescriptorLookupStrategy =
                Constraint.isNotNull(strategy, "Consent flow descriptor lookup strategy cannot be null");
    }

    /**
     * Set the attribute release context lookup strategy.
     *
     * @param strategy the attribute release context lookup strategy
     */
    public void setAttributeReleaseContextLookupStrategy(
            @Nonnull final Function<ProfileRequestContext, AttributeReleaseContext> strategy) {
        attributeReleaseContextLookupStrategy =
                Constraint.isNotNull(strategy, "Attribute release context lookup strategy cannot be null");
    }

    /**
     * Set the function used to compute the hash of an attribute's values.
     *
     * @param function the function used to compute the hash of an attribute's values
     */
    public void setAttributeValuesHashFunction(
            @Nonnull final Function<Collection<IdPAttributeValue<?>>, String> function) {
        attributeValuesHashFunction = Constraint.isNotNull(function, "Hash function cannot be null");
    }

    /** {@inheritDoc} */
    @Override @Nullable public Map<String, Consent> apply(@Nullable final ProfileRequestContext input) {
        log.debug("XXXstroucki apply called");
        if (input == null) {
            return null;
        }

        log.debug("XXXstroucki here1");
        final ConsentFlowDescriptor consentFlowDescriptor = consentFlowDescriptorLookupStrategy.apply(input);
        if (consentFlowDescriptor == null) {
            return null;
        }

        log.debug("XXXstroucki here2");
        final ConsentContext consentContext = consentContextLookupStrategy.apply(input);
        if (consentContext == null) {
            return null;
        }

        log.debug("XXXstroucki here3");
        final AttributeReleaseContext attributeReleaseContext = attributeReleaseContextLookupStrategy.apply(input);
        if (attributeReleaseContext == null) {
            return null;
        }

        log.debug("XXXstroucki here4");
        final Map<String, Consent> currentConsents = new LinkedHashMap<>();

        final Map<String, IdPAttribute> consentableAttributes = attributeReleaseContext.getConsentableAttributes();
        final Oracle oracle = Oracle.getInstance();
        final String relyingPartyId = IdPHelper.getRelyingPartyId(input);

        // to force consent if attribute is required
        final Map<String, Boolean> requiredMap =
                oracle.getAttributeRequired(relyingPartyId);

        for (final IdPAttribute attribute : consentableAttributes.values()) {

            final Consent consent = new Consent();
            consent.setId(attribute.getId());

            if (consentFlowDescriptor.compareValues()) {
                consent.setValue(attributeValuesHashFunction.apply(attribute.getValues()));
            }

            // Remember previous choice.
            final Consent previousConsent = consentContext.getPreviousConsents().get(consent.getId());
            if (previousConsent != null) {
                if (consentFlowDescriptor.compareValues()) {
                    if (Objects.equals(consent.getValue(), previousConsent.getValue())) {
                        consent.setApproved(previousConsent.isApproved());
                    }
                } else {
                    consent.setApproved(previousConsent.isApproved());
                }
            }

            // Force consent if attribute is (now) required
            final boolean required = requiredMap.get(attribute.getId());
            if (required) {
                consent.setApproved(true);
            }

            currentConsents.put(consent.getId(), consent);
        }

        return currentConsents;
    }

}
