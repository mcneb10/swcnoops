package swcnoops.server.commands.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerPvpGetNextTargetCommandResult;
import swcnoops.server.game.*;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;
import swcnoops.server.session.CurrencyDelta;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.creature.CreatureDataMap;
import swcnoops.server.session.creature.CreatureManagerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Finds and returns an enemy base for PVP.
 */
public class PlayerPvpGetNextTarget extends AbstractCommandAction<PlayerPvpGetNextTarget, PlayerPvpGetNextTargetCommandResult> {
    private static final Logger LOG = LoggerFactory.getLogger(PlayerPvpGetNextTarget.class);
    private List<File> layouts;
    private Random rand = new Random();

    @Override
    protected PlayerPvpGetNextTargetCommandResult execute(PlayerPvpGetNextTarget arguments, long time) throws Exception {

        PlayerPvpGetNextTargetCommandResult response = new PlayerPvpGetNextTargetCommandResult();
        setupResponse(response, arguments);

        return response;
    }

    private void setupResponse(PlayerPvpGetNextTargetCommandResult response, PlayerPvpGetNextTarget arguments) {


        PvpMatch pvpMatch = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId()).getPvpSession().getNextMatch();
        response.battleId = pvpMatch.getBattleId();
        pvpMatch.setBattleDate(ServiceFactory.getSystemTimeSecondsFromEpoch());
        response.name = ServiceFactory.instance().getGameDataManager().randomDevBaseName();
        response.guildName = ServiceFactory.instance().getGameDataManager().randomDevBaseName();
        response.playerId = pvpMatch.getParticipantId();
        response.faction = pvpMatch.getFactionType().name();
        response.guildId = pvpMatch.getParticipantId();
        response.level = pvpMatch.getLevel();
        PlayerMap map = new PlayerMap();
        map.planet = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId()).getPlayer().getPlayerSettings().getBaseMap().planet;
        map.next = 2;
        response.map = map;

        response.champions = new HashMap<>();

        if (pvpMatch.isDevBase()) {
            response.map.buildings = ServiceFactory.instance().getPlayerDatasource().getDevBaseMap(pvpMatch.getParticipantId(), pvpMatch.getFactionType());
            setupDevResourcesBaseData(response, arguments.getPlayerId());
        } else {
            response.map.buildings = ServiceFactory.instance().getPlayerDatasource().loadPlayerSettings(pvpMatch.getParticipantId()).baseMap.buildings;
            //TODO, get from defender's file
            setupDevResourcesBaseData(response, arguments.getPlayerId());
        }

        response.creditsCharged = ServiceFactory.instance().getGameDataManager().getPvpMatchCost(ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId()).getHeadQuarter().getBuildingData().getLevel());
        response.xp = pvpMatch.getDefenderXp();
        //TODO - take this value from player's funds...
        CurrencyDelta currencyDelta = new CurrencyDelta(response.creditsCharged, response.creditsCharged, CurrencyType.credits, true );
        ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId()).processInventoryStorage(currencyDelta);

        setupDefenseTroops(response, response.map);
    }

    private void setupDevResourcesBaseData(PlayerPvpGetNextTargetCommandResult response, String attackerId) {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(attackerId);
        response.attacksWon = 0;
        response.attackRating = 0;
        response.defensesWon = 0;//TODO - might be fun to track this actually?
        response.defenseRating = 0;//TODO - might be fun to track this actually? - Dev bases with the most wins!
        //Resources as a % of the attacker's total storage
        Random random = new Random();
        int creditStorage = playerSession.getPlayerSettings().getInventoryStorage().credits.capacity;
        int materialsStorage = playerSession.getPlayerSettings().getInventoryStorage().materials.capacity;
        int contraStorage = playerSession.getPlayerSettings().getInventoryStorage().contraband.capacity;
        //TODO may be worth adding these to config instead...

        BigDecimal creditsAvailable = new BigDecimal(Math.max(creditStorage * 0.5, creditStorage * random.nextFloat()));
        BigDecimal materialsAvailable = new BigDecimal(Math.max(materialsStorage * 0.5, materialsStorage * random.nextFloat()));
        BigDecimal contraAvailable = new BigDecimal(Math.max(contraStorage * 0.5, contraStorage * random.nextFloat()));
//        PvpTargetResourcesMap pvpTargetResourcesMap =new PvpTargetResourcesMap(creditsAvailable.intValue(), materialsAvailable.intValue(), contraAvailable.intValue());

        Map<CurrencyType, Integer> buildingLootCreditsMap = new HashMap<>();
        buildingLootCreditsMap.put(CurrencyType.credits, creditsAvailable.intValue());

        Map<CurrencyType, Integer> buildingLootmaterialsMap = new HashMap<>();
        buildingLootmaterialsMap.put(CurrencyType.materials, materialsAvailable.intValue());

        Map<CurrencyType, Integer> buildingLootcontrabandMap = new HashMap<>();
        buildingLootcontrabandMap.put(CurrencyType.contraband, contraAvailable.intValue());

        HashMap<CurrencyType, Map<CurrencyType, Integer>> pvpTargetResourcesMap = new HashMap<>();
        pvpTargetResourcesMap.put(CurrencyType.credits, buildingLootCreditsMap);
        pvpTargetResourcesMap.put(CurrencyType.materials, buildingLootmaterialsMap);
        pvpTargetResourcesMap.put(CurrencyType.contraband, buildingLootcontrabandMap);
        response.resources = pvpTargetResourcesMap;

        Map<String, Integer> potentialPoints = new HashMap<>();
        int potentialScoreWin = Math.max(random.nextInt(50), 12);
        int potentialScoreLose = Math.max(random.nextInt(50), 12);
        potentialPoints.put("potentialScoreWin", potentialScoreWin);
        potentialPoints.put("potentialScoreLose", potentialScoreLose);
        response.potentialMedalsToGain = potentialScoreWin;
        response.potentialMedalsToLose = potentialScoreLose;
        response.potentialPoints = potentialPoints;


//        Map<ResourceTypes, Map<ResourceTypes, Integer>> resources = new HashMap<>();
//        resources.put(ResourceTypes.credits, new HashMap<>().put(ResourceTypes.credits, creditsAvailable));


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
        response.guildTroops = new DonatedTroops();
        response.guildTroops = createRandomDonatedTroops(scBuildingData.getFaction(), scBuildingData.getStorage());
    }

    // TODO - this needs to be improved to stop miss hits with the sizes and counts.
    // this can be done by building maps of what is available based on size, and using the random to pick an index of
    // that map.
    private DonatedTroops createRandomDonatedTroops(FactionType faction, int storage) {
        DonatedTroops donatedTroops = new DonatedTroops();
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        Map<Integer, List<TroopData>> troopSizeMap = gameDataManager.getTroopSizeMap(faction);

        int finalStorage = storage;
        Predicate<Integer> unitSizeFilter = Integer -> Integer <= finalStorage;
        List<Integer> availableUnitSizes = gameDataManager.getTroopSizesAvailable(faction).stream().filter(unitSizeFilter).collect(Collectors.toList());

        Integer maxUnitsSize = troopSizeMap.keySet().stream().max((o1, o2) -> Integer.compare(o1, o2)).get();

        while (storage > 0) {
            Random random = new Random();

            if (storage < maxUnitsSize)
                maxUnitsSize = storage;

            //ORIGINAL---->int pickedSize = random.nextInt(maxUnitsSize.intValue()) + 1;
            int pickedSize = availableUnitSizes.get(random.nextInt(availableUnitSizes.size() - 1));
            //ORIGINAL---->List<TroopData> troopDataOfSize = troopSizeMap.get(pickedSize) ;
            Predicate<TroopData> notAHeroSork = troopData -> troopData.getType() != TroopType.hero;
            List<TroopData> troopDataOfSize = troopSizeMap.get(pickedSize).stream().filter(notAHeroSork).collect(Collectors.toList());

            if (troopDataOfSize != null) {
                Random random1 = new Random();
                TroopData chosenTroop = troopDataOfSize.get(random1.nextInt(troopDataOfSize.size()));

                int numberOfTroops = 1;
                if (storage > (pickedSize * 2)) {
                    numberOfTroops = random1.nextInt(storage / pickedSize) + 1;
                }

                int maxLevel = gameDataManager.getMaxlevelForTroopUnitId(chosenTroop.getUnitId());
                TroopData maxChosenTroop = gameDataManager.getTroopDataByUnitId(chosenTroop.getUnitId(), maxLevel);

                GuildDonatedTroops guildDonatedTroops = donatedTroops.get(maxChosenTroop.getUid());
                if (guildDonatedTroops == null) {
                    guildDonatedTroops = new GuildDonatedTroops();
                    donatedTroops.put(maxChosenTroop.getUid(), guildDonatedTroops);
                }

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
        } catch (Exception ex) {
            LOG.error("Failed to load next layout", ex);
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
