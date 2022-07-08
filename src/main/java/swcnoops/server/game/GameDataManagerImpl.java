package swcnoops.server.game;

import swcnoops.server.ServiceFactory;
import swcnoops.server.model.FactionType;

import java.util.*;

public class GameDataManagerImpl implements GameDataManager {
    private Map<String, TroopData> troops = new HashMap<>();
    private Map<String, TroopData> lowestLevelTroopByUnitId = new HashMap<>();
    private Map<String, List<TroopData>> troopLevelsByUnitId = new HashMap<>();
    private Map<String, List<BuildingData>> buildingLevelsByBuildingId = new HashMap<>();

    /**
     * This map is only used to change the map from smuggler to faction which happens for new players
     */
    private Map<BuildingType, Map<FactionType, List<BuildingData>>> buildingMapByTypeAndFaction = new HashMap<>();
    private Map<String, BuildingData> buildings = new HashMap<>();
    private Map<String, TrapData> traps = new HashMap<>();
    private Map<FactionType, CampaignSet> factionCampaigns = new HashMap<>();
    private Map<String, CampaignSet> campaignSets = new HashMap<>();


    @Override
    public void initOnStartup() {
        try {
            loadTroops();
            this.buildings = loadBuildings();
            this.traps = loadTraps();
            loadCampaigns();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load game data from patches", ex);
        }
    }

    private void loadCampaigns() throws Exception {
        CampaignFile result = ServiceFactory.instance().getJsonParser()
                .toObjectFromResource(ServiceFactory.instance().getConfig().caeJson, CampaignFile.class);

        for (CampaignData campaignData : result.content.objects.campaignData) {
            CampaignSet factionCampaignSet = this.factionCampaigns.get(campaignData.getFaction());
            if (factionCampaignSet == null) {
                factionCampaignSet = new CampaignSet(campaignData.getFaction());
                this.factionCampaigns.put(factionCampaignSet.getFactionType(), factionCampaignSet);
            }

            factionCampaignSet.addCampaignData(campaignData);
            this.campaignSets.put(campaignData.getUid(), factionCampaignSet);
        }

        for (CampaignMissionData campaignMissionData : result.content.objects.campaignMissionData) {
            CampaignSet campaignSet = this.campaignSets.get(campaignMissionData.getCampaignUid());
            campaignSet.getCampaignMissionSet(campaignMissionData.getCampaignUid()).addMission(campaignMissionData);
        }
    }

    @Override
    public TroopData getTroopDataByUid(String uid) {
        return this.troops.get(uid);
    }

    @Override
    public BuildingData getBuildingDataByUid(String uid) {
        return this.buildings.get(uid);
    }

    @Override
    public TrapData getTrapDataByUid(String uid) {
        return this.traps.get(uid);
    }

    private void loadTroops() throws Exception {
        this.troops.clear();

        Map result = ServiceFactory.instance().getJsonParser()
                .toObjectFromResource(ServiceFactory.instance().getConfig().troopJson, Map.class);
        Map<String, Map> jsonSpreadSheet = (Map<String, Map>) result;
        Map<String, Map> contentMap = jsonSpreadSheet.get("content");
        Map<String, Map> objectsMap = contentMap.get("objects");
        List<Map<String,String>> troopDataMap = (List<Map<String,String>>) objectsMap.get("TroopData");
        addTroopsToMap(troopDataMap);
        List<Map<String,String>> specialAttackDataMap = (List<Map<String,String>>) objectsMap.get("SpecialAttackData");
        addTroopsToMap(specialAttackDataMap);
    }

