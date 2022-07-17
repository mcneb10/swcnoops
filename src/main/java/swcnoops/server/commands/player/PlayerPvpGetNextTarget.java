package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerPvpGetNextTargetCommandResult;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.GameDataManager;
import swcnoops.server.game.TroopData;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;
import swcnoops.server.session.creature.CreatureDataMap;
import swcnoops.server.session.creature.CreatureManagerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Finds and returns an enemy base for PVP.
 */
public class PlayerPvpGetNextTarget extends AbstractCommandAction<PlayerPvpGetNextTarget, PlayerPvpGetNextTargetCommandResult>
{
    private List<File> layouts;
    private Random rand = new Random();

    @Override
    protected PlayerPvpGetNextTargetCommandResult execute(PlayerPvpGetNextTarget arguments, long time) throws Exception {
        PlayerPvpGetNextTargetCommandResult response =
                parseJsonFile("templates/playerPvpGetNextTarget.json", PlayerPvpGetNextTargetCommandResult.class);
        response.battleId = ServiceFactory.createRandomUUID();
        response.map.buildings = getNextLayout();
        setupDefenseTroops(response, response.map);
        return response;
    }

    private void setupDefenseTroops(PlayerPvpGetNextTargetCommandResult response, PlayerMap map) {
        // enable champions and traps
        response.champions.clear();
        Building scBuilding = null;
        for (Building building : map.buildings) {
            BuildingData buildingData =
                    ServiceFactory.instance().getGameDataManager().getBuildingDataByUid(building.uid);
            if (buildingData != null) {
                switch (buildingData.getType()) {
                    case champion_platform:
                        response.champions.put(buildingData.getLinkedUnit(), Integer.valueOf(1));
                        building.currentStorage = 1;
                        break;
                    case trap:
                        building.currentStorage = 1;
                        break;
                    case squad:
                        scBuilding = building;
                        break;
                }
            }
        }

        // enable creature
        CreatureDataMap creatureDataMap = CreatureManagerFactory.findCreatureTrap(map);
        if (creatureDataMap != null && creatureDataMap.building != null) {
            creatureDataMap.building.currentStorage = 1;
            response.creatureTrapData = new ArrayList<>();
            CreatureTrapData creatureTrapData = new CreatureTrapData();
            creatureTrapData.ready = true;
            creatureTrapData.buildingId = creatureDataMap.building.key;
            creatureTrapData.specialAttackUid = creatureDataMap.trapData.getEventData();
            String creatureUnitId = CreatureManagerFactory.getRandomCreatureUnitId(creatureDataMap.buildingData.getFaction());
            String creatureUid = ServiceFactory.instance().getGameDataManager().getTroopDataByUnitId(creatureUnitId, 10).getUid();
            creatureTrapData.championUid = creatureUid;
            response.creatureTrapData.add(creatureTrapData);
        }

        // fill SC
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        BuildingData scBuildingData = gameDataManager.getBuildingDataByUid(scBuilding.uid);
        response.guildTroops = createRandomDonatedTroops(scBuildingData.getFaction(), scBuildingData.getStorage());
    }

    // TODO - this needs to be improved to stop miss hits with the sizes and counts.
    // this can be done by building maps of what is available based on size, and using the random to pick an index of
    // that map.
    private DonatedTroops createRandomDonatedTroops(FactionType faction, int storage) {
        DonatedTroops donatedTroops = new DonatedTroops();
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        Map<Integer, List<TroopData>> troopSizeMap = gameDataManager.getTroopSizeMap(faction);
        Integer maxUnitsSize = troopSizeMap.keySet().stream().max((o1, o2) -> Integer.compare(o1, o2)).get();
        while (storage > 0) {
            Random random = new Random();

            if (storage < maxUnitsSize)
                maxUnitsSize = storage;

            int pickedSize = random.nextInt(maxUnitsSize.intValue()) + 1;
            List<TroopData> troopDataOfSize = troopSizeMap.get(pickedSize);

            if (troopDataOfSize != null) {
                Random random1 = new Random();
                TroopData chosenTroop = troopDataOfSize.get(random1.nextInt(troopDataOfSize.size()));
                int numberOfTroops = 1;
                if (storage > (pickedSize * 2)) {
                    numberOfTroops = random1.nextInt(storage/pickedSize) + 1;
                }

                int maxLevel = gameDataManager.getMaxlevelForTroopUnitId(chosenTroop.getUnitId());
                TroopData maxChosenTroop = gameDataManager.getTroopDataByUnitId(chosenTroop.getUnitId(), maxLevel);

                GuildDonatedTroops guildDonatedTroops = donatedTroops.get(maxChosenTroop.getUid());
                if (guildDonatedTroops == null) {
                    guildDonatedTroops = new GuildDonatedTroops();
                    donatedTroops.put(maxChosenTroop.getUid(), guildDonatedTroops);
                }

//                Integer donatedCount = guildDonatedTroops.get("random-sc");
//                if (donatedCount == null) {
//                    donatedCount = Integer.valueOf(0);
//                }
//                donatedCount += numberOfTroops;
//                guildDonatedTroops.put("random-sc", donatedCount);
                guildDonatedTroops.put(ServiceFactory.createRandomUUID(), numberOfTroops);
                storage -= numberOfTroops * pickedSize;
            }
        }

        return donatedTroops;
    }

    private Buildings getNextLayout() {
        Buildings mapObject = null;
        File layoutFile;
        try {
            if (layouts == null || layouts.size() == 0) {
                layouts = listf(ServiceFactory.instance().getConfig().layoutsPath);
            }

            int index = rand.nextInt(layouts.size());
            if (index < 0)
                index = 0;

            if (index >= layouts.size())
                index = layouts.size() - 1;

            layoutFile = this.layouts.get(index);
            this.layouts.remove(index);
            mapObject = ServiceFactory.instance().getJsonParser().fromJsonFile(layoutFile.getAbsolutePath(), Buildings.class);
            return mapObject;
        } catch(Exception ex) {
            // TODO
            System.out.println(ex);
        }

        return mapObject;
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

    @Override
    protected PlayerPvpGetNextTarget parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerPvpGetNextTarget.class);
    }

    @Override
    public String getAction() {
        return "player.pvp.getNextTarget";
    }
}
