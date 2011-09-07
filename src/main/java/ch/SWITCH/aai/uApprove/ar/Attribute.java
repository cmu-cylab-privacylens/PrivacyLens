/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    protected String getName() {
        return name;
    }

    /**
     * Gets the description.
     * 
     * @return Returns the description.
     */
    protected String getDescription() {
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

}
