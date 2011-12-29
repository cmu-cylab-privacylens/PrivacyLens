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

package ch.SWITCH.aai.uApprove.tou.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.SWITCH.aai.uApprove.tou.ToUAcceptance;

/**
 * No operation implementation of the terms of use acceptance storage interface. I.e., this implementation will not
 * persist any data nor get any data.
 */
public class NOPStorage implements Storage {

    /** Class logger. */
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(NOPStorage.class);

    /** {@inheritDoc} */
    public void createToUAcceptance(final String userId, final ToUAcceptance touAcceptance) {
        return;
    }

    /** {@inheritDoc} */
    public ToUAcceptance readToUAcceptance(final String userId, final String version) {
        return null;
    }

    /** {@inheritDoc} */
    public void updateToUAcceptance(final String userId, final ToUAcceptance touAcceptance) {
        return;
    }

    /** {@inheritDoc} */
    public boolean containsToUAcceptance(final String userId, final String version) {
        return false;
    }

}
