package swcnoops.server.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuildSessionImpl implements GuildSession {
    final private String guildId;
    final private String guildName;
    final private Map<String,PlayerSession> guildPlayerSessions = new ConcurrentHashMap<>();

    public GuildSessionImpl(String guildId, String guildName) {
        this.guildId = guildId;
        this.guildName = guildName;
    }

    @Override
    public String getGuildId() {
        return guildId;
    }

    @Override
    public String getGuildName() {
        return guildName;
    }

    // TODO - needs proper handler to notify other players, or if the player is already in the squad
    @Override
    public void join(PlayerSession playerSession) {
        playerSession.setGuildSession(this);
        this.guildPlayerSessions.put(playerSession.getPlayerId(), playerSession);
    }

    @Override
    public void troopsRequest(String playerId, String message, long time) {
        // TODO
    }
}
