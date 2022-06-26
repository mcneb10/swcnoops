package swcnoops.server.datasource;

import swcnoops.server.session.PlayerSessionImpl;

public interface PlayerDataSource {
    Player loadPlayer(String playerId);
    void initOnStartup();
    void savePlayerName(String playerId, String playerName);

    PlayerSettings loadPlayerSettings(String playerId);

    void savePlayerSession(PlayerSessionImpl playerSession);
}
