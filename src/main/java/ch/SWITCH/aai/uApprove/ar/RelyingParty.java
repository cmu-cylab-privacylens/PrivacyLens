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
