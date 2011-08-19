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
import java.util.Locale;
import java.util.Map;

/**
 *
 */
public class RelyingParty {

    private final String id;

    private final Map<Locale, String> localizedNames;

    private final Map<Locale, String> localizedDescriptions;

    /**
     * Constructor.
     * 
     * @param id
     * @param localizedNames
     * @param localizedDescriptions
     */
    public RelyingParty(final String id, final Map<Locale, String> localizedNames,
            final Map<Locale, String> localizedDescriptions) {
        this.id = id;
        if (localizedNames != null) {
            this.localizedNames = localizedNames;
        } else {
            this.localizedNames = Collections.emptyMap();
        }

        if (localizedDescriptions != null) {
            this.localizedDescriptions = localizedDescriptions;
        } else {
            this.localizedDescriptions = Collections.emptyMap();
        }
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    /**
     * @param locale
     * @return Returns the localized name.
     */
    public String getLocalizedName(final Locale locale) {
        return localizedNames.get(locale);
    }

    /**
     * @param locale
     * @return Returns the localized description.
     */
    public String getLocalizedDescription(final Locale locale) {
        return localizedDescriptions.get(locale);
    }
}
