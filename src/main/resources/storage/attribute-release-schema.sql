CREATE TABLE AttributeReleaseConsent (
 userId         VARCHAR(104)                           NOT NULL,
 relyingPartyId VARCHAR(104)                           NOT NULL,
 attributeId    VARCHAR(104)                           NOT NULL,
 valuesHash     VARCHAR(256)                           NOT NULL,
 consentDate    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL,

 PRIMARY KEY (userId, relyingPartyId, attributeId)
)