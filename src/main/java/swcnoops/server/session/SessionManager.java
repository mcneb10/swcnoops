package swcnoops.server.session;

import swcnoops.server.model.PlayerModel;

public interface SessionManager {
    PlayerSession getPlayerSession(String playerId);

    GuildSession getGuildSession(PlayerSession playerSession, String guildId);

    PlayerSession getPlayerSession(String playerId, PlayerModel defaultPlayerModel);

    void removePlayerSession(String playerId);

    GuildSession getGuildSession(String squadId);
}
