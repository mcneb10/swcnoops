package swcnoops.server.session;

import java.util.Map;

public interface GuildSession {
    String getGuildId();

    void join(PlayerSession playerSession);

    void troopsRequest(String playerId, String message, long time);

    String getGuildName();

    void processDonations(Map<String, Integer> troopsDonated, String requestId, PlayerSession playerSession, String recipientId, long time);
}
