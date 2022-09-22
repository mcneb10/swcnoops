package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerNeighborVisitResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;

public class PlayerNeighborVisit extends AbstractCommandAction<PlayerNeighborVisit, PlayerNeighborVisitResult> {
    private String neighborId;
    private String playerId;

    @Override
    protected PlayerNeighborVisitResult execute(PlayerNeighborVisit arguments, long time) throws Exception {
        // TODO - change this to read straight from DB
        swcnoops.server.datasource.Player player = ServiceFactory.instance().getPlayerDatasource()
                .loadPlayer(arguments.getNeighborId(), true,
                        "playerSettings.baseMap", "playerSettings.faction", "playerSettings.inventoryStorage",
                        "playerSettings.guildId", "playerSettings.guildName", "playerSettings.scalars");

        PlayerNeighborVisitResult playerNeighborVisitResult = new PlayerNeighborVisitResult();
        if (player != null) {
            playerNeighborVisitResult.player = new Player();
            playerNeighborVisitResult.player.name = player.getPlayerSettings().getName();

            // TODO - to finish and populate other things
            playerNeighborVisitResult.player.scalars = player.getPlayerSettings().getScalars();
            playerNeighborVisitResult.player.playerModel = new PlayerModel();
            playerNeighborVisitResult.player.playerModel.faction = player.getPlayerSettings().getFaction();
            playerNeighborVisitResult.player.playerModel.map = player.getPlayerSettings().getBaseMap();
            playerNeighborVisitResult.player.playerModel.inventory = new Inventory();
            playerNeighborVisitResult.player.playerModel.inventory.capacity = -1;
            playerNeighborVisitResult.player.playerModel.inventory.storage = player.getPlayerSettings().getInventoryStorage();
            playerNeighborVisitResult.player.playerModel.inventory.subStorage = new SubStorage();

            if (player.getPlayerSettings().getGuildId() != null) {
                playerNeighborVisitResult.player.playerModel.guildInfo = new GuildInfo();
                playerNeighborVisitResult.player.playerModel.guildInfo.guildId = player.getPlayerSettings().getGuildId();
                playerNeighborVisitResult.player.playerModel.guildInfo.guildName = player.getPlayerSettings().getGuildName();
            }

            playerNeighborVisitResult.player.playerModel.upgrades = new Upgrades();
        }

        return playerNeighborVisitResult;
    }

    @Override
    protected PlayerNeighborVisit parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerNeighborVisit.class);
    }

    @Override
    public String getAction() {
        return "player.neighbor.visit";
    }

    public String getNeighborId() {
        return neighborId;
    }

    @Override
    public String getPlayerId() {
        return playerId;
    }
}