    private void addTroopsToMap(List<Map<String, String>> troopDataMap) {
        Set<String> unitIdsNeedsSorting = new HashSet<>();
        for (Map<String,String> troop : troopDataMap) {
            String faction = troop.get("faction");
            int lvl = Integer.valueOf(troop.get("lvl"));
            String uid = troop.get("uid");              //troopEmpireChicken1
            String unitId = troop.get("unitID");        //EmpireChicken
            String type = troop.get("type");
            int size = Integer.valueOf(troop.get("size")).intValue();
            long trainingTime = Long.valueOf(troop.get("trainingTime")).longValue();
            long upgradeTime = Long.valueOf(troop.get("upgradeTime")).longValue();
            String upgradeShardUid = troop.get("upgradeShardUid");
            int upgradeShards = Integer.valueOf(troop.get("upgradeShards") == null ? "0" : troop.get("upgradeShards")).intValue();
            String specialAttackID = troop.get("specialAttackID");

            TroopData troopData = new TroopData(uid);
            troopData.setFaction(faction);
            troopData.setLevel(lvl);
            troopData.setUnitId(unitId != null ? unitId : specialAttackID);
            troopData.setType(type);
            troopData.setSize(size);
            troopData.setTrainingTime(trainingTime);
            troopData.setUpgradeTime(upgradeTime);
            troopData.setUpgradeShardUid(upgradeShardUid);
            troopData.setUpgradeShards(upgradeShards);
            troopData.setSpecialAttackID(specialAttackID);

            this.troops.put(troopData.getUid(), troopData);
            addToLowestLevelTroopUnitId(troopData);

            String needToSort = addToLevelMaps(troopData);
            if (needToSort != null)
                unitIdsNeedsSorting.add(needToSort);
        }

        unitIdsNeedsSorting.forEach(a -> this.troopLevelsByUnitId.get(a)
                .sort((b,c) -> Integer.compare(b.getLevel(),c.getLevel())));
    }

    private String addToLevelMaps(TroopData troopData) {
        String unitId = troopData.getUnitId();
        if (unitId != null) {
            List<TroopData> levels = this.troopLevelsByUnitId.get(unitId);
            if (levels == null) {
                levels = new ArrayList<>();
                this.troopLevelsByUnitId.put(unitId, levels);
            }

            // this list will be sorted later before it can be used
            levels.add(troopData);
        }

        return unitId;
    }

    private void addToLowestLevelTroopUnitId(TroopData troopData) {
        TroopData currentLowest = this.lowestLevelTroopByUnitId.get(troopData.getUnitId());
        if (currentLowest == null) {
            this.lowestLevelTroopByUnitId.put(troopData.getUnitId(), troopData);
        } else {
            if (troopData.getLevel() < currentLowest.getLevel()) {
                this.lowestLevelTroopByUnitId.put(troopData.getUnitId(), troopData);
            }
        }
    }

    private Map<String, TrapData> loadTraps() throws Exception {
        Map<String, TrapData> map = new HashMap<>();
        Map result = ServiceFactory.instance().getJsonParser()
                .toObjectFromResource(ServiceFactory.instance().getConfig().baseJson, Map.class);
        Map<String, Map> jsonSpreadSheet = (Map<String, Map>) result;
        Map<String, Map> contentMap = jsonSpreadSheet.get("content");
        Map<String, Map> objectsMap = contentMap.get("objects");
        List<Map<String,String>> trapDataMap = (List<Map<String,String>>) objectsMap.get("TrapData");
        addTrapsToMap(map, trapDataMap);
        return map;
    }

    private void addTrapsToMap(Map<String, TrapData> map, List<Map<String, String>> trapDataMap) {
        for (Map<String,String> trap : trapDataMap) {
            String uid = trap.get("uid");
            TrapEventType trapEventType = TrapEventType.valueOf(trap.get("eventType"));
            Long rearmTime = trap.get("rearmTime") != null ? new Long(trap.get("rearmTime")) : null;
            String eventData = trap.get("eventData");
            TrapData trapData = new TrapData(uid);
            trapData.setEventType(trapEventType);
            trapData.setRearmTime(rearmTime);
            trapData.setEventData(eventData);
            map.put(trapData.getUid(), trapData);
        }
    }

