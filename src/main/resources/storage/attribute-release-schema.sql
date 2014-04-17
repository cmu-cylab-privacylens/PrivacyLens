CREATE TABLE AttributeReleaseChoice (
 userId         VARCHAR(104)                           NOT NULL,
 relyingPartyId VARCHAR(104)                           NOT NULL,
 attributeId    VARCHAR(104)                           NOT NULL,
 valuesHash     VARCHAR(256)                           NOT NULL,
 choiceDate     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL,
 isConsented    BIT          DEFAULT 0                 NOT NULL,

 PRIMARY KEY (userId, relyingPartyId, attributeId, isConsented)
);

CREATE TABLE ForceShowInterface (
 userId         VARCHAR(104)                           NOT NULL,
 relyingPartyId VARCHAR(104)                           NOT NULL,
 isForceShow    BIT          DEFAULT 0                 NOT NULL,

 PRIMARY KEY (userId, relyingPartyId)
);

CREATE TABLE ReminderInterval (
 userId         VARCHAR(104)                           NOT NULL,
 relyingPartyId VARCHAR(104)                           NOT NULL,
 remindAfter      INTEGER          DEFAULT 0             NOT NULL,
 currentCount   INTEGER          DEFAULT 0             NOT NULL,

 PRIMARY KEY (userId, relyingPartyId, remindAfter)
);

CREATE TABLE LoginEvent (
 userId         VARCHAR(104)                           NOT NULL,
 serviceName    VARCHAR(104)                           NOT NULL,
 serviceUrl     VARCHAR(104)                           NOT NULL,
 eventDate      TIMESTAMP                              NOT NULL,
 eventDetailHash VARCHAR(256)                          NOT NULL,

 PRIMARY KEY (eventDetailHash)
);

CREATE UNIQUE INDEX loginEventTuple ON LoginEvent(userId, serviceName, eventDate);

CREATE TABLE LoginEventDetail (
 eventDetailHash VARCHAR(256)                          NOT NULL,
 eventDetailData BLOB                                  NOT NULL,

 PRIMARY KEY (eventDetailHash) 
);
