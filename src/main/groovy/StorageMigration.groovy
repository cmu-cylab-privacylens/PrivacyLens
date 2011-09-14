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

import groovy.sql.Sql

assert this.args.size() == 2

def properties = new Properties()
new File(this.args[0]).withInputStream { stream ->
    properties.load(stream)
}

new File(this.args[1]).withInputStream { stream ->
    properties.load(stream)
}

def fromSql = Sql.newInstance(properties['url'], properties['user'], properties['password'], properties['driver'])
def toSql = Sql.newInstance(properties['database.url'], properties['database.user'], properties['database.password'], properties['database.driver'])

println "Terms Of Use Migration"

def touAcceptance = toSql.dataSet("ToUAcceptance");
fromSql.eachRow('SELECT auUserName, auLastTermsVersion, auFirstAccess FROM ArpUser') {
    def userId = it['auUserName']
    def version = it['auLastTermsVersion']
    def fingerprint = 'n/a'
    def acceptanceDate = it['auFirstAccess']
    println "Migrating ToU Acceptance of ${userId} for version ${version}."
    try {
        touAcceptance.add(userId:userId, version:version, fingerprint:fingerprint, acceptanceDate:acceptanceDate)
    } catch (SQLIntegrityConstraintViolationException) {
        // Ignore, but continue
    }
}


println "Attribute Release Consent Migration"
def attributeReleaseConsent = toSql.dataSet("AttributeReleaseConsent");
fromSql.eachRow('SELECT au.auUserName, sp.spProviderName, ara.araAttributes, ara.araTimeStamp FROM ArpUser au, ShibProvider sp, AttrReleaseApproval ara WHERE ara.araIdxArpUser = au.IdxArpUser AND ara.araIdxShibProvider = sp.IdxShibProvider') {

    def userId = it['auUserName']
    def relyingPartyId = it['spProviderName']
    def attributes = it['araAttributes']
    def consentDate = it['araTimeStamp']

    if (!relyingPartyId) {
        println "Migrating general Attribute Release Consent of ${userId}."
        try {
            attributeReleaseConsent.add(userId:userId, relyingPartyId:'*', attributeId:'*', valuesHash:"n/a", consentDate: consentDate);
        }catch (SQLIntegrityConstraintViolationException) {
            // Ignore, but continue
        }
    } else {
        attributes.split(":").each {
            if (it) {
                println "Migrating Attribute Release Consent of ${userId} to ${relyingPartyId} for ${it}."
                try {
                    attributeReleaseConsent.add(userId:userId, relyingPartyId:relyingPartyId, attributeId:it, valuesHash:"n/a", consentDate: consentDate);
                }catch (SQLIntegrityConstraintViolationException) {
                    // Ignore, but continue
                }
            }
        }
    }
}

