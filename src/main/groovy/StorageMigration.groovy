/*
 * Copyright (c) 2011, SWITCH
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
def toSql = Sql.newInstance(properties['database.url'], properties['database.username'], properties['database.password'], properties['database.driver'])

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


println "Attribute Release Choice Migration"
def attributeReleaseChoice = toSql.dataSet("AttributeReleaseChoice");
fromSql.eachRow('SELECT au.auUserName, sp.spProviderName, ara.araAttributes, ara.araTimeStamp FROM ArpUser au, ShibProvider sp, AttrReleaseApproval ara WHERE ara.araIdxArpUser = au.IdxArpUser AND ara.araIdxShibProvider = sp.IdxShibProvider') {

    def userId = it['auUserName']
    def relyingPartyId = it['spProviderName']
    def attributes = it['araAttributes']
    def choiceDate = it['araTimeStamp']

    if (!relyingPartyId) {
        println "Migrating general Attribute Release Choice of ${userId}."
        try {
            attributeReleaseChoice.add(userId:userId, relyingPartyId:'*', attributeId:'*', valuesHash:"n/a", choiceDate: choiceDate);
        }catch (SQLIntegrityConstraintViolationException) {
            // Ignore, but continue
        }
    } else {
        attributes.split(":").each {
            if (it) {
                println "Migrating Attribute Release Choice of ${userId} to ${relyingPartyId} for ${it}."
                try {
                    attributeReleaseChoice.add(userId:userId, relyingPartyId:relyingPartyId, attributeId:it, valuesHash:"n/a", choiceDate: choiceDate);
                }catch (SQLIntegrityConstraintViolationException) {
                    // Ignore, but continue
                }
            }
        }
    }
}

