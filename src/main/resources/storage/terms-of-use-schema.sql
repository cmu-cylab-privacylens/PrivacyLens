CREATE TABLE ToUAcceptance (
 userId         VARCHAR(104)                           NOT NULL,
 version        VARCHAR(104)                           NOT NULL,
 fingerprint    VARCHAR(256)                           NOT NULL,
 acceptanceDate TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
 PRIMARY KEY (userId, version)
)