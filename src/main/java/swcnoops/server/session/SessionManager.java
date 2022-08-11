package swcnoops.server.session;

import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.model.PlayerModel;

public interface SessionManager {
    PlayerSession getPlayerSession(String playerId);

    GuildSession getGuildSession(PlayerSettings playerSettings, String guildId);

    PlayerSession getPlayerSession(String playerId, PlayerModel defaultPlayerModel);

    void removePlayerSession(String playerId);
}
