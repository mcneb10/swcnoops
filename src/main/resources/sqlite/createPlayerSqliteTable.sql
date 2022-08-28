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
     unlockedPlanets json,
     hqLevel NUMERIC,
     warMap	json);

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
     minScoreAtEnrollment NUMERIC,
     warSignUpTime NUMERIC);

CREATE TABLE IF NOT EXISTS SquadMembers
    (guildId text,
     playerId text,
     isOfficer NUMERIC,
     isOwner NUMERIC,
     joinDate NUMERIC,
     troopsDonated NUMERIC,
     troopsReceived NUMERIC,
     warParty NUMERIC,
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
	PRIMARY KEY("id", "guildId")
);

CREATE INDEX IF NOT EXISTS "SquadNotification_idx" ON "SquadNotifications" (
	"guildId"
);

CREATE INDEX IF NOT EXISTS "squadNotification_date_idx" ON "SquadNotifications" (
	"guildId",
	"date"
);

CREATE TABLE IF NOT EXISTS "War" (
	"warId"	TEXT,
	"squadIdA"	TEXT,
	"squadIdB"	TEXT,
	"prepGraceStartTime"	NUMERIC,
	"prepEndTime"	NUMERIC,
	"actionGraceStartTime"	NUMERIC,
	"actionEndTime"	NUMERIC,
	"cooldownEndTime"	NUMERIC,
	PRIMARY KEY("warId")
);

CREATE INDEX IF NOT EXISTS "War_Squad1_idx" ON "War" (
	"squadIdA"
);

CREATE INDEX IF NOT EXISTS "War_Squad2_idx" ON "War" (
	"squadIdB"
);

CREATE TABLE IF NOT EXISTS "MatchMake" (
	"guildId"	TEXT,
	"warSignUpTime"	NUMERIC,
	"faction"	NUMERIC,
	participants JSON,
	PRIMARY KEY("guildId")
);

CREATE TABLE IF NOT EXISTS "WarParticipants" (
	"playerId"	TEXT,
	"warId"	TEXT,
	"warMap"	json,
	"donatedTroops"	json,
	"turns"	INTEGER,
	"attacksWon"	INTEGER,
	"defensesWon"	INTEGER,
	"score"	INTEGER,
	"victoryPoints"	INTEGER,
	attackExpirationDate NUMERIC,
	attackBattleId TEXT,
	defenseExpirationDate NUMERIC,
	defenseBattleId TEXT,
	"defenseRemaining"	INTEGER,
	PRIMARY KEY("playerId","warId")
);

CREATE INDEX IF NOT EXISTS "warDefenseBattleId_idx" ON "WarParticipants" (
	"defenseBattleId"
);

CREATE INDEX IF NOT EXISTS "warAttackBattleId_idx" ON "WarParticipants" (
	"attackBattleId"
);

CREATE TABLE IF NOT EXISTS "WarBattles" (
	"warId"	TEXT,
	"battleId"	TEXT,
	"attackerId"	TEXT,
	"defenderId"	TEXT,
	battleResponse  JSON,
	attackExpirationDate NUMERIC,
	attackResponseDate NUMERIC,
	attackerScore NUMERIC,
	PRIMARY KEY("battleId")
);

