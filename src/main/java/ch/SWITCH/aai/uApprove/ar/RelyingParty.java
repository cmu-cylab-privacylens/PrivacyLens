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
 *
 */
public class RelyingParty {

    private final String id;

    private final String localizedName;

    private final String localizedDescription;

    /**
     * Constructor.
     * 
     * @param id
     */
    public RelyingParty(final String id) {
        this(id, null, null);
    }

    /**
     * Constructor.
     * 
     * @param id
     * @param localizedName
     * @param localizedDescription
     */
    public RelyingParty(final String id, final String localizedName, final String localizedDescription) {
        this.id = id;
        if (localizedName != null) {
            this.localizedName = localizedName;
        } else {
            this.localizedName = AttributeReleaseHelper.resolveFqdn(id);
        }
        this.localizedDescription = localizedDescription;
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    /**
     * @return Returns the localizedName.
     */
    public String getLocalizedName() {
        return localizedName;
    }

    /**
     * @return Returns the localizedDescription.
     */
    public String getLocalizedDescription() {
        return localizedDescription;
    }

}
