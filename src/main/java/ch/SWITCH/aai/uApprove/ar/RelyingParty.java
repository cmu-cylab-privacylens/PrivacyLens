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

package ch.SWITCH.aai.uApprove.ar;

/**
 * Relying Party.
 */
public class RelyingParty {

    /** The relying party id. */
    private final String id;

    /** The name of the relying party. */
    private final String name;

    /** The description of the relying party. */
    private final String description;

    /**
     * Constructor.
     * 
     * @param id The id.
     */
    public RelyingParty(final String id) {
        this(id, null, null);
    }

    /**
     * Constructor.
     * 
     * @param id The id.
     * @param name The name.
     * @param description The description.
     */
    public RelyingParty(final String id, final String name, final String description) {
        this.id = id;
        if (name != null) {
            this.name = name;
        } else {
            this.name = AttributeReleaseHelper.resolveFqdn(id);
        }
        this.description = description;
    }

    /**
     * Gets the id.
     * 
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the name.
     * 
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the description.
     * 
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

}
