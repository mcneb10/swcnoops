package swcnoops.server.game;

import swcnoops.server.model.FactionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a map to classify and group buildings to help converting a building in one faction to another.
 * Only really used during setFaction call.
 */
public class FactionBuildingEquivalentMap {
    private Map<String, Map<FactionType, List<BuildingData>>> equivalentMap = new HashMap<>();

    protected void addMap(List<BuildingData> buildingLevels) {
        BuildingData buildingData = buildingLevels.get(0);

        if (buildingData.getStoreTab() == StoreTab.not_in_store &&
                buildingData.getSubType() == BuildingSubType.rapid_fire_turret)
            return;

        String buildingKey = createKey(buildingData);

        Map<FactionType, List<BuildingData>> factionMap = this.equivalentMap.get(buildingKey);
        if (factionMap == null) {
            factionMap = new HashMap<>();
            this.equivalentMap.put(buildingKey, factionMap);
        }

        factionMap.put(buildingData.getFaction(), buildingLevels);
    }

    private String createKey(BuildingData buildingData) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(buildingData.getType());
        stringBuffer.append("-");
        stringBuffer.append(buildingData.getCurrency());
        stringBuffer.append("-");
        stringBuffer.append(buildingData.getSubType());
        return stringBuffer.toString();
    }

    public BuildingData getEquivalentBuilding(BuildingData oldBuildingData, FactionType targetFaction) {
        BuildingData equivBuildingDataInFaction = null;
        String buildingKey = createKey(oldBuildingData);
        Map<FactionType, List<BuildingData>> equivBuildings = this.equivalentMap.get(buildingKey);

        if (equivBuildings != null) {
            List<BuildingData> factionBuildings = equivBuildings.get(targetFaction);
            if (factionBuildings != null) {
                equivBuildingDataInFaction = factionBuildings.get(oldBuildingData.getLevel() - 1);
            }
        }

        return equivBuildingDataInFaction;
    }
}
