package swcnoops.server.game;

import swcnoops.server.ServiceFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameDataManagerImpl implements GameDataManager {
    private Map<String, TroopData> troops = new HashMap<>();
    private Map<String, BuildingData> buildings = new HashMap<>();

    @Override
    public void initOnStartup() {
        try {
            this.troops = loadTroops();
            this.buildings = loadBuildings();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load game data from patches", ex);
        }
    }

    @Override
    public TroopData getTroopDataByUid(String uid) {
        return troops.get(uid);
    }

    private Map<String, TroopData> loadTroops() throws Exception {
        Map<String, TroopData> map = new HashMap<>();

        Map result = ServiceFactory.instance().getJsonParser()
                .toObjectFromResource(ServiceFactory.instance().getConfig().troopJson, Map.class);
        Map<String, Map> jsonSpreadSheet = (Map<String, Map>) result;
        Map<String, Map> contentMap = jsonSpreadSheet.get("content");
        Map<String, Map> objectsMap = contentMap.get("objects");
        List<Map<String,String>> troopDataMap = (List<Map<String,String>>) objectsMap.get("TroopData");

        for (Map<String,String> troop : troopDataMap) {
            String faction = troop.get("faction");
            int lvl = Integer.valueOf(troop.get("lvl"));
            String uid = troop.get("uid");              //troopEmpireChicken1
            String unitId = troop.get("unitID");        //EmpireChicken
            String type = troop.get("type");
            int size = Integer.valueOf(troop.get("size")).intValue();
            int trainingTime = Integer.valueOf(troop.get("trainingTime")).intValue();
            String upgradeShardUid = troop.get("upgradeShardUid");
            int upgradeShards = Integer.valueOf(troop.get("upgradeShards") == null ? "0" : troop.get("upgradeShards")).intValue();

            TroopData troopData = new TroopData(uid);
            troopData.setFaction(faction);
            troopData.setLevel(lvl);
            troopData.setUnitId(unitId);
            troopData.setType(type);
            troopData.setSize(size);
            troopData.setTrainingTime(trainingTime);
            troopData.setUpgradeShardUid(upgradeShardUid);
            troopData.setUpgradeShards(upgradeShards);

            map.put(troopData.getUid(), troopData);
        }

        return map;
    }

    private Map<String, BuildingData> loadBuildings() throws Exception {
        Map<String, BuildingData> map = new HashMap<>();

        Map result = ServiceFactory.instance().getJsonParser()
                .toObjectFromResource(ServiceFactory.instance().getConfig().baseJson, Map.class);
        Map<String, Map> jsonSpreadSheet = (Map<String, Map>) result;
        Map<String, Map> contentMap = jsonSpreadSheet.get("content");
        Map<String, Map> objectsMap = contentMap.get("objects");
        List<Map<String,String>> buildingDataMap = (List<Map<String,String>>) objectsMap.get("BuildingData");

        for (Map<String,String> building : buildingDataMap) {
            String faction = building.get("faction");
            String uid = building.get("uid");
            String buildingID = building.get("buildingID");
            String type = building.get("type");
            int lvl = Integer.valueOf(building.get("lvl"));
            int storage = Integer.valueOf(building.get("storage") == null ? "0" : building.get("storage")).intValue();
            int time = Integer.valueOf(building.get("time")).intValue();

            BuildingData buildingData = new BuildingData(uid);
            buildingData.setFaction(faction);
            buildingData.setLevel(lvl);
            buildingData.setType(type);
            buildingData.setStorage(storage);
            buildingData.setTime(time);
            buildingData.setBuildingID(buildingID);

            map.put(buildingData.getUid(), buildingData);
        }

        return map;
    }

    @Override
    public BuildingData getBuildingDataByUid(String uid) {
        return this.buildings.get(uid);
    }
}
