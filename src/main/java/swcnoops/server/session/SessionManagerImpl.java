package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.Player;
import swcnoops.server.datasource.PlayerDataSource;
import swcnoops.server.datasource.PlayerSettings;

import java.util.HashMap;
import java.util.Map;

public class SessionManagerImpl implements SessionManager {
    final private Map<String, PlayerSession> sessions = new HashMap<>();

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

        if (player == null) {
            // TODO - for now we create unknown players and default them some data
            //player = new Player(ServiceFactory.createRandomUUID());
            throw new RuntimeException("Unknown user id " + playerId);
        }

        PlayerSettings playerSettings = playerDataSource.loadPlayerSettings(playerId);
        player.setPlayerSettings(playerSettings);
        PlayerSession playerSession = new PlayerSessionImpl(player);
        return playerSession;
    }
}