    private Map<String, BuildingData> loadBuildings() throws Exception {
        Map<String, BuildingData> map = new HashMap<>();

        Map result = ServiceFactory.instance().getJsonParser()
                .toObjectFromResource(ServiceFactory.instance().getConfig().baseJson, Map.class);
        Map<String, Map> jsonSpreadSheet = (Map<String, Map>) result;
        Map<String, Map> contentMap = jsonSpreadSheet.get("content");
        Map<String, Map> objectsMap = contentMap.get("objects");
        List<Map<String,String>> buildingDataMap = (List<Map<String,String>>) objectsMap.get("BuildingData");

        Set<String> buildingIdsNeedsSorting = new HashSet<>();

        for (Map<String,String> building : buildingDataMap) {
            String faction = building.get("faction");
            String uid = building.get("uid");
            String buildingID = building.get("buildingID");
            String type = building.get("type");
            String trapId = building.get("trapID");
            int lvl = Integer.valueOf(building.get("lvl"));
            int storage = Integer.valueOf(building.get("storage") == null ? "0" : building.get("storage")).intValue();
            int time = Integer.valueOf(building.get("time")).intValue();
            String linkedUnit = building.get("linkedUnit");
            StoreTab storeTab = building.get("storeTab") != null ? StoreTab.valueOf(building.get("storeTab")) : null;
            BuildingSubType buildingSubType = building.get("subType") != null ?
                    BuildingSubType.valueOf(building.get("subType")) : null;

            BuildingData buildingData = new BuildingData(uid);
            buildingData.setFaction(FactionType.valueOf(faction));
            buildingData.setLevel(lvl);
            buildingData.setType(BuildingType.valueOf(type));
            buildingData.setStorage(storage);
            buildingData.setTime(time);
            buildingData.setBuildingID(buildingID);
            buildingData.setTrapId(trapId);
            buildingData.setLinkedUnit(linkedUnit);
            buildingData.setStoreTab(storeTab);
            buildingData.setSubType(buildingSubType);

            map.put(buildingData.getUid(), buildingData);
            String needToSort = addToLevelMaps(buildingData);
            if (needToSort != null)
                buildingIdsNeedsSorting.add(needToSort);
        }

        buildingIdsNeedsSorting.forEach(a -> this.buildingLevelsByBuildingId.get(a)
                .sort((b,c) -> Integer.compare(b.getLevel(),c.getLevel())));

        return map;
    }

    private String addToLevelMaps(BuildingData buildingData) {
        String buildingID = buildingData.getBuildingID();
        if (buildingID != null) {
            List<BuildingData> levels = this.buildingLevelsByBuildingId.get(buildingID);
            if (levels == null) {
                levels = new ArrayList<>();
                this.buildingLevelsByBuildingId.put(buildingID, levels);
            }

            // this list will be sorted later before it can be used
            levels.add(buildingData);

            if (buildingData.getStoreTab() != StoreTab.not_in_store) {
                if (!(buildingData.getType() == BuildingType.turret &&
                        buildingData.getSubType() != BuildingSubType.rapid_fire_turret))
                {

                    Map<FactionType, List<BuildingData>> factionMap = this.buildingMapByTypeAndFaction.get(buildingData.getType());
                    if (factionMap == null) {
                        factionMap = new HashMap<>();
                        this.buildingMapByTypeAndFaction.put(buildingData.getType(), factionMap);
                    }

                    factionMap.put(buildingData.getFaction(), levels);
                }
            }
        }

        return buildingID;
    }

    @Override
    public TroopData getLowestLevelTroopDataByUnitId(String unitId) {
        return this.lowestLevelTroopByUnitId.get(unitId);
    }

    @Override
    public TroopData getTroopDataByUnitId(String unitId, int level) {
        return this.troopLevelsByUnitId.get(unitId).get(level - 1);
    }

    @Override
    public BuildingData getBuildingDataByBuildingId(String buildingID, int level) {
        return this.buildingLevelsByBuildingId.get(buildingID).get(level - 1);
    }

    @Override
    public BuildingData getBuildingData(BuildingType type, FactionType faction, int level) {
        return this.buildingMapByTypeAndFaction.get(type).get(faction).get(level - 1);
    }
}
