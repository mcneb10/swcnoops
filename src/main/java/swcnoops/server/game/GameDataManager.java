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

    /**
     * Returns level1 data by the UnitID.
     * This is so if a player does not have the troop assigned, but the client thinks it should
     * then it should be using the first level of that unit.
     * @param unitId
     * @return
     */
    TroopData getLowestLevelTroopDataByUnitId(String unitId);

    TroopData getTroopDataByUnitId(String unitId, int level);

    BuildingData getBuildingDataByBuildingId(String buildingID, int level);
}
