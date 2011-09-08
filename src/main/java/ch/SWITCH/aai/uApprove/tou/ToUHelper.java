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

package ch.SWITCH.aai.uApprove.tou;

import org.apache.commons.lang.StringUtils;

import ch.SWITCH.aai.uApprove.Util;

/**
 * ToU Helper.
 */
public final class ToUHelper {

    /** Default constructor for utility classes is private. */
    private ToUHelper() {
    }

    /**
     * Determines if the ToU are accepted.
     * 
     * @param tou The ToU.
     * @param touAcceptance The ToU acceptance.
     * @param compareContent Whether content is compared or not.
     * @return Returns true if ToU are accepted.
     */
    public static boolean acceptedToU(final ToU tou, final ToUAcceptance touAcceptance, final boolean compareContent) {
        if (touAcceptance == null) {
            return false;
        }

        if (StringUtils.equals(tou.getVersion(), touAcceptance.getVersion())) {
            if (compareContent) {
                return StringUtils.equals(Util.hash(tou.getContent()), touAcceptance.getFingerprint());
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
