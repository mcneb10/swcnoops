package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerNeighborVisitResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;
import swcnoops.server.session.PlayerSession;

public class PlayerNeighborVisit extends AbstractCommandAction<PlayerNeighborVisit, PlayerNeighborVisitResult> {
    private String neighborId;
    private String playerId;

    @Override
    protected PlayerNeighborVisitResult execute(PlayerNeighborVisit arguments, long time) throws Exception {
        PlayerSession neighborSession =
                ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getNeighborId());

        PlayerNeighborVisitResult playerNeighborVisitResult = new PlayerNeighborVisitResult();
        if (neighborSession != null) {
            playerNeighborVisitResult.player = new Player();
            playerNeighborVisitResult.player.name = neighborSession.getPlayerSettings().getName();

            // TODO - to finish
            //playerNeighborVisitResult.player.scalars = new Object();
            playerNeighborVisitResult.player.playerModel = new PlayerModel();
            playerNeighborVisitResult.player.playerModel.faction = neighborSession.getFaction();
            playerNeighborVisitResult.player.playerModel.map = neighborSession.getPlayer().getPlayerSettings().baseMap;
            playerNeighborVisitResult.player.playerModel.inventory = new Inventory();
            playerNeighborVisitResult.player.playerModel.inventory.capacity = -1;
            playerNeighborVisitResult.player.playerModel.inventory.storage = neighborSession.getPlayerSettings().getInventoryStorage();
            playerNeighborVisitResult.player.playerModel.inventory.subStorage = new SubStorage();

            if (neighborSession.getGuildSession() != null) {
                playerNeighborVisitResult.player.playerModel.guildInfo = new GuildInfo();
                playerNeighborVisitResult.player.playerModel.guildInfo.guildName = neighborSession.getGuildSession().getGuildName();
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
