package swcnoops.server.game;

public interface GameDataManager {
    void initOnStartup();

    /**
     * Troop data includes specialAttacks
     * @param uid
     * @return
     */
    TroopData getTroopDataByUid(String uid);

    BuildingData getBuildingDataByUid(String uid);

    TrapData getTrapDataByUid(String uid);
}
