package swcnoops.server.game;

public interface GameDataManager {
    void initOnStartup();

    TroopData getTroopDataByUid(String uid);

    BuildingData getBuildingDataByUid(String uid);

    TrapData getTrapDataByUid(String uid);
}
