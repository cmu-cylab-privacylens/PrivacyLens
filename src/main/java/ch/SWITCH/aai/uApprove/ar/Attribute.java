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

import java.util.Collections;
import java.util.List;

/**
 * Attribute.
 */
public class Attribute {

    /** The id. */
    private final String id;

    /** The name. */
    private final String name;

    /** The description. */
    private final String description;

    /** The values. */
    private final List<String> values;

    /**
     * Constructor.
     * 
     * @param id The id.
     * @param values The values.
     */
    public Attribute(final String id, final List<String> values) {
        this(id, null, null, values);
    }

    /**
     * Constructor.
     * 
     * @param id The id.
     * @param name The name.
     * @param description The description.
     * @param values The values.
     */
    public Attribute(final String id, final String name, final String description, final List<String> values) {
        this.id = id;
        if (name != null) {
            this.name = name;
        } else {
            this.name = id;
        }
        this.description = description;
        if (values != null) {
            this.values = values;
        } else {
            this.values = Collections.emptyList();
        }
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

    /**
     * Gets the values.
     * 
     * @return Returns the values.
     */
    public List<String> getValues() {
        return values;
    }

    /** {@inheritDoc} */
    public String toString() {
        return getId();
    }

}
