package swcnoops.server.datasource;

import swcnoops.server.game.PvpMatch;
import swcnoops.server.model.*;
import swcnoops.server.session.*;

import java.util.*;

public interface PlayerDataSource {
    Player loadPlayer(String playerId);

    PlayerSettings loadPlayerSettings(String playerId, boolean includeGuildId, String... fieldNames);

    Player loadPlayer(String playerId, boolean includeGuildId, String... fieldNames);

    void initOnStartup();

    void savePlayerName(PlayerSession playerSession, String playerName);

    void savePlayerSession(PlayerSession playerSession);

    void savePlayerLogin(PlayerSession playerSession);

    void saveTroopDonation(GuildSession guildSession, PlayerSession playerSession, PlayerSession recipientPlayerSession,
                           SquadNotification squadNotification);

    void newPlayer(String playerId, String secret, PlayerModel playerModel, Map<String, String> sharedPrefs, String name);

    void newGuild(PlayerSession playerSession, Squad squad);

    GuildSettings loadGuildSettings(String guildId);

    void editGuild(GuildSession guildSession, String guildId, String description, String icon, Integer minScoreAtEnrollment, boolean openEnrollment);

    List<Squad> getGuildList(FactionType faction);

    PlayerSecret getPlayerSecret(String primaryId);

    void saveNotification(GuildSession guildSession, SquadNotification squadNotification);

    void joinSquad(GuildSession guildSession, PlayerSession playerSession, SquadNotification squadNotification);

    void leaveSquad(GuildSession guildSession, PlayerSession playerSession, SquadNotification squadNotification);

    void changeSquadRole(GuildSession guildSession, PlayerSession invokerSession, PlayerSession memberSession, SquadNotification squadNotification, SquadRole squadRole);

    void joinRequest(GuildSession guildSession, PlayerSession playerSession, SquadNotification joinRequestNotification);

    void joinRejected(GuildSession guildSession, PlayerSession memberSession, SquadNotification joinRequestRejectedNotification);

    List<Squad> searchGuildByName(String searchTerm);

    boolean saveWarSignUp(FactionType faction, GuildSession guildId, List<String> participantIds, boolean isSameFactionWarAllowed, SquadNotification squadNotification, long time);

    boolean cancelWarSignUp(GuildSession guildSession, SquadNotification squadNotification);

    String matchMake(String guildId);

    War getWar(String warId);

    SquadMemberWarData loadPlayerWarData(String warId, String playerId);

    List<SquadMemberWarData> getWarParticipants(String guildId, String warId);

    void saveWarMap(PlayerSession playerSession, SquadMemberWarData squadMemberWarData);

    void saveWarTroopDonation(GuildSession guildSession, PlayerSession playerSession, SquadMemberWarData squadMemberWarData,
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

    PvpMatch getDevBaseMatches(PvpManager pvpManager, Set<String> devBasesSeen);

    Buildings getDevBaseMap(String id, FactionType faction);

    List<BattleLog> getPlayerBattleLogs(String playerId);
    BattleReplay getBattleReplay(String battleId);

    List<Member> loadSquadMembers(String guildId);

    War processWarEnd(String id, String squadIdA, String warId);

    void resetWarPartyForParticipants(String warId);

    List<WarHistory> loadWarHistory(String squadId);

    void newPlayerWithMissingSecret(String playerId, String secret, PlayerModel playerModel,
                                    Map<String, String> sharedPrefs, String name);

    void shutdown();

    void savePlayerKeepAlive(PlayerSession playerSession);

    void recoverWithPlayerSettings(PlayerSession playerSession, PlayerModel playerModel, Map<String, String> sharedPrefs);

    void saveDevBase(DevBase devBase);

    void savePvPBattleComplete(PlayerSession playerSession, PvpMatch pvpMatch, BattleReplay battleReplay);

    PvpMatch getPvPMatches(PvpManager pvpManager, Set<String> playersSeen);

    void pvpReleaseTarget(PvpManager pvpSession);

    void savePvPBattleStart(PlayerSession playerSession);

    PvpMatch getPvPRevengeMatch(PvpManager pvpManager, String opponentId, long time);

    void battleShare(GuildSessionImpl guildSession, PlayerSession playerSession, SquadNotification shareBattleNotification);

    Squad loadSquad(String guildId);

    TournamentLeaderBoard getTournamentLeaderBoard(String uid, String playerId);

    TournamentStat getTournamentPlayerRank(String uid, String playerId);
}
