package swcnoops.server.session;

public interface GuildSession {
    String getGuildId();

    void join(PlayerSession playerSession);

    void troopsRequest(String playerId, String message, long time);
}
