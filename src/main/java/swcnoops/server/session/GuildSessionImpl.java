package swcnoops.server.session;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GuildSessionImpl implements GuildSession {
    final private String guildId;
    final private Map<String,PlayerSession> guildPlayerSessions = new ConcurrentHashMap<>();

    public GuildSessionImpl(String guildId) {
        this.guildId = guildId;
    }

    @Override
    public String getGuildId() {
        return guildId;
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
