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
 *
 */
public class Attribute {

    private final String id;

    private final String localizedName;

    private final String localizedDescription;

    private final List<String> values;

    /**
     * Constructor.
     * 
     * @param id
     * @param values
     */
    public Attribute(final String id, final List<String> values) {
        this(id, null, null, values);
    }

    /**
     * Constructor.
     * 
     * @param id
     * @param localizedName
     * @param localizedDescription
     * @param values
     */
    public Attribute(final String id, final String localizedName, final String localizedDescription,
            final List<String> values) {
        this.id = id;
        if (localizedName != null) {
            this.localizedName = localizedName;
        } else {
            this.localizedName = id;
        }
        this.localizedDescription = localizedDescription;
        if (values != null) {
            this.values = values;
        } else {
            this.values = Collections.emptyList();
        }
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

    /**
     * @return Returns the values.
     */
    public List<String> getValues() {
        return values;
    }

}
