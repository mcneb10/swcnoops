package swcnoops.server.datasource;

import swcnoops.server.model.*;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;

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

    void saveNotification(String guildId, SquadNotification squadNotification);

    void saveGuildChange(GuildSettings guildSettings, PlayerSession playerSession, SquadNotification leaveNotification);

    void joinSquad(GuildSession guildSession, PlayerSession playerSession, SquadNotification squadNotification);

    void leaveSquad(GuildSession guildSession, PlayerSession playerSession, SquadNotification squadNotification);

    void changeSquadRole(GuildSession guildSession, PlayerSession memberSession, SquadNotification squadNotification, SquadRole squadRole);

    void joinRequest(GuildSession guildSession, PlayerSession playerSession, SquadNotification joinRequestNotification);

    void joinRejected(GuildSession guildSession, PlayerSession memberSession, SquadNotification joinRequestRejectedNotification);

    List<Squad> searchGuildByName(String searchTerm);

    void saveWarMatchMake(FactionType faction, String guildId, List<String> participantIds, SquadNotification squadNotification, Long time);

    void saveWarMatchCancel(String guildId, SquadNotification squadNotification);

    String matchMake(String guildId);

    War getWar(String warId);

    SquadMemberWarData loadPlayerWarData(String warId, String playerId);

    List<SquadMemberWarData> getWarParticipants(String guildId, String warId);
}
