/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2015-2016, Carnegie Mellon University
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

package edu.cmu.ece.privacylens;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.ece.privacylens.ar.AttributeReleaseModule;
import net.shibboleth.idp.profile.context.RelyingPartyContext;
import net.shibboleth.idp.session.context.SessionContext;

/**
 * Provides helper functions to retrieve information from the IdP.
 */
public final class IdPHelper {

    /** Class logger. */
    private static final Logger log = LoggerFactory.getLogger(IdPHelper.class);

    /** using this for storage. should it be in IdPHelper? */
    private static AttributeReleaseModule attributeReleaseModule;

    private static final Object lock = new Object();

    /** Default constructor for utility classes is private. */
    private IdPHelper() {
    }

    public static final String getPrincipalName(
            final ProfileRequestContext profileRequestContext) {
        final SessionContext sessionCtx =
                profileRequestContext.getSubcontext(SessionContext.class);
        final String principalName =
                sessionCtx.getIdPSession().getPrincipalName();

        log.trace("Principal name is {}.", principalName);
        return principalName;
    }

    public static final String getRelyingPartyId(
            final ProfileRequestContext profileRequestContext) {

        final RelyingPartyContext rpc =
                profileRequestContext.getSubcontext(RelyingPartyContext.class,
                        true);
        final String relyingPartyId = rpc.getRelyingPartyId();

        log.trace("Relying party id is {}.", relyingPartyId);
        return relyingPartyId;
    }

    /**
     * Return the AttributeReleaseModule
     *
     * @return the AttributeReleaseModule.
     */
    public static final AttributeReleaseModule getAttributeReleaseModule() {
        if (attributeReleaseModule == null) {
            throw new InternalError("ARM not yet set");
        }
        return attributeReleaseModule;
    }

    /**
     * Set the AttributeReleaseModule
     *
     * @param attributeReleaseModule The attributeReleaseModule to set.
     */
    public static final void setAttributeReleaseModule(
            final AttributeReleaseModule attributeReleaseModule) {
        synchronized (lock) {
            if (IdPHelper.attributeReleaseModule != null) {
                throw new InternalError("ARM is already set");
            }
            IdPHelper.attributeReleaseModule = attributeReleaseModule;
        }
    }

}
