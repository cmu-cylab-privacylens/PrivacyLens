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

package ch.SWITCH.aai.uApprove.tou;

import org.apache.commons.lang.StringUtils;

import ch.SWITCH.aai.uApprove.Util;

/**
 * ToU Helper.
 */
public final class ToUHelper {

    /** Default constructor for utility classes is private. */
    private ToUHelper() {
    }

    /**
     * Determines if the ToU are accepted.
     * 
     * @param tou The ToU.
     * @param touAcceptance The ToU acceptance.
     * @param compareContent Whether content is compared or not.
     * @return Returns true if ToU are accepted.
     */
    public static boolean acceptedToU(final ToU tou, final ToUAcceptance touAcceptance, final boolean compareContent) {
        if (touAcceptance == null) {
            return false;
        }

        if (StringUtils.equals(tou.getVersion(), touAcceptance.getVersion())) {
            if (compareContent) {
                return StringUtils.equals(Util.hash(tou.getContent()), touAcceptance.getFingerprint());
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
