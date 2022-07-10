package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.Command;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.game.ContractType;
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
        PlayerLoginCommandResult response = loadPlayerTemplate();
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId(),
                        response.playerModel);
        playerSession.playerLogin(time);
        mapLoginForPlayer(response, playerSession);
        return response;
    }

    private PlayerLoginCommandResult loadPlayerTemplate() throws Exception {
        PlayerLoginCommandResult response = ServiceFactory.instance().getJsonParser()
                .toObjectFromResource(ServiceFactory.instance().getConfig().playerLoginTemplate, PlayerLoginCommandResult.class);
        return response;
    }

    // TODO - setup map and troops
    private void mapLoginForPlayer(PlayerLoginCommandResult playerLoginResponse, PlayerSession playerSession) {
        playerLoginResponse.playerModel.map = playerSession.getPlayerMapItems().getBaseMap();
        playerLoginResponse.playerId = playerSession.getPlayerId();
        playerLoginResponse.name = playerSession.getPlayer().getPlayerSettings().getName();
        playerLoginResponse.playerModel.faction = playerSession.getPlayer().getPlayerSettings().getFaction();
        playerLoginResponse.playerModel.currentQuest = playerSession.getPlayerSettings().getCurrentQuest();

        if (playerLoginResponse.playerModel.currentQuest == null || !playerLoginResponse.playerModel.currentQuest.isEmpty())
            playerLoginResponse.playerModel.isFueInProgress = true;
        else
            playerLoginResponse.playerModel.isFueInProgress = false;
        playerLoginResponse.firstTimePlayer = playerSession.getPlayerSettings().getCurrentQuest() == null ? true : false;

        mapBuildableTroops(playerLoginResponse.playerModel, playerSession.getPlayer().getPlayerSettings());
        mapShards(playerLoginResponse.playerModel);
        mapDonatedTroops(playerLoginResponse.playerModel, playerSession);

        mapContracts(playerLoginResponse.playerModel, playerSession);
        mapInventory(playerLoginResponse.playerModel, playerSession);
        mapCreatureTrapData(playerLoginResponse.playerModel, playerSession);

        mapCampaignAndMissions(playerLoginResponse.playerModel, playerSession.getPlayerSettings());

        // turn off conflicts
        playerLoginResponse.sharedPrefs.put("tv", null);
        // this disables login to google at start up
        playerLoginResponse.sharedPrefs.put("promptedForGoogleSignin", "1");

        playerLoginResponse.liveness = new Liveness();
        playerLoginResponse.liveness.keepAliveTime = ServiceFactory.getSystemTimeSecondsFromEpoch();

        // this last login seems very important, the client needs it as setting this to funny values
        // seems to make the client send funny times in the commands. Just not sure if this should be set
        // to the current real world time.
        playerLoginResponse.liveness.lastLoginTime = ServiceFactory.getSystemTimeSecondsFromEpoch();
    }

    private void mapCampaignAndMissions(PlayerModel playerModel, PlayerSettings playerSettings) {
        playerModel.campaigns = playerSettings.getPlayerCampaignMission().campaigns;
        playerModel.missions = playerSettings.getPlayerCampaignMission().missions;
    }

    private void mapCreatureTrapData(PlayerModel playerModel, PlayerSession playerSession) {
        playerModel.creatureTrapData = new ArrayList<>();
        if (playerSession.getCreatureManager().hasCreature()) {
            CreatureTrapData creatureTrapData = new CreatureTrapData();
            creatureTrapData.buildingId = playerSession.getCreatureManager().getBuildingKey();
            creatureTrapData.specialAttackUid = playerSession.getCreatureManager().getSpecialAttackUid();
            creatureTrapData.ready = playerSession.getCreatureManager().isCreatureAlive();
            creatureTrapData.championUid = playerSession.getCreatureManager().getCreatureUid();
            playerModel.creatureTrapData.add(creatureTrapData);

            // set the storage to indicate if we have a creature in there or not
            playerModel.map.buildings.stream().filter(a -> a.key.equals(creatureTrapData.buildingId))
                    .findFirst().get().currentStorage = creatureTrapData.ready ? 1 : 0;
        }
    }

    private void mapInventory(PlayerModel playerModel, PlayerSession playerSession) {
        playerModel.inventory.capacity = -1;
        playerModel.inventory.storage = playerSession.getPlayerSettings().getInventoryStorage();
        playerModel.inventory.subStorage = mapDeployableTroops(playerSession);
    }

    private SubStorage mapDeployableTroops(PlayerSession playerSession) {
        SubStorage subStorage = new SubStorage();
        TrainingManager trainingManager = playerSession.getTrainingManager();
        mapDeployableTroops(playerSession, trainingManager.getDeployableTroops(), subStorage.troop.storage);
        mapDeployableTroops(playerSession, trainingManager.getDeployableChampion(), subStorage.champion.storage);
        mapDeployableTroops(playerSession, trainingManager.getDeployableHero(), subStorage.hero.storage);
        mapDeployableTroops(playerSession, trainingManager.getDeployableSpecialAttack(), subStorage.specialAttack.storage);
        return subStorage;
    }

    private void mapDeployableTroops(PlayerSession playerSession, DeployableQueue deployableQueue, Map<String, StorageAmount> storage) {
        storage.clear();
        for (Map.Entry<String,Integer> entry : deployableQueue.getDeployableUnits().entrySet()) {
            TroopData troopData = this.getTroopForPlayerByUnitId(playerSession, entry.getKey());
            StorageAmount storageAmount = new StorageAmount();
            storageAmount.amount = entry.getValue().longValue();
            storageAmount.capacity = -1;
            storageAmount.scale = troopData.getSize();
            storage.put(troopData.getUid(), storageAmount);
        }
    }

    // TODO - set the troops for the SC
    private void mapDonatedTroops(PlayerModel playerModel, PlayerSession playerSession) {
        playerModel.donatedTroops = playerSession.getDonatedTroops();
    }

    /**
     * Contracts are things that are still being built
     * @param playerModel
     * @param playerSession
     */
    private void mapContracts(PlayerModel playerModel, PlayerSession playerSession) {
        playerModel.contracts.clear();
        mapContracts(playerSession, playerModel.contracts, playerSession.getTrainingManager().getDeployableTroops().getUnitsInQueue());
        mapContracts(playerSession, playerModel.contracts, playerSession.getTrainingManager().getDeployableChampion().getUnitsInQueue());
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
            for (TroopUpgrade troopUpgrade: troops.getUpgrades()) {
                Contract contract = new Contract();
                contract.contractType = ContractType.Research.name();
                contract.buildingId = troopUpgrade.getBuildingKey();
                contract.uid = troopUpgrade.getTroopUnitId();
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

    private TroopData getTroopForPlayerByUnitId(PlayerSession playerSession, String unitId) {
        return playerSession.getTroopInventory().getTroopByUnitId(unitId);
    }

    // TODO
    private void mapBuildableTroops(PlayerModel playerModel, PlayerSettings playerSettings) {
        playerModel.upgrades = playerSettings.getUpgrades();
        playerModel.upgrades.troop = map(playerSettings.getTroops().getTroops());
        playerModel.upgrades.specialAttack = map(playerSettings.getTroops().getSpecialAttacks());

        // samples
        playerModel.prizes = new Upgrades();
    }

    private Map<String, Integer> map(HashMap<String, TroopRecord> troops) {
        Map<String,Integer> map = new HashMap<>();
        if (troops != null)
            troops.forEach((a,b) -> map.put(a, b.getLevel()));
        return map;
    }

    // TODO - no shards for now but may need to populate as some troops needs shards to be able to build them
    private void mapShards(PlayerModel playerModel) {
        playerModel.shards.clear();
    }

    @Override
    protected Messages createMessage(Command command, PlayerLoginCommandResult commandResult) {
        return new LoginMessages(command.getTime(), ServiceFactory.getSystemTimeSecondsFromEpoch(),
                ServiceFactory.createRandomUUID());
    }
}