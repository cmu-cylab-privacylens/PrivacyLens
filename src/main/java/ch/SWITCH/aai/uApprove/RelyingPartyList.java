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

package ch.SWITCH.aai.uApprove;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Relying Party List.
 */
public class RelyingPartyList extends ArrayList<String> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private boolean isBlacklist;

    public RelyingPartyList() {
        super();
        isBlacklist = true;
    }

    /**
     * @param expressions
     */
    public void setRegularExpressions(final String expressions) {
        super.addAll(Util.stringToList(expressions));
    }

    /**
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
