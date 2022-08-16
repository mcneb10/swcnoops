CREATE TABLE IF NOT EXISTS Player
    (id text PRIMARY KEY,
     secret text,
     secondaryAccount text);

insert into player (id, secret) values ('2c2d4aea-7f38-11e5-a29f-069096004f69', '1118035f8f4160d5606e0c1a5e101ae5')
on conflict(id) do nothing;

CREATE TABLE IF NOT EXISTS PlayerSettings
    (id text PRIMARY KEY,
     faction text,
     name text,
     baseMap json,
     upgrades json,
     deployables json,
     contracts json,
     creature json,
     troops json,
     donatedTroops json,
     inventoryStorage json,
     campaigns json,
     preferences json,
     currentQuest text,
     guildId text,
     unlockedPlanets json);

insert into PlayerSettings (id, upgrades) values ('2c2d4aea-7f38-11e5-a29f-069096004f69', '{}')
on conflict(id) do nothing;

CREATE TABLE IF NOT EXISTS Squads
    (id text PRIMARY KEY,
     faction text,
     name text,
     perks json,
     members json,
     warId text,
     description text,
     icon text,
     openEnrollment NUMERIC,
     minScoreAtEnrollment NUMERIC);

CREATE TABLE IF NOT EXISTS SquadMembers
    (guildId text,
     playerId text,
     isOfficer NUMERIC,
     isOwner NUMERIC,
     joinDate NUMERIC,
     troopsDonated NUMERIC,
     troopsReceived NUMERIC,
     primary key(guildId, playerId));

CREATE TABLE IF NOT EXISTS "SquadNotifications" (
	"guildId"	TEXT NOT NULL,
	"id"	TEXT NOT NULL,
	"orderNo"	NUMERIC NOT NULL,
	"date"	NUMERIC NOT NULL,
	"playerId"	TEXT,
	"name"	TEXT,
	"squadMessageType"	TEXT,
	"message"	TEXT,
	"squadNotification"	json,
	PRIMARY KEY("id")
);

CREATE INDEX IF NOT EXISTS "SquadNotification_idx" ON "SquadNotifications" (
	"guildId"
);

