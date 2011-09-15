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

package ch.SWITCH.aai.uApprove;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Relying Party List.
 */
public class RelyingPartyList extends ArrayList<String> {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Indicates whether the list is a black- or whitelist. */
    private boolean isBlacklist;

    /** Default constructor. */
    public RelyingPartyList() {
        super();
        isBlacklist = true;
    }

    /**
     * Sets the regular expressions.
     * 
     * @param expressions The regular expressions (whitespace delimited).
     */
    public void setRegularExpressions(final String expressions) {
        super.addAll(Util.stringToList(expressions));
    }

    /**
     * Sets whether the list should be interpreted as blacklist or whitelist.
     * 
     * @param isBlacklist The isBlacklist to set.
     */
    public void setBlacklist(final boolean isBlacklist) {
        this.isBlacklist = isBlacklist;
    }

    /** {@inheritDoc} */
    public boolean contains(final Object o) {
        boolean found = false;
        for (final String serviceRegEx : this) {
            final Pattern pattern = Pattern.compile(serviceRegEx);
            if (pattern.matcher(String.valueOf(o)).find()) {
                found = true;
                break;
            }
        }
        return isBlacklist ? !found : found;
    }

}
