package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PvpTargetCommandResult;
import swcnoops.server.datasource.TournamentStat;
import swcnoops.server.game.*;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.creature.CreatureDataMap;
import swcnoops.server.session.creature.CreatureManagerFactory;
import swcnoops.server.session.creature.CreatureStatus;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Finds and returns an enemy base for PVP.
 */
public class PlayerPvpGetNextTarget extends AbstractCommandAction<PlayerPvpGetNextTarget, CommandResult> {

    @Override
    protected CommandResult execute(PlayerPvpGetNextTarget arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId());
        PvpMatch pvpMatch = playerSession.getPvpSession().getNextMatch();

        CommandResult response = setupResponse(playerSession, pvpMatch, time);
        return response;
    }

    static public CommandResult setupResponse(PlayerSession playerSession, PvpMatch pvpMatch, long time) {
        if (pvpMatch == null) {
            return ResponseHelper.newStatusResult(ResponseHelper.STATUS_CODE_PVP_TARGET_NOT_FOUND);
        }

        if (pvpMatch.isPlayerProtected())
            return ResponseHelper.newStatusResult(ResponseHelper.STATUS_CODE_PVP_TARGET_IS_UNDER_PROTECTION);

        PvpTargetCommandResult response = new PvpTargetCommandResult();
        response.battleId = pvpMatch.getBattleId();

        pvpMatch.setBattleDate(ServiceFactory.getSystemTimeSecondsFromEpoch());
        response.playerId = pvpMatch.getParticipantId();
        response.faction = pvpMatch.getFactionType();
        response.level = pvpMatch.getLevel();
        response.champions = new HashMap<>();

        // set tournament
        TournamentData tournamentData = ServiceFactory.instance().getGameDataManager().getConflictManager()
                .getConflict(playerSession.getPlayerSettings().getBaseMap().planet);
        if (tournamentData != null && tournamentData.isActive(ServiceFactory.getSystemTimeSecondsFromEpoch())) {
            pvpMatch.setTournamentData(tournamentData);
        }

        if (pvpMatch.isDevBase()) {
            response.name = ServiceFactory.instance().getGameDataManager().randomDevBaseName();
            response.guildName = ServiceFactory.instance().getGameDataManager().randomDevBaseName();
            response.guildId = pvpMatch.getParticipantId();
            response.map = new PlayerMap();
            response.map.planet = playerSession.getPlayer().getPlayerSettings().getBaseMap().planet;
            response.map.next = 2;
            response.map.buildings = ServiceFactory.instance().getPlayerDatasource()
                    .getDevBaseMap(pvpMatch.getParticipantId(), pvpMatch.getFactionType());
            setupDevResourcesBaseData(response, playerSession);
            setupCreatureAndTraps(response, pvpMatch, response.map);
            setupRandomSCTroops(response, response.map);
        } else {
            response.map = pvpMatch.getDefendersBaseMap();
            response.name = pvpMatch.getDefendersName();
            response.guildId = pvpMatch.getDefendersGuildId();
            response.guildName = pvpMatch.getDefendersGuildName();
            setupResources(response, pvpMatch);
            setupCreatureAndTraps(response, pvpMatch, response.map);
            response.guildTroops = pvpMatch.getDefendersDonatedTroops();
        }

        response.creditsCharged = pvpMatch.creditsCharged;
        response.xp = pvpMatch.getDefenderXp();

        setupParticipants(response, pvpMatch);
        setupScoreAndPoints(response, pvpMatch);
        //TODO... this properly.
        pvpMatch.setAttackerEquipment(new JsonStringArrayList());
        pvpMatch.setDefenderEquipment(new JsonStringArrayList());
        return response;
    }

    // TODO - this currently calculates what is stored and not what is available uncollected in the store buildings.
    // may need to make this smarter as the client can take a list of buildings and its breakdown (I think)
    static private void setupResources(PvpTargetCommandResult response, PvpMatch pvpMatch) {
        response.attacksWon = pvpMatch.getDefendersScalars().attacksWon;
        response.attackRating = pvpMatch.getDefendersScalars().attackRating;
        response.defensesWon = pvpMatch.getDefendersScalars().defensesWon;
        response.defenseRating = pvpMatch.getDefendersScalars().defenseRating;

        //Resources as a % of the attacker's total storage
        Random random = new Random();
        InventoryStorage inventoryStorage = pvpMatch.getDefendersInventoryStorage();
        int creditAmount = inventoryStorage.credits.amount;
        int materialsAmount = inventoryStorage.materials.amount;
        int contraAmount = inventoryStorage.contraband.amount;

        float resourceScale = 0.5f;
        BigDecimal creditsAvailable = new BigDecimal(Math.min(creditAmount * resourceScale, creditAmount * random.nextFloat()));
        BigDecimal materialsAvailable = new BigDecimal(Math.min(materialsAmount * resourceScale, materialsAmount * random.nextFloat()));
        BigDecimal contraAvailable = new BigDecimal(Math.min(contraAmount * resourceScale, contraAmount * random.nextFloat()));

        response.resources = createResourceMap(creditsAvailable, materialsAvailable, contraAvailable);
    }

    static private void setupParticipants(PvpTargetCommandResult response, PvpMatch pvpMatch) {
        if (pvpMatch.getDefendersScalars() == null)
            pvpMatch.setDefendersScalars(new Scalars());

        Scalars defendersScalars = pvpMatch.getDefendersScalars();
        ConflictManager conflictManager = ServiceFactory.instance().getGameDataManager().getConflictManager();
        TournamentStat defendersTournamentStat = conflictManager.getTournamentStats(pvpMatch.getDefendersTournaments(),
                pvpMatch.getTournamentData());
        int defendersTournamentRating = defendersTournamentStat != null ? defendersTournamentStat.value : 0;

        BattleParticipant defender = new BattleParticipant(response.playerId, response.name, response.guildId, response.guildName,
                defendersScalars.attackRating, defendersScalars.defenseRating, defendersTournamentRating, pvpMatch.getFactionType());

        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(pvpMatch.getPlayerId());
        String guildName = playerSession.getGuildSession() == null ? null : playerSession.getGuildSession().getGuildName();

        Scalars scalars = playerSession.getScalarsManager().getObjectForReading();
        TournamentStat attackersTournamentStat = conflictManager.getTournamentStats(playerSession.getPlayerSettings().getTournaments(),
                pvpMatch.getTournamentData());

        int attackerTournamentRating = attackersTournamentStat != null ? attackersTournamentStat.value : 0;
        BattleParticipant attacker = new BattleParticipant(pvpMatch.getPlayerId(),
                playerSession.getPlayerSettings().getName(),
                playerSession.getPlayerSettings().getGuildId(),
                guildName,
                scalars.attackRating, scalars.defenseRating, attackerTournamentRating,
                playerSession.getFaction());

        pvpMatch.setDefender(defender);
        pvpMatch.setAttacker(attacker);
    }

    static private void setupScoreAndPoints(PvpTargetCommandResult response, PvpMatch pvpMatch) {
        Random random = new Random();

        // TODO - this impacts medals need to decide how to formulate
        int potentialScoreWin = Math.max(random.nextInt(50), 12);
        int potentialScoreLose = Math.max(random.nextInt(50), 12);
        int potentialPointsWin = 0;
        int potentialPointsLose = 0;

        if (pvpMatch.getTournamentData() != null) {
            GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
            potentialPointsWin = gameDataManager.getTournamentAttackerMedals(3);
            potentialPointsLose = gameDataManager.getTournamentAttackerMedals(0);
        }

        pvpMatch.setPotentialScoreWin(potentialScoreWin);
        pvpMatch.setPotentialScoreLose(potentialScoreLose);
        pvpMatch.setPotentialPointsWin(potentialPointsWin);
        pvpMatch.setPotentialPointsLose(potentialPointsLose);

        Map<String, Integer> potentialPoints = new HashMap<>();
        potentialPoints.put("potentialScoreWin", potentialScoreWin);
        potentialPoints.put("potentialScoreLose", potentialScoreLose);
        potentialPoints.put("potentialPointsWin", potentialPointsWin);
        potentialPoints.put("potentialPointsLose", potentialPointsLose);

        response.potentialPoints = potentialPoints;
    }

    static private void setupDevResourcesBaseData(PvpTargetCommandResult response, PlayerSession playerSession) {
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

        float resourceScale = 0.1f;
        BigDecimal creditsAvailable = new BigDecimal(Math.min(creditStorage * resourceScale, creditStorage * random.nextFloat()));
        BigDecimal materialsAvailable = new BigDecimal(Math.min(materialsStorage * resourceScale, materialsStorage * random.nextFloat()));
        BigDecimal contraAvailable = new BigDecimal(Math.min(contraStorage * resourceScale, contraStorage * random.nextFloat()));

        response.resources = createResourceMap(creditsAvailable, materialsAvailable, contraAvailable);
    }

    static private void setupCreatureAndTraps(PvpTargetCommandResult response, PvpMatch pvpMatch, PlayerMap map) {
        // enable champions and traps
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();

        response.champions.clear();
        for (Building building : map.buildings) {
            BuildingData buildingData = gameDataManager.getBuildingDataByUid(building.uid);
            if (buildingData != null) {
                switch (buildingData.getType()) {
                    case champion_platform:
                        building.currentStorage = 0;
                        TroopData champData = gameDataManager.getTroopDataByUid(buildingData.getLinkedUnit());
                        if (pvpMatch.isDevBase() || pvpMatch.getDefendersDeployableTroopsChampion().containsKey(champData.getUnitId())) {
                            response.champions.put(buildingData.getLinkedUnit(), Integer.valueOf(1));
                            building.currentStorage = 1;
                        }
                        break;
                    case trap:
                        if (pvpMatch.isDevBase() ) {
                            building.currentStorage = 1;
                        }
                        break;
                }
            }
        }

        // TODO - for real PvP enable creature
        CreatureDataMap creatureDataMap = CreatureManagerFactory.findCreatureTrap(map);
        if (creatureDataMap != null && creatureDataMap.building != null) {
            response.creatureTrapData = new ArrayList<>();
            CreatureTrapData creatureTrapData = new CreatureTrapData();
            creatureTrapData.buildingId = creatureDataMap.building.key;
            creatureTrapData.specialAttackUid = creatureDataMap.trapData.getEventData();

            if (pvpMatch.isDevBase()) {
                String creatureUnitId = CreatureManagerFactory.getRandomCreatureUnitId(creatureDataMap.buildingData.getFaction());
                String creatureUid = ServiceFactory.instance().getGameDataManager()
                        .getTroopDataByUnitId(creatureUnitId, creatureDataMap.buildingData.getLevel()).getUid();
                creatureTrapData.championUid = creatureUid;
                creatureTrapData.ready = true;
            } else if (pvpMatch.getDefendersCreature() != null) {
                TroopData troopData = gameDataManager.getTroopByUnitId(pvpMatch.getDefendersTroops(),
                        pvpMatch.getDefendersCreature().getCreatureUnitId());
                creatureTrapData.championUid = troopData.getUid();
                creatureTrapData.ready = pvpMatch.getDefendersCreature().getCreatureStatus() == CreatureStatus.Alive;
            }

            if (creatureTrapData.ready)
                creatureDataMap.building.currentStorage = 1;

            response.creatureTrapData.add(creatureTrapData);
        }
    }

    static private void setupRandomSCTroops(PvpTargetCommandResult response, PlayerMap map) {
        Building scBuilding = null;
        for (Building building : map.buildings) {
            BuildingData buildingData =
                    ServiceFactory.instance().getGameDataManager().getBuildingDataByUid(building.uid);
            if (buildingData != null) {
                switch (buildingData.getType()) {
                    case squad:
                        scBuilding = building;
                        break;
                }
            }
        }

        response.guildTroops = new DonatedTroops();
        if (scBuilding != null) {
            GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
            BuildingData scBuildingData = gameDataManager.getBuildingDataByUid(scBuilding.uid);
            response.guildTroops = createRandomDonatedTroops(scBuildingData.getFaction(), scBuildingData.getStorage());
        }
    }

    // TODO - this needs to be improved to stop miss hits with the sizes and counts.
    // this can be done by building maps of what is available based on size, and using the random to pick an index of
    // that map.
    static private DonatedTroops createRandomDonatedTroops(FactionType faction, int storage) {
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

    static private Map<String, Map<CurrencyType, Integer>> createResourceMap(BigDecimal creditsAvailable,
                                                                            BigDecimal materialsAvailable,
                                                                            BigDecimal contraAvailable)
    {
        Map<CurrencyType, Integer> buildingLootCreditsMap = new HashMap<>();
        buildingLootCreditsMap.put(CurrencyType.credits, creditsAvailable.intValue());

        Map<CurrencyType, Integer> buildingLootmaterialsMap = new HashMap<>();
        buildingLootmaterialsMap.put(CurrencyType.materials, materialsAvailable.intValue());

        Map<CurrencyType, Integer> buildingLootcontrabandMap = new HashMap<>();
        buildingLootcontrabandMap.put(CurrencyType.contraband, contraAvailable.intValue());

        HashMap<String, Map<CurrencyType, Integer>> pvpTargetResourcesMap = new HashMap<>();

        // TODO - not sure which building to assign this to yet, but this seems to work
        pvpTargetResourcesMap.put(CurrencyType.credits.toString(), buildingLootCreditsMap);
        pvpTargetResourcesMap.put(CurrencyType.materials.toString(), buildingLootmaterialsMap);
        pvpTargetResourcesMap.put(CurrencyType.contraband.toString(), buildingLootcontrabandMap);

        return pvpTargetResourcesMap;
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
