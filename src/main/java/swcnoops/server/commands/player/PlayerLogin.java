package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.Command;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.json.JsonParser;
import swcnoops.server.commands.player.response.PlayerLoginCommandResult;
import swcnoops.server.model.*;
import swcnoops.server.requests.LoginMessages;
import swcnoops.server.requests.Messages;
import swcnoops.server.session.training.BuildUnit;
import swcnoops.server.session.training.TrainingManager;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.training.DeployableQueue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        PlayerLoginCommandResult response;
        try {
            response = loadPlayerTemplate();
            mapLoginForPlayer(response, arguments.getPlayerId());
        } catch (Exception ex) {
            // TODO
            response = new PlayerLoginCommandResult();
        }

        return response;
    }

    private PlayerLoginCommandResult loadPlayerTemplate() throws Exception {
        PlayerLoginCommandResult response = ServiceFactory.instance().getJsonParser()
                .toObjectFromResource(ServiceFactory.instance().getConfig().playerLoginTemplate, PlayerLoginCommandResult.class);
        return response;
    }

    // TODO - setup map and troops
    private void mapLoginForPlayer(PlayerLoginCommandResult playerLoginResponse, String playerId) {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(playerId);

        playerLoginResponse.playerModel.map = playerSession.getBaseMap();
        playerLoginResponse.playerId = playerSession.getPlayerId();
        playerLoginResponse.name = playerSession.getPlayer().getPlayerSettings().getName();
        //playerLoginResponse.playerModel.faction = playerSession.getPlayer().getPlayerSettings().getFaction();

        mapBuildableTroops(playerLoginResponse.playerModel, playerSession.getPlayer().getPlayerSettings());
        mapShards(playerLoginResponse.playerModel);
        mapDonatedTroops(playerLoginResponse.playerModel);

        playerSession.processCompletedContracts(ServiceFactory.getSystemTimeSecondsFromEpoch());
        mapContracts(playerLoginResponse.playerModel, playerSession);
        mapInventory(playerLoginResponse.playerModel, playerSession);

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

    private void mapInventory(PlayerModel playerModel, PlayerSession playerSession) {
        playerModel.inventory = new Inventory();
        playerModel.inventory.capacity = -1;
        playerModel.inventory.storage = mapInventoryStorage();
        playerModel.inventory.subStorage = mapDeployableTroops(playerSession);
    }

    private SubStorage mapDeployableTroops(PlayerSession playerSession) {
        SubStorage subStorage = new SubStorage();
        TrainingManager trainingManager = playerSession.getTrainingManager();
        mapDeployableTroops(trainingManager.getDeployableTroops(), subStorage.troop.storage);
        mapDeployableTroops(trainingManager.getDeployableChampion(), subStorage.champion.storage);
        mapDeployableTroops(trainingManager.getDeployableHero(), subStorage.hero.storage);
        mapDeployableTroops(trainingManager.getDeployableSpecialAttack(), subStorage.specialAttack.storage);
        return subStorage;
    }

    private void mapDeployableTroops(DeployableQueue deployableQueue, Map<String, StorageAmount> storage) {
        storage.clear();
        Iterator<Map.Entry<String,Integer>> troopIterator = deployableQueue.getDeployableUnits().entrySet().iterator();
        while(troopIterator.hasNext()) {
            Map.Entry<String,Integer> entry = troopIterator.next();
            StorageAmount storageAmount = new StorageAmount();
            storageAmount.amount = entry.getValue().intValue();
            storageAmount.capacity = -1;
            storageAmount.scale = ServiceFactory.instance().getGameDataManager()
                    .getTroopDataByUid(entry.getKey()).getSize();
            storage.put(entry.getKey(), storageAmount);
        }
    }

    private InventoryStorage mapInventoryStorage() {
        InventoryStorage inventoryStorage = new InventoryStorage();
        inventoryStorage.crystals.capacity = 7200000;
        inventoryStorage.crystals.amount = 7200000;
        inventoryStorage.crystals.scale = 1;

        inventoryStorage.credits.capacity = 7200000;
        inventoryStorage.credits.amount = 7200000;
        inventoryStorage.credits.scale = 1;

        inventoryStorage.materials.capacity = 7200000;
        inventoryStorage.materials.amount = 7200000;
        inventoryStorage.materials.scale = 1;

        inventoryStorage.contraband.capacity = 930000;
        inventoryStorage.contraband.amount = 930000;
        inventoryStorage.contraband.scale = 1;

        inventoryStorage.droids.capacity = 5;
        inventoryStorage.droids.amount = 5;
        inventoryStorage.droids.scale = 1;

        inventoryStorage.droids_prestige.capacity = 1;
        inventoryStorage.droids_prestige.amount = 51;
        inventoryStorage.droids_prestige.scale = 1;

        inventoryStorage.troop.capacity = 20;
        inventoryStorage.troop.amount = 0;
        inventoryStorage.troop.scale = 1;

        inventoryStorage.hero.capacity = 0;
        inventoryStorage.hero.amount = 0;
        inventoryStorage.hero.scale = 1;

        inventoryStorage.champion.capacity = 0;
        inventoryStorage.champion.amount = 0;
        inventoryStorage.champion.scale = 1;
        return inventoryStorage;
    }

    // TODO - set the troops for the SC
    private void mapDonatedTroops(PlayerModel playerModel) {
        if (playerModel.donatedTroops == null)
            playerModel.donatedTroops = new HashMap<>();
        else
            playerModel.donatedTroops.clear();
    }

    /**
     * Contracts are things that are still being built
     * @param playerModel
     * @param playerSession
     */
    private void mapContracts(PlayerModel playerModel, PlayerSession playerSession) {
        playerModel.contracts.clear();
        mapContracts(playerModel.contracts, playerSession.getTrainingManager().getDeployableTroops().getUnitsInQueue());
        mapContracts(playerModel.contracts, playerSession.getTrainingManager().getDeployableChampion().getUnitsInQueue());
        mapContracts(playerModel.contracts, playerSession.getTrainingManager().getDeployableHero().getUnitsInQueue());
        mapContracts(playerModel.contracts, playerSession.getTrainingManager().getDeployableSpecialAttack().getUnitsInQueue());
    }

    private void mapContracts(List<Contract> contracts, List<BuildUnit> troopsInQueue) {
        for (BuildUnit buildUnit : troopsInQueue) {
            Contract contract = new Contract();
            contract.contractType = buildUnit.getBuildSlot().getBuildableData().getContractType();
            contract.buildingId = buildUnit.getBuildingId();
            contract.uid = buildUnit.getUnitTypeId();
            contract.endTime = buildUnit.getEndTime();
            contracts.add(contract);
        }
    }

    // TODO
    private void mapBuildableTroops(PlayerModel playerModel, PlayerSettings playerSettings) {
        playerModel.upgrades = playerSettings.getUpgrades();

        // samples
        playerModel.prizes = new Upgrades();
    }

    // TODO - no shards for now but may need to populate as some troops needs shards to be able to build them
    private void mapShards(PlayerModel playerModel) {
        playerModel.shards.clear();
    }

    @Override
    protected Messages createMessage(Command command) {
        return new LoginMessages(command.getTime(), ServiceFactory.getSystemTimeSecondsFromEpoch(),
                ServiceFactory.createRandomUUID());
    }
}