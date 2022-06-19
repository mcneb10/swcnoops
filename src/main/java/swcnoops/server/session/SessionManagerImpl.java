package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.Player;
import swcnoops.server.datasource.PlayerDataSource;

import java.util.HashMap;
import java.util.Map;

public class SessionManagerImpl implements SessionManager {
    private Map<String, PlayerSession> sessions = new HashMap<>();

    @Override
    public PlayerSession getPlayerSession(String playerId) {
        PlayerSession playerSession = getOrLoadPlayerSession(playerId);
        return playerSession;
    }

    private PlayerSession getOrLoadPlayerSession(String playerId) {
        PlayerSession playerSession;
        if (!this.sessions.containsKey(playerId)) {
            playerSession = loadPlayer(playerId);
            if (playerSession != null) {
                sessions.put(playerSession.getPlayerId(), playerSession);
            }
        }

        playerSession = this.sessions.get(playerId);

        return playerSession;
    }

    private PlayerSession loadPlayer(String playerId) {
        PlayerDataSource playerDataSource = ServiceFactory.instance().getPlayerDatasource();
        Player player = playerDataSource.loadPlayer(playerId);

        if (player == null)
            throw new RuntimeException("Unknown user id " + playerId);

        PlayerSession playerSession = new PlayerSessionImpl(player);
        return playerSession;
    }
}
