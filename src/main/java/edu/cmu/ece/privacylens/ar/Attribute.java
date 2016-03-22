/*
 * Copyright (c) 2011, SWITCH
 * Copyright (c) 2013-2015, Carnegie Mellon University
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

package edu.cmu.ece.privacylens.ar;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attribute.
 */
public class Attribute {
    /** Class logger. */
    // if this is not static, Gson will barf on this.
    private static final Logger logger = LoggerFactory.getLogger(Attribute.class);

    /** The id. */
    private final String id;

    /**
     * The name. a short name
     */
    private final String name;

    /**
     * The description. a longer description.
     */
    private final String description;

    /** The values. */
    private final List<String> values;

    /**
     * Flag denoting that the attribute value is machine readable, and should not be presented to the user
     */
    private boolean machinereadable;

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
     * Long form constructor.
     *
     * @param id The id.
     * @param name The name.
     * @param description The description.
     * @param values The values.
     */
    public Attribute(final String id, final String name,
            final String description, final List<String> values) {
        logger.trace(
                "Attribute construction id: {} name: {} description: {} values: {}",
                new Object[] {id, name, description, values});
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
        //this.required = required;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        //result = prime * result + (required ? 1231 : 1237);
        result = prime * result + ((values == null) ? 0 : values.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Attribute)) {
            return false;
        }
        final Attribute other = (Attribute) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        /*
        if (required != other.required) {
            return false;
        }
        */
        if (values == null) {
            if (other.values != null) {
                return false;
            }
        } else if (!values.equals(other.values)) {
            return false;
        }
        return true;
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

    /**
     * @param b the attribute is machine readable and its value should not be presented
     */
    public void setMachineReadable(final boolean b) {
        machinereadable = b;
    }

    /**
     * @return whether the attribute is machine readable and its value should not be presented
     */
    public boolean isMachineReadable() {
        return machinereadable;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getId();
    }

}
