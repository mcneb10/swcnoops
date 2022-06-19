package swcnoops.server.session;

public interface SessionManager {
    PlayerSession getPlayerSession(String playerId);
}
