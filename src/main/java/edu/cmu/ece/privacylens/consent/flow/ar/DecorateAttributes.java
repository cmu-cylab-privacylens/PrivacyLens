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

package edu.cmu.ece.privacylens.consent.flow.ar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import edu.cmu.ece.privacylens.AttributeUtils;
import edu.cmu.ece.privacylens.ar.Attribute;
import edu.cmu.ece.privacylens.context.AttributeContext;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.profile.context.ProfileInterceptorContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * Attribute consent action to populate the attribute with useful textual info,
 * with PrivacyLens' attribute type
 *
 * @event {@link org.opensaml.profile.action.EventIds#PROCEED_EVENT_ID}
 * @post See above.
 */
public class DecorateAttributes extends AbstractAttributeReleaseAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(DecorateAttributes.class);

    /** Default locale. */
    @Nonnull
    private final Locale locale = Locale.getDefault();

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
    }

    /** {@inheritDoc} */
    @Override protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final ProfileInterceptorContext interceptorContext) {

        final Map<String, IdPAttribute> consentableAttributes =
                getAttributeReleaseContext().getConsentableAttributes();

        final List<Attribute> outAttributes = new ArrayList<Attribute>();
        final Map<String, Boolean> attributeSettings =
                new HashMap<String, Boolean>();

        for (final IdPAttribute attribute : consentableAttributes.values()) {
            final String attributeId = attribute.getId();
            final String attributeName = AttributeUtils.getName(attributeId);
            final String attributeDescription =
                    AttributeUtils.getDescription(attributeId);
            final List<IdPAttributeValue<?>> attributeOriginalValues =
                    attribute.getValues();
            final List<String> attributeValues = new ArrayList<String>();
            for (final IdPAttributeValue<?> originalValue : attributeOriginalValues) {
                attributeValues.add(originalValue.getDisplayValue());
            }
            final Attribute outAttribute =
                    new Attribute(attributeId, attributeName,
                            attributeDescription, attributeValues);
            outAttributes.add(outAttribute);
            // CHANGEME get proper settings
            attributeSettings.put(attributeId, false);
        }

        final RequestContext requestContext =
                RequestContextHolder.getRequestContext();

        final MutableAttributeMap<Object> flowScope =
                requestContext.getFlowScope();

        // make available through flow and context
        flowScope.put("attributes", outAttributes);
        final AttributeContext attributeContext =
                new AttributeContext(outAttributes);
        // overwrite it, because we may have come from the consent interface
        getAttributeReleaseContext().addSubcontext(attributeContext, true);

        // CHANGEME get proper settings (previous consents?)
        flowScope.put("attributeSettings", attributeSettings);
        log.debug("{} Decorated attributes '{}'", getLogPrefix(),
 outAttributes);
    }

}
