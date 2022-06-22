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
import swcnoops.server.session.PlayerSession;

import java.util.HashMap;

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
            response = loadPlayerTemplate(arguments.getPlayerId());
            configureLoginForPlayer(response, arguments.getPlayerId(), time);
        } catch (Exception ex) {
            // TODO
            response = new PlayerLoginCommandResult();
        }

        return response;
    }

    private PlayerLoginCommandResult loadPlayerTemplate(String playerId) throws Exception {
        PlayerLoginCommandResult response = ServiceFactory.instance().getJsonParser()
                .toObjectFromResource(ServiceFactory.instance().getConfig().playerLoginTemplate, PlayerLoginCommandResult.class);
        return response;
    }

    // TODO - setup map and troops
    private void configureLoginForPlayer(PlayerLoginCommandResult playerLoginResponse, String playerId, long time) {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(playerId);

        setupBaseMap(playerLoginResponse.playerModel, playerSession.getPlayer().getPlayerSettings());
        playerSession.configureForMap(playerLoginResponse.playerModel.map);

        playerLoginResponse.playerId = playerSession.getPlayerId();
        playerLoginResponse.name = playerSession.getPlayer().getPlayerSettings().getName();
        //playerLoginResponse.playerModel.faction = playerSession.getPlayer().getPlayerSettings().getFaction();

        setupBuildableTroops(playerLoginResponse.playerModel, playerSession.getPlayer().getPlayerSettings());
        setupShards(playerLoginResponse.playerModel);
        setupDonatedTroops(playerLoginResponse.playerModel);

        playerSession.onboardTransports(ServiceFactory.getSystemTimeSecondsFromEpoch());
        setupContracts(playerLoginResponse.playerModel, playerSession, time);
        setupInventory(playerLoginResponse.playerModel, playerSession);

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

    private void setupBaseMap(PlayerModel playerModel, PlayerSettings playerSettings) {
        // if settings does not have a map then we take our template one
        if (playerSettings.getBaseMap() == null) {
            playerSettings.setBaseMap(playerModel.map);
        }

        playerModel.map = playerSettings.getBaseMap();
    }

    private void setupInventory(PlayerModel playerModel, PlayerSession playerSession) {
        playerModel.inventory = new Inventory();
        playerModel.inventory.capacity = -1;
        playerModel.inventory.storage = loadInventoryStorage();
        playerModel.inventory.subStorage = loadTroopsInTransport(playerSession);
    }

    private SubStorage loadTroopsInTransport(PlayerSession playerSession) {
        SubStorage subStorage = new SubStorage();
        playerSession.loadTransports(subStorage);
        subStorage.champion.storage.clear();
        return subStorage;
    }

    private InventoryStorage loadInventoryStorage() {
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
    private void setupDonatedTroops(PlayerModel playerModel) {
        if (playerModel.donatedTroops == null)
            playerModel.donatedTroops = new HashMap<>();
        else
            playerModel.donatedTroops.clear();
    }

    // TODO - this is to set the contracts which represents the things that are currently being built
    private void setupContracts(PlayerModel playerModel, PlayerSession playerSession, long time) {
        playerSession.loadContracts(playerModel.contracts, time);
    }

    // TODO
    private void setupBuildableTroops(PlayerModel playerModel, PlayerSettings playerSettings) {
        playerModel.upgrades = playerSettings.getUpgrades();

        // samples
        playerModel.prizes = new Upgrades();
    }

    // TODO - no shards for now but may need to populate as some troops needs shards to be able to build them
    private void setupShards(PlayerModel playerModel) {
        playerModel.shards.clear();
    }

    @Override
    protected Messages createMessage(Command command) {
        return new LoginMessages(command.getTime(), ServiceFactory.getSystemTimeSecondsFromEpoch(),
                ServiceFactory.createRandomUUID());
    }
}