package swcnoops.server.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.player.PlayerPvpGetNextTarget;
import swcnoops.server.model.*;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class GameDataManagerImpl implements GameDataManager {
    private Map<String, TroopData> troops = new HashMap<>();
    private Map<String, TroopData> lowestLevelTroopByUnitId = new HashMap<>();
    private Map<String, List<TroopData>> troopLevelsByUnitId = new HashMap<>();
    private Map<String, List<BuildingData>> buildingLevelsByBuildingId = new HashMap<>();
    private Map<FactionType, List<TroopData>> creaturesByFaction = new HashMap<>();
    private Map<FactionType, List<TroopData>> lowestLevelTroopByFaction = new HashMap<>();
    private Map<String, BuildingData> buildings = new HashMap<>();
    private Map<String, TrapData> traps = new HashMap<>();
    private Map<FactionType, CampaignSet> factionCampaigns = new HashMap<>();
    private Map<String, CampaignSet> campaignSets = new HashMap<>();
    private Map<String, CampaignMissionData> missionDataMap = new HashMap<>();
    private Map<String, CampaignMissionSet> campaignMissionSets = new HashMap<>();
    private Map<FactionType, Map<Integer, List<TroopData>>> troopSizeMapByFaction = new HashMap<>();
    private GameConstants gameConstants;
    private List<File> layouts;
    private static final Logger LOG = LoggerFactory.getLogger(PlayerPvpGetNextTarget.class);
    private FactionBuildingEquivalentMap factionBuildingEquivalentMap;

    @Override
    public void initOnStartup() {
        try {
            loadTroops();
            loadBaseJson();
            this.traps = loadTraps();
            loadCampaigns();
            buildCustomTroopMaps();
            setDevBases();
            this.factionBuildingEquivalentMap = create(this.buildingLevelsByBuildingId);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load game data from patches", ex);
        }
    }

    private FactionBuildingEquivalentMap create(Map<String, List<BuildingData>> buildingLevelsByBuildingId) {
        FactionBuildingEquivalentMap factionBuildingEquivalentMap = new FactionBuildingEquivalentMap();
        for (List<BuildingData> buildingLevels : buildingLevelsByBuildingId.values()) {
            factionBuildingEquivalentMap.addMap(buildingLevels);
        }

        return factionBuildingEquivalentMap;
    }

    private void buildCustomTroopMaps() {
        // list of creatures for faction for populating strix beacon
        this.lowestLevelTroopByUnitId.forEach((a, b) -> {
            if (b.getType() == TroopType.creature) {
                List<TroopData> creatureList = this.creaturesByFaction.get(b.getFaction());
                if (creatureList == null) {
                    creatureList = new ArrayList<>();
                    this.creaturesByFaction.put(b.getFaction(), creatureList);
                }
                creatureList.add(b);
            }
        });

        this.lowestLevelTroopByUnitId.forEach((a, b) -> {
            List<TroopData> troopList = this.lowestLevelTroopByFaction.get(b.getFaction());
            if (troopList == null) {
                troopList = new ArrayList<>();
                this.lowestLevelTroopByFaction.put(b.getFaction(), troopList);
            }
            troopList.add(b);
        });

        // map used to group troops by unit size for SC populating, does not include special attacks or champions
        this.lowestLevelTroopByUnitId.forEach((a, b) -> {
            if (!b.isSpecialAttack() && !(b.getType() == TroopType.champion)) {
                Map<Integer, List<TroopData>> troopBySizeMap = this.troopSizeMapByFaction.get(b.getFaction());
                if (troopBySizeMap == null) {
                    troopBySizeMap = new HashMap<>();
                    this.troopSizeMapByFaction.put(b.getFaction(), troopBySizeMap);
                }

                List<TroopData> troopBySize = troopBySizeMap.get(b.getSize());

                if (troopBySize == null) {
                    troopBySize = new ArrayList<>();
                    troopBySizeMap.put(b.getSize(), troopBySize);
                }

                troopBySize.add(b);
            }
        });
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
            CampaignMissionSet campaignMissionSet = campaignSet.getCampaignMissionSet(campaignMissionData.getCampaignUid());
            campaignMissionSet.addMission(campaignMissionData);
            this.campaignMissionSets.put(campaignMissionSet.getUid(), campaignMissionSet);
            this.missionDataMap.put(campaignMissionData.getUid(), campaignMissionData);
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
        List<Map<String, String>> troopDataMap = (List<Map<String, String>>) objectsMap.get("TroopData");
        addTroopsToMap(troopDataMap);
        List<Map<String, String>> specialAttackDataMap = (List<Map<String, String>>) objectsMap.get("SpecialAttackData");
        addTroopsToMap(specialAttackDataMap);
    }

    private void addTroopsToMap(List<Map<String, String>> troopDataMap) {
        Set<String> unitIdsNeedsSorting = new HashSet<>();
        for (Map<String, String> troop : troopDataMap) {
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
            int credits = Integer.valueOf(troop.get("credits") == null ? "0" : troop.get("credits"));
            int materials = Integer.valueOf(troop.get("materials") == null ? "0" : troop.get("materials"));
            int contraband = Integer.valueOf(troop.get("contraband") == null ? "0" : troop.get("contraband"));
            int upgradeCredits = Integer.valueOf(troop.get("upgradeCredits") == null ? "0" : troop.get("upgradeCredits"));
            int upgradeMaterials = Integer.valueOf(troop.get("upgradeMaterials") == null ? "0" : troop.get("upgradeMaterials"));
            int upgradeContraband = Integer.valueOf(troop.get("upgradeContraband") == null ? "0" : troop.get("upgradeContraband"));

            TroopData troopData = new TroopData(uid);
            troopData.setFaction(FactionType.valueOf(faction));
            troopData.setLevel(lvl);
            troopData.setUnitId(unitId != null ? unitId : specialAttackID);
            troopData.setType(type != null ? TroopType.valueOf(type) : null);
            troopData.setSize(size);
            troopData.setTrainingTime(trainingTime);
            troopData.setUpgradeTime(upgradeTime);
            troopData.setUpgradeShardUid(upgradeShardUid);
            troopData.setUpgradeShards(upgradeShards);
            troopData.setSpecialAttackID(specialAttackID);
            troopData.setCredits(credits);
            troopData.setMaterials(materials);
            troopData.setContraband(contraband);
            troopData.setUpgradeCredits(upgradeCredits);
            troopData.setUpgradeMaterials(upgradeMaterials);
            troopData.setUpgradeContraband(upgradeContraband);

            this.troops.put(troopData.getUid(), troopData);
            addToLowestLevelTroopUnitId(troopData);

            String needToSort = addToLevelMaps(troopData);
            if (needToSort != null)
                unitIdsNeedsSorting.add(needToSort);
        }

        unitIdsNeedsSorting.forEach(a -> this.troopLevelsByUnitId.get(a)
                .sort((b, c) -> Integer.compare(b.getLevel(), c.getLevel())));
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
        List<Map<String, String>> trapDataMap = (List<Map<String, String>>) objectsMap.get("TrapData");
        addTrapsToMap(map, trapDataMap);
        return map;
    }

    private void addTrapsToMap(Map<String, TrapData> map, List<Map<String, String>> trapDataMap) {
        for (Map<String, String> trap : trapDataMap) {
            String uid = trap.get("uid");
            TrapEventType trapEventType = TrapEventType.valueOf(trap.get("eventType"));
            long rearmTime = trap.get("rearmTime") != null ? Long.valueOf(trap.get("rearmTime")) : 0;
            int rearmMaterialsCost = trap.get("rearmMaterialsCost") != null ? Integer.valueOf(trap.get("rearmMaterialsCost")) : 0;
            String eventData = trap.get("eventData");
            TrapData trapData = new TrapData(uid);
            trapData.setEventType(trapEventType);
            trapData.setRearmTime(rearmTime);
            trapData.setEventData(eventData);
            trapData.setRearmMaterialsCost(rearmMaterialsCost);
            map.put(trapData.getUid(), trapData);
        }
    }

    private void loadBaseJson() throws Exception {
        Map result = ServiceFactory.instance().getJsonParser()
                .toObjectFromResource(ServiceFactory.instance().getConfig().baseJson, Map.class);
        Map<String, Map> jsonSpreadSheet = (Map<String, Map>) result;
        Map<String, Map> contentMap = jsonSpreadSheet.get("content");
        Map<String, Map> objectsMap = contentMap.get("objects");
        List<Map<String, String>> buildingDataMap = (List<Map<String, String>>) objectsMap.get("BuildingData");
        this.buildings = readBuildingData(buildingDataMap);

        List<Map<String, String>> gameConstants = (List<Map<String, String>>) objectsMap.get("GameConstants");
        this.gameConstants = readGameConstants(gameConstants);
    }

    private GameConstants readGameConstants(List<Map<String, String>> gameConstants) throws Exception {
        GameConstants constants = GameConstants.createFromBaseJson(gameConstants);
        return constants;
    }

    private Map<String, BuildingData> readBuildingData(List<Map<String, String>> buildingDataMap) {
        Map<String, BuildingData> map = new HashMap<>();

        Set<String> buildingIdsNeedsSorting = new HashSet<>();

        for (Map<String, String> building : buildingDataMap) {
            String faction = building.get("faction");
            String uid = building.get("uid");
            String buildingID = building.get("buildingID");
            String type = building.get("type");
            String trapId = building.get("trapID");
            int lvl = Integer.valueOf(building.get("lvl"));
            int storage = Integer.valueOf(building.get("storage") == null ? "0" : building.get("storage")).intValue();
            int time = Integer.valueOf(building.get("time")).intValue();
            int crossTime = Integer.valueOf(building.get("crossTime") == null ? "0" : building.get("crossTime")).intValue();
            int crossCredits = Integer.valueOf(building.get("crossCredits") == null ? "0" : building.get("crossCredits")).intValue();
            int crossMaterials = Integer.valueOf(building.get("crossMaterials") == null ? "0" : building.get("crossMaterials")).intValue();
            int materials = Integer.valueOf(building.get("materials") == null ? "0" : building.get("materials")).intValue();
            int credits = Integer.valueOf(building.get("credits") == null ? "0" : building.get("credits")).intValue();
            int contraband = Integer.valueOf(building.get("contraband") == null ? "0" : building.get("contraband")).intValue();
            String currency = building.get("currency") != null ? building.get("currency") : "none";
            String linkedUnit = building.get("linkedUnit");
            StoreTab storeTab = building.get("storeTab") != null ? StoreTab.valueOf(building.get("storeTab")) : null;
            BuildingSubType buildingSubType = building.get("subType") != null ?
                    BuildingSubType.valueOf(building.get("subType")) : null;
            float produce = Float.valueOf(building.get("produce") != null ? building.get("produce") : "0");
            float cycleTime = Float.valueOf(building.get("cycleTime") != null ? building.get("cycleTime") : "0");
            boolean prestige = Boolean.valueOf(building.get("prestige") != null ? building.get("prestige") : "false");
            int xp = Integer.valueOf(building.get("xp") == null ? "0" : building.get("xp")).intValue();


            BuildingData buildingData = new BuildingData(uid);
            buildingData.setFaction(FactionType.valueOf(faction));
            buildingData.setLevel(lvl);
            buildingData.setType(BuildingType.valueOf(type));
            buildingData.setCurrency(CurrencyType.valueOf(currency.equals("0") ? "none" : currency));
            buildingData.setProduce(produce);
            buildingData.setCycleTime(cycleTime);
            buildingData.setStorage(storage);
            buildingData.setTime(time);
            buildingData.setCrossTime(crossTime);
            buildingData.setCrossCredits(crossCredits);
            buildingData.setCrossMaterials(crossMaterials);
            buildingData.setBuildingID(buildingID);
            buildingData.setTrapId(trapId);
            buildingData.setLinkedUnit(linkedUnit);
            buildingData.setStoreTab(storeTab);
            buildingData.setSubType(buildingSubType);
            buildingData.setMaterials(materials);
            buildingData.setCredits(credits);
            buildingData.setContraband(contraband);
            buildingData.setPrestige(prestige);
            buildingData.setXp(xp); //used to calculate base scores


            map.put(buildingData.getUid(), buildingData);
            String needToSort = addToLevelMaps(buildingData);
            if (needToSort != null)
                buildingIdsNeedsSorting.add(needToSort);
        }

        buildingIdsNeedsSorting.forEach(a -> this.buildingLevelsByBuildingId.get(a)
                .sort((b, c) -> Integer.compare(b.getLevel(), c.getLevel())));
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
        }

        return buildingID;
    }

    @Override
    public TroopData getLowestLevelTroopDataByUnitId(String unitId) {
        return this.lowestLevelTroopByUnitId.get(unitId);
    }

    @Override
    public TroopData getTroopDataByUnitId(String unitId, int level) {
        if (this.troopLevelsByUnitId.get(unitId).size() < (level - 1))
            return null;

        return this.troopLevelsByUnitId.get(unitId).get(level - 1);
    }

    @Override
    public BuildingData getBuildingDataByBuildingId(String buildingID, int level) {
        return this.buildingLevelsByBuildingId.get(buildingID).get(level - 1);
    }

    @Override
    public CampaignMissionData getCampaignMissionData(String missionUid) {
        return this.missionDataMap.get(missionUid);
    }

    @Override
    public CampaignSet getCampaignForFaction(FactionType faction) {
        return this.factionCampaigns.get(faction);
    }

    @Override
    public CampaignMissionSet getCampaignMissionSet(String campaignUid) {
        return this.campaignMissionSets.get(campaignUid);
    }

    @Override
    public List<TroopData> getLowestLevelTroopsForFaction(FactionType faction) {
        return this.lowestLevelTroopByFaction.get(faction);
    }

    @Override
    public Map<FactionType, List<TroopData>> getCreaturesByFaction() {
        return creaturesByFaction;
    }

    @Override
    public Map<Integer, List<TroopData>> getTroopSizeMap(FactionType faction) {
        return this.troopSizeMapByFaction.get(faction);
    }

    @Override
    public int getMaxlevelForTroopUnitId(String unitId) {
        return this.troopLevelsByUnitId.get(unitId).size();
    }

    @Override
    public GameConstants getGameConstants() {
        return this.gameConstants;
    }

    /**
     * This is only used when changing faction
     *
     * @param oldBuildingData
     * @param targetFaction
     * @return
     */
    @Override
    public BuildingData getFactionEquivalentOfBuilding(BuildingData oldBuildingData, FactionType targetFaction) {
        return this.factionBuildingEquivalentMap.getEquivalentBuilding(oldBuildingData, targetFaction);
    }


    private boolean clearDevBases(Connection connection) {
        String sql = "DELETE FROM DevBases";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            LOG.error("Failed to clear from Dev bases");
            return false;
        }
    }

    private void setDevBases() {

        //Clear existing
        try {
            Connection conn = getConnection();
            if (clearDevBases(conn)) {
                try {
                    layouts = listf(ServiceFactory.instance().getConfig().layoutsPath);
                    for (File layoutFile : layouts) {
                        Buildings mapObject = null;
                        mapObject = ServiceFactory.instance().getJsonParser().fromJsonFile(layoutFile.getAbsolutePath(), Buildings.class);

                        int xp = ServiceFactory.getXpFromBuildings(mapObject);
                        int hq = 0;
                        for (Building building : mapObject) {
                            BuildingData buildingData = ServiceFactory.instance().getGameDataManager().getBuildingDataByUid(building.uid);
                            if (buildingData != null) {
                                switch (buildingData.getType()) {
                                    case trap: //fill trap
                                        building.currentStorage = 1;
                                        break;
                                    case HQ:
                                        hq = buildingData.getLevel();
                                        break;
                                }
                            }
                        }

                        String s = ServiceFactory.instance().getJsonParser().toJson(mapObject);
                        String s1 = s.replace(FactionType.rebel.name(), "{faction}").replace(FactionType.empire.name(), "{faction}");

                        String insertSql = "insert into DevBases (Id, buildings, hqlevel, xp) values (?, ?, ? ,?)";
                        try {
                            PreparedStatement statement = conn.prepareStatement(insertSql);
                            statement.setString(1, ServiceFactory.createRandomUUID());
                            statement.setString(2, s1);
                            statement.setInt(3, hq);
                            statement.setInt(4, xp);
                            statement.executeUpdate();

                        } catch (SQLException e) {

                        }
                    }

                } catch (Exception ex) {
                    LOG.error("Failed to load next layout", ex);
                }
            }
        } catch (SQLException e) {
            LOG.error("Failed to get connection to the db to clear and set dev bases");
            throw new RuntimeException(e);
        }


    }

    private List<File> listf(String directoryName) {
        File directory = new File(directoryName);
        List<File> resultList = new ArrayList<>();
        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isDirectory()) {
                resultList.addAll(listf(file.getAbsolutePath()));
            } else if (file.getAbsolutePath().toLowerCase().endsWith(".json")) {
                resultList.add(file);
            }
        }
        return resultList;
    }

    private Connection getConnection() throws SQLException {
        String url = ServiceFactory.instance().getConfig().playerSqliteDB;
        Connection conn = DriverManager.getConnection(url);
        return conn;
    }
}
