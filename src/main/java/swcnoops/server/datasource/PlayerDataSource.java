package swcnoops.server.datasource;

import swcnoops.server.commands.player.PlayerPvpBattleComplete;
import swcnoops.server.game.PvpMatch;
import swcnoops.server.model.*;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.WarSession;
import swcnoops.server.session.WarSessionImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public interface PlayerDataSource {
    Player loadPlayer(String playerId);

    void initOnStartup();

    void savePlayerName(String playerId, String playerName);

    PlayerSettings loadPlayerSettings(String playerId);

    void savePlayerSession(PlayerSession playerSession, SquadNotification squadNotification);

    void savePlayerSessions(GuildSession guildSession, PlayerSession playerSession, PlayerSession recipientPlayerSession,
                            SquadNotification squadNotification);

    void newPlayer(String playerId, String secret);

    void newGuild(String playerId, GuildSettings squadResult);

    GuildSettings loadGuildSettings(String guildId);

    void editGuild(String guildId, String description, String icon, Integer minScoreAtEnrollment, boolean openEnrollment);

    List<Squad> getGuildList(FactionType faction);

    PlayerSecret getPlayerSecret(String primaryId);

    void saveNotification(GuildSession guildSession, SquadNotification squadNotification);

    void saveGuildChange(GuildSettings guildSettings, PlayerSession playerSession, SquadNotification leaveNotification);

    void joinSquad(GuildSession guildSession, PlayerSession playerSession, SquadNotification squadNotification);

    void leaveSquad(GuildSession guildSession, PlayerSession playerSession, SquadNotification squadNotification);

    void changeSquadRole(GuildSession guildSession, PlayerSession memberSession, SquadNotification squadNotification, SquadRole squadRole);

    void joinRequest(GuildSession guildSession, PlayerSession playerSession, SquadNotification joinRequestNotification);

    void joinRejected(GuildSession guildSession, PlayerSession memberSession, SquadNotification joinRequestRejectedNotification);

    List<Squad> searchGuildByName(String searchTerm);

    void saveWarMatchMake(FactionType faction, GuildSession guildId, List<String> participantIds, SquadNotification squadNotification, Long time);

    void saveWarMatchCancel(GuildSession guildSession, SquadNotification squadNotification);

    String matchMake(String guildId);

    War getWar(String warId);

    SquadMemberWarData loadPlayerWarData(String warId, String playerId);

    List<SquadMemberWarData> getWarParticipants(String guildId, String warId);

    void saveWarParticipant(SquadMemberWarData squadMemberWarData);

    void saveWarParticipant(GuildSession guildSession, PlayerSession playerSession, SquadMemberWarData squadMemberWarData,
                            SquadNotification squadNotification);

    AttackDetail warAttackStart(WarSession warSession, String playerId,
                                String opponentId, SquadNotification attackStartNotification, long time);

    void deleteWarForSquads(War war);

    void saveWar(War war);

    Collection<SquadNotification> getSquadNotificationsSince(String guildId, String guildName, long latestNotificationDate);

    WarNotification warPrepared(WarSessionImpl warSession, String warId, SquadNotification warPreparedNotification);

    AttackDetail warAttackComplete(WarSession warSession, PlayerSession playerId, BattleReplay playerBattleComplete,
                                   SquadNotification attackCompleteNotification, SquadNotification attackReplayNotification, DefendingWarParticipant defendingWarParticipant, long time);

    DefendingWarParticipant getDefendingWarParticipantByBattleId(String battleId);

    WarBattle getWarBattle(String battleId);


    HashMap<String, PvpMatch> getDevBaseMatches(PlayerSession playerSession);

    Buildings getDevBaseMap(String id, FactionType faction);

    void saveNewPvPBattle(PlayerPvpBattleComplete pvpBattle, PvpMatch match, BattleLog battleLog);

    List<BattleLog> getPlayerBattleLogs(String playerId);
    BattleType getBattleType(String battleId);

    BattleReplay pvpReplay(String battleId);


    List<Member> loadSquadMembers(String guildId);

    War processWarEnd(String id, String squadIdA, String warId);

    void resetWarPartyForParticipants(String warId);

    List<WarHistory> loadWarHistory(String squadId);

    void newPlayerWithMissingSecret(String playerId, String secret);

    void removeMissingSecret(String playerId);

}
