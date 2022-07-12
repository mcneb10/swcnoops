package swcnoops.server.session;

import swcnoops.server.model.PlayerModel;

public interface SessionManager {
    PlayerSession getPlayerSession(String playerId);

    GuildSession getGuildSession(String playerId, String guildId);

    PlayerSession getPlayerSession(String playerId, PlayerModel defaultPlayerModel);
}
