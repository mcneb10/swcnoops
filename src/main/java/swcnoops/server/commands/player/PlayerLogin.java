package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.Command;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.datasource.PvpAttack;
import swcnoops.server.datasource.TournamentStat;
import swcnoops.server.game.ContractType;
import swcnoops.server.game.GameDataManager;
import swcnoops.server.game.RaidManager;
import swcnoops.server.game.TroopData;
import swcnoops.server.json.JsonParser;
import swcnoops.server.commands.player.response.PlayerLoginCommandResult;
import swcnoops.server.model.*;
import swcnoops.server.requests.LoginMessages;
import swcnoops.server.requests.Messages;
import swcnoops.server.session.creature.CreatureManager;
import swcnoops.server.session.inventory.TroopRecord;
import swcnoops.server.session.inventory.TroopUpgrade;
import swcnoops.server.session.inventory.Troops;
import swcnoops.server.session.training.BuildUnit;
import swcnoops.server.session.training.TrainingManager;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.training.DeployableQueue;

import java.util.*;

public class PlayerLogin extends AbstractCommandAction<PlayerLogin, PlayerLoginCommandResult> {
    private float timeZoneOffset;

    public float getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(float timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

    @Override
    final public String getAction() {
        return "player.login";
    }

    @Override
    protected PlayerLogin parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerLogin.class);
    }

    // TODO - need to fix this to log in properly for the player
    @Override
    protected PlayerLoginCommandResult execute(PlayerLogin arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .loginPlayerSession(arguments.getPlayerId());

        playerSession.playerLogin(time);
        PlayerLoginCommandResult response = loadPlayerTemplate();
        playerSession.getPlayerSettings().setTimeZoneOffset(arguments.getTimeZoneOffset());
        mapLoginForPlayer(response, playerSession, time);
        playerSession.savePlayerLogin(arguments.getTimeZoneOffset(), time);

        return response;
    }

    private PlayerLoginCommandResult loadPlayerTemplate() throws Exception {
        PlayerLoginCommandResult response = ServiceFactory.instance().getJsonParser()
                .toObjectFromResource(ServiceFactory.instance().getConfig().playerLoginTemplate, PlayerLoginCommandResult.class);
        return response;
    }

    // TODO - setup map and troops
    private void mapLoginForPlayer(PlayerLoginCommandResult playerLoginResponse, PlayerSession playerSession, long time) {
        playerLoginResponse.playerModel.map = playerSession.getPlayerMapItems().getBaseMap();
        playerLoginResponse.playerId = playerSession.getPlayerId();
        playerLoginResponse.name = playerSession.getPlayer().getPlayerSettings().getName();
        playerLoginResponse.playerModel.faction = playerSession.getPlayer().getPlayerSettings().getFaction();
        playerLoginResponse.playerModel.currentQuest = playerSession.getPlayerSettings().getCurrentQuest();

        mapGuild(playerLoginResponse.playerModel, playerSession);

        if (playerLoginResponse.playerModel.currentQuest == null || !playerLoginResponse.playerModel.currentQuest.isEmpty())
            playerLoginResponse.playerModel.isFueInProgress = true;
        else
            playerLoginResponse.playerModel.isFueInProgress = false;
        playerLoginResponse.firstTimePlayer = playerSession.getPlayerSettings().getCurrentQuest() == null ? true : false;

        mapBuildableTroops(playerLoginResponse.playerModel, playerSession.getPlayer().getPlayerSettings());
        mapShards(playerLoginResponse.playerModel, playerSession);
        mapDonatedTroops(playerLoginResponse.playerModel, playerSession);

        mapContracts(playerLoginResponse.playerModel, playerSession);
        mapInventory(playerLoginResponse.playerModel, playerSession);
        mapCreatureTrapData(playerLoginResponse.playerModel, playerSession);

        mapCampaignAndMissions(playerLoginResponse.playerModel, playerSession.getPlayerSettings());
        mapRaids(playerLoginResponse.playerModel, playerSession, time);

        mapSharedPreferencs(playerLoginResponse, playerSession);
        mapUnlockedPlanets(playerLoginResponse, playerSession.getPlayerSettings());

        playerLoginResponse.liveness = new Liveness();
        playerLoginResponse.liveness.keepAliveTime = ServiceFactory.getSystemTimeSecondsFromEpoch();

        // TODO - not sure if this should be the servers time now or the time passed in by client
        // this last login seems very important, the client needs it as setting this to funny values
        // seems to make the client send funny times in the commands. Just not sure if this should be set
        // to the current real world time.
        playerLoginResponse.liveness.lastLoginTime = ServiceFactory.getSystemTimeSecondsFromEpoch();

        playerLoginResponse.playerModel.identitySwitchTimes = new HashMap<>();
        if (!playerSession.getPlayer().getPlayerSecret().getMissingSecret()) {
            playerLoginResponse.playerModel.identitySwitchTimes.put(playerSession.getPlayerId(), playerLoginResponse.liveness.lastLoginTime);
            playerLoginResponse.playerModel.identitySwitchTimes.put(playerSession.getPlayerId() + "-2", playerLoginResponse.liveness.lastLoginTime);
        }

        playerLoginResponse.scalars = playerSession.getScalarsManager().getObjectForReading();
        playerLoginResponse.playerModel.battleLogs = ServiceFactory.instance().getPlayerDatasource().getPlayerBattleLogs(playerSession.getPlayerId());
        playerLoginResponse.playerModel.DamagedBuildings = mapDamagedBuildings(playerSession);
        playerLoginResponse.playerModel.tournaments = mapTournaments(playerSession);
        playerLoginResponse.playerModel.timeZoneOffset = playerSession.getPlayerSettings().getTimeZoneOffset();
        playerLoginResponse.currentlyDefending = mapCurrentlyDefending(playerSession);

        playerLoginResponse.playerModel.playerObjectives = mapObjectives(playerSession.getPlayerSettings(), time);

        mapProtection(playerLoginResponse.playerModel, playerSession, time);
    }

    private void mapProtection(PlayerModel playerModel, PlayerSession playerSession, long time) {
        // TODO - reset if no protection anymore and if not a bought one
        if (playerSession.getProtectionManager().getObjectForReading() != null) {
            Long protectedUntil = playerSession.getProtectionManager().getObjectForReading();
            if (protectedUntil != 0 && protectedUntil < time) {
                playerSession.getProtectionManager().setObjectForSaving(Long.valueOf(0));
            }

            playerModel.protectedUntil = playerSession.getProtectionManager().getObjectForReading();
        }
    }

    private Map<String, ObjectiveGroup> mapObjectives(PlayerSettings playerSettings, long time) {
        int hqLevel = playerSettings.getHqLevel();
        if (hqLevel < ServiceFactory.instance().getGameDataManager().getGameConstants().objectives_unlocked)
            return null;

        Map<String, ObjectiveGroup> groups = ServiceFactory.instance().getGameDataManager()
                .getObjectiveManager().getObjectiveGroups(playerSettings.getUnlockedPlanets(),
                        playerSettings.getFaction(),
                        hqLevel);
        return groups;
    }

    private void mapRaids(PlayerModel playerModel, PlayerSession playerSession, long time) {
        playerModel.raids = new HashMap<>();
        RaidManager raidManager = ServiceFactory.instance().getGameDataManager().getRaidManager();
        PlayerSettings playerSettings = playerSession.getPlayerSettings();
        List<Raid> playersRaids = raidManager.getRaids(playerSettings.getUnlockedPlanets(),
                playerSession.getRaidLogsManager().getObjectForReading(),
                playerSettings.getTimeZoneOffset(),
                playerSettings.getFaction(),
                playerSettings.getHqLevel(),
                time);

        if (playersRaids != null) {
            playersRaids.forEach(r -> playerModel.raids.put(r.planetId, r));
        }
    }

    private Map<String, Tournament> mapTournaments(PlayerSession playerSession) {
        Map<String, Tournament> tournaments = null;

        List<TournamentStat> tournamentStats = playerSession.getPlayerSettings().getTournaments();

        if (tournamentStats != null) {
            tournaments = new HashMap<>();
            for (TournamentStat tournamentStat : tournamentStats) {
                Tournament tournament = new Tournament();
                tournament.uid = tournamentStat.uid;
                tournament.bestTier = 1;
                tournament.rating = tournamentStat.value;
                tournament.redeemedRewards = null;
                tournament.collected = false;

                // TODO - when conflict finishes
                tournament.finalRank = new TournamentRank();
                tournament.finalRank.percentile = 2;
                tournament.finalRank.tier = null;
                
                tournaments.put(tournamentStat.uid, tournament);
            }
        }

        return tournaments;
    }

    private Map<String, Long> mapCurrentlyDefending(PlayerSession playerSession) {
        Map<String, Long> currentlyDefending = null;

        PvpAttack pvpAttack = playerSession.getCurrentPvPDefence().getObjectForReading();
        if (pvpAttack != null) {
            currentlyDefending = new HashMap<>();
            currentlyDefending.put("expiration", pvpAttack.expiration);
        }

        return currentlyDefending;
    }

    private Map<String, Integer> mapDamagedBuildings(PlayerSession playerSession) {
        Map<String, Integer> damagedBuildings = playerSession.getDamagedBuildingManager().getObjectForReading();
        return damagedBuildings;
    }

    private void mapUnlockedPlanets(PlayerLoginCommandResult playerLoginResponse, PlayerSettings playerSettings) {
        playerLoginResponse.playerModel.unlockedPlanets = playerSettings.getUnlockedPlanets();
    }

    private void mapGuild(PlayerModel playerModel, PlayerSession playerSession) {
        if (playerSession.getPlayerSettings().getGuildId() != null) {
            playerModel.guildInfo = new GuildInfo();
            playerModel.guildInfo.guildId = playerSession.getPlayerSettings().getGuildId();
            playerModel.guildInfo.guildName = playerSession.getPlayerSettings().getGuildName();
            // TODO - and the rest
        } else {
            playerModel.guildInfo = null;
        }
    }

    private void mapSharedPreferencs(PlayerLoginCommandResult playerLoginResponse, PlayerSession playerSession) {
        PlayerSettings playerSettings = playerSession.getPlayerSettings();
        playerLoginResponse.sharedPrefs.putAll(playerSettings.getSharedPreferences());

        // this disables login to google at start up
        playerLoginResponse.sharedPrefs.put("promptedForGoogleSignin", "1");
        // this is to stop armory tutorial from triggering
        playerLoginResponse.sharedPrefs.put("EqpTut", "3");
        playerLoginResponse.sharedPrefs.remove("EqpTutStep");

        // send the last login, this is used by client to work out which battle logs are new
        playerLoginResponse.sharedPrefs.put("llt", String.valueOf(playerSession.getLastLoginTime()));
    }

    private void mapCampaignAndMissions(PlayerModel playerModel, PlayerSettings playerSettings) {
        playerModel.campaigns = playerSettings.getPlayerCampaignMission().campaigns;
        playerModel.missions = playerSettings.getPlayerCampaignMission().missions;
    }

    private void mapCreatureTrapData(PlayerModel playerModel, PlayerSession playerSession) {
        playerModel.creatureTrapData = new ArrayList<>();
        CreatureManager creatureManager = playerSession.getCreatureManager();
        if (playerSession.getCreatureManager().hasCreature()) {
            CreatureTrapData creatureTrapData = new CreatureTrapData();
            creatureTrapData.buildingId = creatureManager.getBuildingKey();
            creatureTrapData.specialAttackUid = creatureManager.getSpecialAttackUid();
            creatureTrapData.ready = creatureManager.isCreatureAlive();
            TroopData troopData = playerSession.getTroopInventory().getTroopByUnitId(creatureManager.getCreatureUnitId());
            creatureTrapData.championUid = troopData.getUid();
            playerModel.creatureTrapData.add(creatureTrapData);
            // set the storage to indicate if we have a creature in there or not
            creatureManager.getBuilding().currentStorage = creatureTrapData.ready ? 1 : 0;
        }
    }

    private void mapInventory(PlayerModel playerModel, PlayerSession playerSession) {
        playerModel.inventory.capacity = -1;
        playerModel.inventory.storage = playerSession.getInventoryManager().getObjectForReading();
        playerModel.inventory.subStorage = mapDeployableTroops(playerSession);
    }

    private SubStorage mapDeployableTroops(PlayerSession playerSession) {
        SubStorage subStorage = new SubStorage();
        TrainingManager trainingManager = playerSession.getTrainingManager();
        mapDeployableTroops(playerSession, trainingManager.getDeployableTroops(), subStorage.troop.storage);
        mapDeployableChampion(playerSession, trainingManager.getDeployableChampion(), subStorage.champion.storage);
        mapDeployableTroops(playerSession, trainingManager.getDeployableHero(), subStorage.hero.storage);
        mapDeployableTroops(playerSession, trainingManager.getDeployableSpecialAttack(), subStorage.specialAttack.storage);
        return subStorage;
    }

    /**
     * Only allow 1 instance of each deka
     * @param playerSession
     * @param deployableQueue
     * @param storage
     */
    private void mapDeployableChampion(PlayerSession playerSession, DeployableQueue deployableQueue, Map<String, StorageAmount> storage) {
        storage.clear();
        for (Map.Entry<String,Integer> entry : deployableQueue.getDeployableUnits().entrySet()) {
            TroopData troopData = this.getTroopForPlayerByUnitId(playerSession, entry.getKey());
            StorageAmount storageAmount = new StorageAmount();
            storageAmount.amount = entry.getValue().longValue() == 0 ? 0 : 1;
            storageAmount.capacity = -1;
            storageAmount.scale = troopData.getSize();
            storage.put(troopData.getUid(), storageAmount);
        }
    }

    private void mapDeployableTroops(PlayerSession playerSession, DeployableQueue deployableQueue, Map<String, StorageAmount> storage) {
        storage.clear();
        for (Map.Entry<String, Integer> entry : deployableQueue.getDeployableUnits().entrySet()) {
            TroopData troopData = this.getTroopForPlayerByUnitId(playerSession, entry.getKey());
            StorageAmount storageAmount = new StorageAmount();
            storageAmount.amount = entry.getValue().longValue();
            storageAmount.capacity = -1;
            storageAmount.scale = troopData.getSize();
            storage.put(troopData.getUid(), storageAmount);
        }
    }

    private void mapDonatedTroops(PlayerModel playerModel, PlayerSession playerSession) {
        playerModel.donatedTroops = levelUpTroopsByUid(playerSession.getDonatedTroops(), playerSession);
    }

    private DonatedTroops levelUpTroopsByUid(DonatedTroops donatedTroops, PlayerSession playerSession) {
        DonatedTroops levelUpTroops = new DonatedTroops();

        if (donatedTroops != null) {
            GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
            donatedTroops.forEach((donatedUid, donatedByGroup) -> {
                TroopData troopData = gameDataManager.getTroopDataByUid(donatedUid);
                TroopData playersTroopData = playerSession.getTroopInventory().getTroopByUnitId(troopData.getUnitId());
                if (playersTroopData != null) {
                    if (playersTroopData.getLevel() > troopData.getLevel()) {
                        donatedUid = playersTroopData.getUid();
                    }
                }

                if (levelUpTroops.containsKey(donatedUid)) {
                    GuildDonatedTroops guildDonatedTroops = levelUpTroops.get(donatedUid);
                    donatedByGroup.forEach((donatedBy, numberOfTroops) ->
                    {
                        if (guildDonatedTroops.containsKey(donatedBy))
                            guildDonatedTroops.put(donatedBy, guildDonatedTroops.get(donatedBy) + numberOfTroops);
                        else
                            guildDonatedTroops.put(donatedBy, numberOfTroops);
                    });
                } else {
                    levelUpTroops.put(donatedUid, donatedByGroup);
                }
            });
        }

        return levelUpTroops;
    }

    /**
     * Contracts are things that are still being built
     *
     * @param playerModel
     * @param playerSession
     */
    private void mapContracts(PlayerModel playerModel, PlayerSession playerSession) {
        playerModel.contracts.clear();
        mapContracts(playerSession, playerModel.contracts, playerSession.getTrainingManager().getDeployableTroops().getUnitsInQueue());
        mapChampionContracts(playerSession, playerModel.contracts, playerSession.getTrainingManager().getDeployableChampion());
        mapContracts(playerSession, playerModel.contracts, playerSession.getTrainingManager().getDeployableHero().getUnitsInQueue());
        mapContracts(playerSession, playerModel.contracts, playerSession.getTrainingManager().getDeployableSpecialAttack().getUnitsInQueue());
        mapCreatureContract(playerModel.contracts, playerSession.getCreatureManager());
        mapTroopUpgradeContract(playerModel.contracts, playerSession.getTroopInventory().getTroops());
        mapBuildingContracts(playerModel.contracts, playerSession.getDroidManager().getUnitsInQueue());
    }

    private void mapBuildingContracts(List<Contract> contracts, Collection<BuildUnit> unitsInQueue) {
        for (BuildUnit buildUnit : unitsInQueue) {
            Contract contract = new Contract();
            contract.contractType = buildUnit.getContractType().name();
            contract.buildingId = buildUnit.getBuildingId();
            contract.uid = buildUnit.getUnitId();
            contract.endTime = buildUnit.getEndTime();
            contracts.add(contract);
        }
    }

    private void mapTroopUpgradeContract(List<Contract> contracts, Troops troops) {
        if (troops.getUpgrades().size() > 0) {
            for (TroopUpgrade troopUpgrade : troops.getUpgrades()) {
                Contract contract = new Contract();
                contract.contractType = ContractType.Research.name();
                contract.buildingId = troopUpgrade.getBuildingKey();
                contract.uid = troopUpgrade.getTroopUId();
                contract.endTime = troopUpgrade.getEndTime();
                contracts.add(contract);
            }
        }
    }

    private void mapCreatureContract(List<Contract> contracts, CreatureManager creatureManager) {
        if (creatureManager.hasCreature()) {
            if (creatureManager.isRecapturing()) {
                Contract contract = new Contract();
                contract.contractType = ContractType.Creature.name();
                contract.buildingId = creatureManager.getBuildingKey();
                contract.uid = creatureManager.getBuildingUid();
                contract.endTime = creatureManager.getRecaptureEndTime();
                contracts.add(contract);
            }
        }
    }

    private void mapContracts(PlayerSession playerSession, List<Contract> contracts, List<BuildUnit> troopsInQueue) {
        for (BuildUnit buildUnit : troopsInQueue) {
            Contract contract = new Contract();
            contract.contractType = buildUnit.getContractType().name();
            contract.buildingId = buildUnit.getBuildingId();
            TroopData troopData = getTroopForPlayerByUnitId(playerSession, buildUnit.getUnitId());
            contract.uid = troopData.getUid();
            contract.endTime = buildUnit.getEndTime();
            contracts.add(contract);
        }
    }

    private void mapChampionContracts(PlayerSession playerSession, List<Contract> contracts, DeployableQueue deployableQueue) {
        Set<String> addedDeka = new HashSet<>();

        for (Map.Entry<String,Integer> dekaDeployable : deployableQueue.getDeployableUnits().entrySet()) {
            if (dekaDeployable.getValue() > 0) {
                String unitId = dekaDeployable.getKey();
                addedDeka.add(unitId);
            }
        }

        // we only allow 1 contract per deka
        for (BuildUnit buildUnit : deployableQueue.getUnitsInQueue()) {
            TroopData troopData = getTroopForPlayerByUnitId(playerSession, buildUnit.getUnitId());
            if (!addedDeka.contains(troopData.getUnitId())) {
                addedDeka.add(troopData.getUnitId());
                Contract contract = new Contract();
                contract.contractType = buildUnit.getContractType().name();
                contract.buildingId = buildUnit.getBuildingId();
                contract.uid = troopData.getUid();
                contract.endTime = buildUnit.getEndTime();
                contracts.add(contract);
            }
        }
    }

    private TroopData getTroopForPlayerByUnitId(PlayerSession playerSession, String unitId) {
        return playerSession.getTroopInventory().getTroopByUnitId(unitId);
    }

    // TODO - loading and saving samples
    private void mapBuildableTroops(PlayerModel playerModel, PlayerSettings playerSettings) {
        playerModel.upgrades = new Upgrades();
        playerModel.upgrades.troop = map(playerSettings.getTroops().getTroops());
        playerModel.upgrades.specialAttack = map(playerSettings.getTroops().getSpecialAttacks());

        // add in all troops that needs fragments
        List<TroopData> troopsForFaction = new LinkedList<>(ServiceFactory.instance().getGameDataManager()
                .getLowestLevelTroopsForFaction(playerSettings.getFaction()));
        troopsForFaction.removeIf(a -> a.getUpgradeShardUid() == null);
        troopsForFaction.forEach(a -> {
            if (!a.isSpecialAttack() && !playerModel.upgrades.troop.containsKey(a.getUnitId()))
                playerModel.upgrades.troop.put(a.getUnitId(), a.getLevel());
        });

        troopsForFaction.forEach(a -> {
            if (a.isSpecialAttack() && !playerModel.upgrades.specialAttack.containsKey(a.getUnitId()))
                playerModel.upgrades.specialAttack.put(a.getUnitId(), a.getLevel());
        });

        // TODO - samples
        playerModel.prizes = new Upgrades();
    }

    private Map<String, Integer> map(HashMap<String, TroopRecord> troops) {
        Map<String, Integer> map = new HashMap<>();
        if (troops != null)
            troops.forEach((a, b) -> map.put(a, b.getLevel()));
        return map;
    }

    private void mapShards(PlayerModel playerModel, PlayerSession playerSession) {
        playerModel.shards.clear();
        List<TroopData> troopsForFaction = new LinkedList<>(ServiceFactory.instance().getGameDataManager()
                .getLowestLevelTroopsForFaction(playerSession.getFaction()));
        troopsForFaction.removeIf(a -> a.getUpgradeShardUid() == null);
        troopsForFaction.forEach(a -> playerModel.shards.put(a.getUpgradeShardUid(), Integer.valueOf(1000)));
    }

    @Override
    protected Messages createMessage(Command command, PlayerLoginCommandResult commandResult) {
        return new LoginMessages(command.getTime(), ServiceFactory.getSystemTimeSecondsFromEpoch(),
                ServiceFactory.createRandomUUID());
    }

    @Override
    public boolean canAttachGuildNotifications() {
        return false;
    }
}