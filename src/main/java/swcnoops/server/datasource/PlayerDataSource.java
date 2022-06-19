package swcnoops.server.datasource;

public interface PlayerDataSource {
    Player loadPlayer(String playerId);

    void initOnStartup();
}
