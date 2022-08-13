package swcnoops.server.datasource;

import swcnoops.server.model.FactionType;
import swcnoops.server.model.Squad;
import swcnoops.server.model.SquadNotification;
import swcnoops.server.session.PlayerSession;
import java.util.List;

public interface PlayerDataSource {
    Player loadPlayer(String playerId);
    void initOnStartup();
    void savePlayerName(String playerId, String playerName);

    PlayerSettings loadPlayerSettings(String playerId);

    void savePlayerSession(PlayerSession playerSession, SquadNotification squadNotification);

    void savePlayerSessions(PlayerSession playerSession, PlayerSession recipientPlayerSession, SquadNotification squadNotification);

    void newPlayer(String playerId, String secret);

    void newGuild(String playerId, GuildSettings squadResult);

    GuildSettings loadGuildSettings(String guildId);

    void editGuild(String guildId, String description, String icon, Integer minScoreAtEnrollment, boolean openEnrollment);

    List<Squad> getGuildList(FactionType faction);

    PlayerSecret getPlayerSecret(String primaryId);

    void saveNotification(String guildId, SquadNotification squadNotification);
}
