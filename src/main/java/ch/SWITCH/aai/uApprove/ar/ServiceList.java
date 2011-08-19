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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 */
public class ServiceList {

    private List<String> services;

    private boolean isBlacklist;

    public ServiceList() {
        services = Collections.emptyList();
        isBlacklist = true;
    }

    /**
     * @param services The services to set.
     */
    public void setServices(final String list) {
        services = Arrays.asList(list.split("\\s+"));
    }

    /**
     * @param isBlacklist The isBlacklist to set.
     */
    public void setBlacklist(final boolean isBlacklist) {
        this.isBlacklist = isBlacklist;
    }

    public boolean skipRelyingParty(final String relyingPartyId) {
        boolean found = false;
        for (final String serviceRegEx : services) {
            final Pattern pattern = Pattern.compile(serviceRegEx);
            if (pattern.matcher(relyingPartyId).find()) {
                found = true;
                break;
            }
        }
        return isBlacklist ? found : !found;
    }

}
