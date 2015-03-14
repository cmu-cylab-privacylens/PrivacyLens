/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2013-2015 Carnegie Mellon University
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

package edu.cmu.ece.PrivacyLens.ar;

import org.joda.time.DateTime;

import edu.cmu.ece.PrivacyLens.Util;

/** Represents a login event. */
public class LoginEvent {

    /** login event timestamp */
    private final DateTime eventDate;

    /** user id */
    private final String userId;

    /** service name */
    private final String serviceName;

    /** service url (service provider id) */
    private final String serviceUrl;

    /** event detail hash */
    private final String eventDetailHash;

    /** generate event detail hash. work around java limitation */
    private static String genHash(final String userId, final String relyingPartyId, final DateTime eventDate) {
        return Util.hash(userId + "+" + relyingPartyId + "+" + eventDate);
    }

    /**
     * Constructs a @see LoginEvent.
     *
     * @param userId The user id.
     * @param serviceName The service name.
     * @param serviceUrl The service url.
     * @param eventDate The timestamp for this @see LoginEvent.
     */
    public LoginEvent(final String userId, final String serviceName, final String serviceUrl, final DateTime eventDate) {
        // I would like to have put the hash in a string here to make the call to the full
        // constructor look nicer, but java doesn't like that.
        this(userId, serviceName, serviceUrl, eventDate, genHash(userId, serviceUrl, eventDate));
    }

    /**
     * Constructs a @see LoginEvent.
     *
     * @param userId The user id.
     * @param serviceName The service name.
     * @param serviceUrl The relying party id.
     * @param eventDate The timestamp for this @see LoginEvent.
     * @param eventDetailHash The event detail hash for this
     */
    public LoginEvent(final String userId, final String serviceName, final String serviceUrl, final DateTime eventDate,
            final String eventDetailHash) {

        if (userId == null || serviceUrl == null || eventDate == null || eventDetailHash == null) {
            throw new IllegalArgumentException("Invalid arguments to LoginEvent constructor");
        }
        this.userId = userId;
        this.serviceName = serviceName;
        this.serviceUrl = serviceUrl;
        this.eventDate = eventDate;
        this.eventDetailHash = eventDetailHash;
    }

    /**
     * Generate fake LoginEvent
     *
     * @return Returns a fake login event (without linkage to other objects)
     */
    public LoginEvent genFakeLoginEvent() {
        return new LoginEvent("userId", "serviceName", "serviceUrl", new DateTime(), "fake");
    }

    /**
     * Gets the user id.
     *
     * @return Returns the user id.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Gets the service name.
     *
     * @return Returns the service name.
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Gets the service url.
     *
     * @return Returns the service url.
     */
    public String getServiceUrl() {
        return serviceUrl;
    }

    /**
     * Gets the timestamp when the login event happened.
     *
     * @return Returns the date.
     */
    public DateTime getDate() {
        return eventDate;
    }

    /**
     * Gets the login event detail hash.
     *
     * @return Returns the event detail hash.
     */
    public String getEventDetailHash() {
        return eventDetailHash;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "LoginEvent [eventDate=" + eventDate + ", userId=" + userId + ", relyingPartyId=" + serviceUrl
                + ", eventDetailHash=" + eventDetailHash + "]";
    }

    /** {@inheritDoc} */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((eventDate == null) ? 0 : eventDate.hashCode());
        result = prime * result + ((eventDetailHash == null) ? 0 : eventDetailHash.hashCode());
        result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
        result = prime * result + ((serviceUrl == null) ? 0 : serviceUrl.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LoginEvent)) {
            return false;
        }
        final LoginEvent other = (LoginEvent) obj;
        if (eventDate == null) {
            if (other.eventDate != null) {
                return false;
            }
        } else if (!eventDate.equals(other.eventDate)) {
            return false;
        }
        if (eventDetailHash == null) {
            if (other.eventDetailHash != null) {
                return false;
            }
        } else if (!eventDetailHash.equals(other.eventDetailHash)) {
            return false;
        }
        if (serviceName == null) {
            if (other.serviceName != null) {
                return false;
            }
        } else if (!serviceName.equals(other.serviceName)) {
            return false;
        }
        if (serviceUrl == null) {
            if (other.serviceUrl != null) {
                return false;
            }
        } else if (!serviceUrl.equals(other.serviceUrl)) {
            return false;
        }
        if (userId == null) {
            if (other.userId != null) {
                return false;
            }
        } else if (!userId.equals(other.userId)) {
            return false;
        }
        return true;
    }

}
