package swcnoops.server.game;

import swcnoops.server.model.FactionType;
import swcnoops.server.session.inventory.Troops;

import java.util.List;
import java.util.Map;

public interface GameDataManager {
    void initOnStartup();

    /**
     * Troop data includes specialAttacks
     *
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
     *
     * @param unitId
     * @return
     */
    TroopData getLowestLevelTroopDataByUnitId(String unitId);

    TroopData getTroopDataByUnitId(String unitId, int level);

    BuildingData getBuildingDataByBuildingId(String buildingID, int level);

    CampaignMissionData getCampaignMissionData(String missionUid);

    CampaignSet getCampaignForFaction(FactionType faction);

    CampaignMissionSet getCampaignMissionSet(String campaignUid);

    List<TroopData> getLowestLevelTroopsForFaction(FactionType faction);

    Map<FactionType, List<TroopData>> getCreaturesByFaction();

    Map<Integer, List<TroopData>> getTroopSizeMap(FactionType faction);

    List<Integer> getTroopSizesAvailable(FactionType faction);

    int getMaxlevelForTroopUnitId(String unitId);

    GameConstants getGameConstants();

    BuildingData getFactionEquivalentOfBuilding(BuildingData oldBuildingData, FactionType faction);

    int getPvpMatchCost(int hQLevel);

    String randomDevBaseName();

    Map<String,Integer> remapTroopUidToUnitId(Map<String, Integer> troopUids);

    TroopData getTroopByUnitId(Troops troops, String unitId);

    void setPatchesAvailable(List<Patch> availablePatches);
    List<Patch> getPatchesAvailable();
    ConflictManager getConflictManager();

    int getTournamentAttackerMedals(int stars);

    int getTournamentDefenderMedals(int stars);
}
