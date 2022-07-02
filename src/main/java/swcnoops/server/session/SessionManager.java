package swcnoops.server.session;

import swcnoops.server.model.PlayerModel;

public interface SessionManager {
    PlayerSession getPlayerSession(String playerId);

    GuildSession getGuildSession(String guildId, String guildName);

    PlayerSession getPlayerSession(String playerId, PlayerModel defaultPlayerModel);
}
