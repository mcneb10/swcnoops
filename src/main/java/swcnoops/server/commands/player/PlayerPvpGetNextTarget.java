package swcnoops.server.commands.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerPvpGetNextTargetCommandResult;
import swcnoops.server.game.*;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.creature.CreatureDataMap;
import swcnoops.server.session.creature.CreatureManagerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Finds and returns an enemy base for PVP.
 */
public class PlayerPvpGetNextTarget extends AbstractCommandAction<PlayerPvpGetNextTarget, CommandResult> {
    private static final Logger LOG = LoggerFactory.getLogger(PlayerPvpGetNextTarget.class);

    @Override
    protected CommandResult execute(PlayerPvpGetNextTarget arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId());
        PvpMatch pvpMatch = playerSession.getPvpSession().getNextMatch();

        CommandResult response = setupResponse(playerSession, pvpMatch);
        return response;
    }

    private CommandResult setupResponse(PlayerSession playerSession, PvpMatch pvpMatch) {
        if (pvpMatch == null) {
            return ResponseHelper.newErrorResult(ResponseHelper.STATUS_CODE_PVP_TARGET_NOT_FOUND);
        }

        PlayerPvpGetNextTargetCommandResult response = new PlayerPvpGetNextTargetCommandResult();
        response.battleId = pvpMatch.getBattleId();

        pvpMatch.setBattleDate(ServiceFactory.getSystemTimeSecondsFromEpoch());
        response.playerId = pvpMatch.getParticipantId();
        response.faction = pvpMatch.getFactionType();
        response.level = pvpMatch.getLevel();
        response.champions = new HashMap<>();

        if (pvpMatch.isDevBase()) {
            response.name = ServiceFactory.instance().getGameDataManager().randomDevBaseName();
            response.guildName = ServiceFactory.instance().getGameDataManager().randomDevBaseName();
            response.guildId = pvpMatch.getParticipantId();
            response.map = new PlayerMap();
            response.map.planet = playerSession.getPlayer().getPlayerSettings().getBaseMap().planet;
            response.map.next = 2;
            response.map.buildings = ServiceFactory.instance().getPlayerDatasource().getDevBaseMap(pvpMatch.getParticipantId(), pvpMatch.getFactionType());
            setupDevResourcesBaseData(response, pvpMatch.getPlayerId());
        } else {
            swcnoops.server.datasource.Player opponentPlayer = ServiceFactory.instance().getPlayerDatasource().loadPlayer(pvpMatch.getParticipantId());
            response.map = opponentPlayer.getPlayerSettings().baseMap;
            response.name = opponentPlayer.getPlayerSettings().getName();
            response.guildName = opponentPlayer.getPlayerSettings().getGuildName();
            response.guildId = opponentPlayer.getPlayerSettings().getGuildId();
            //TODO, get from defender's file
            setupDevResourcesBaseData(response, pvpMatch.getPlayerId());
        }

        response.creditsCharged = pvpMatch.creditsCharged;
        response.xp = pvpMatch.getDefenderXp();
        setupParticipants(response, pvpMatch);
        setupScoreAndPoints(response, pvpMatch);
        setupDefenseTroops(response, response.map);
        //TODO... this properly.
        pvpMatch.setAttackerEquipment(new JsonStringArrayList());
        pvpMatch.setDefenderEquipment(new JsonStringArrayList());
        return response;
    }

    private void setupParticipants(PlayerPvpGetNextTargetCommandResult response, PvpMatch pvpMatch) {
        // TODO - get the defenders stats
        BattleParticipant defender = new BattleParticipant(response.playerId, response.name, response.guildId, response.guildName,
                0, 0, 0, response.faction);

        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(pvpMatch.getPlayerId());
        String guildName = playerSession.getGuildSession() == null ? null : playerSession.getGuildSession().getGuildName();

        Scalars scalars = playerSession.getScalarsManager().getObjectForReading();

        BattleParticipant attacker = new BattleParticipant(pvpMatch.getPlayerId(),
                playerSession.getPlayerSettings().getName(),
                playerSession.getPlayerSettings().getGuildId(),
                guildName,
                scalars.attackRating, scalars.defenseRating, 0,
                playerSession.getFaction());

        pvpMatch.setDefender(defender);
        pvpMatch.setAttacker(attacker);
    }

    private void setupScoreAndPoints(PlayerPvpGetNextTargetCommandResult response, PvpMatch pvpMatch) {
        Random random = new Random();

        // TODO - this impacts medals need to decide how to formulate
        int potentialScoreWin = Math.max(random.nextInt(50), 12);
        int potentialScoreLose = Math.max(random.nextInt(50), 12);

        // TODO - these are conflict points, need to decide how to formulate when running conflicts
        // for now these will be 0
        int potentialPointsWin = 0;
        int potentialPointsLose = 0;

        pvpMatch.setPotentialScoreWin(potentialScoreWin);
        pvpMatch.setPotentialScoreLose(potentialScoreLose);

        Map<String, Integer> potentialPoints = new HashMap<>();
        potentialPoints.put("potentialScoreWin", potentialScoreWin);
        potentialPoints.put("potentialScoreLose", potentialScoreLose);
        potentialPoints.put("potentialPointsWin", potentialPointsWin);
        potentialPoints.put("potentialPointsLose", potentialPointsLose);

        response.potentialPoints = potentialPoints;
    }

    private void setupDevResourcesBaseData(PlayerPvpGetNextTargetCommandResult response, String attackerId) {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(attackerId);

        response.attacksWon = 0;
        response.attackRating = 0;
        response.defensesWon = 0;//TODO - might be fun to track this actually?
        response.defenseRating = 0;//TODO - might be fun to track this actually? - Dev bases with the most wins!
        //Resources as a % of the attacker's total storage
        Random random = new Random();
        InventoryStorage inventoryStorage = playerSession.getInventoryManager().getObjectForReading();
        int creditStorage = inventoryStorage.credits.capacity;
        int materialsStorage = inventoryStorage.materials.capacity;
        int contraStorage = inventoryStorage.contraband.capacity;

        int currentCredits = inventoryStorage.credits.amount;
        int currentMaterials = inventoryStorage.materials.amount;
        int currentContraband = inventoryStorage.contraband.amount;
        //TODO may be worth adding these to config instead...
        float resourceScale = 0.1f;
//        BigDecimal creditsAvailable = creditStorage == currentCredits ? new BigDecimal(0) : new BigDecimal(Math.min(creditStorage * 0.25, creditStorage * random.nextFloat()));
        BigDecimal creditsAvailable = new BigDecimal(Math.min(creditStorage * resourceScale, creditStorage * random.nextFloat()));
        BigDecimal materialsAvailable = new BigDecimal(Math.min(materialsStorage * resourceScale, materialsStorage * random.nextFloat()));
        BigDecimal contraAvailable = new BigDecimal(Math.min(contraStorage * resourceScale, contraStorage * random.nextFloat()));

        Map<CurrencyType, Integer> buildingLootCreditsMap = new HashMap<>();
        buildingLootCreditsMap.put(CurrencyType.credits, creditsAvailable.intValue());

        Map<CurrencyType, Integer> buildingLootmaterialsMap = new HashMap<>();
        buildingLootmaterialsMap.put(CurrencyType.materials, materialsAvailable.intValue());

        Map<CurrencyType, Integer> buildingLootcontrabandMap = new HashMap<>();
        buildingLootcontrabandMap.put(CurrencyType.contraband, contraAvailable.intValue());

        // TODO - not sure this is correct have to debug it in client
        HashMap<CurrencyType, Map<CurrencyType, Integer>> pvpTargetResourcesMap = new HashMap<>();
        pvpTargetResourcesMap.put(CurrencyType.credits, buildingLootCreditsMap);
        pvpTargetResourcesMap.put(CurrencyType.materials, buildingLootmaterialsMap);
        pvpTargetResourcesMap.put(CurrencyType.contraband, buildingLootcontrabandMap);
        response.resources = pvpTargetResourcesMap;
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

//    private void setupLoot(PlayerPvpGetNextTargetCommandResult response, PlayerMap map) {
//        response.resources = new HashMap<>();
//        response.resources.put("bld_107", createResourceMap(50,0,0));
//        response.resources.put("bld_272", createResourceMap(0,25,0));
//    }

    private Map<CurrencyType, Integer> createResourceMap(int credits, int materials, int contraband) {
        Map<CurrencyType, Integer> buildingResource = new HashMap<>();
        if (credits > 0) {
            buildingResource.put(CurrencyType.credits, Integer.valueOf(credits));
        }

        if (materials > 0) {
            buildingResource.put(CurrencyType.materials, Integer.valueOf(materials));
        }

        if (contraband > 0) {
            buildingResource.put(CurrencyType.contraband, Integer.valueOf(contraband));
        }

        return buildingResource;
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
