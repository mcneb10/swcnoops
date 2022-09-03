package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerBuildingSwap extends PlayerChecksum<PlayerBuildingSwap, CommandResult> {
    private String buildingId;
    private String buildingUid;

    @Override
    protected CommandResult execute(PlayerBuildingSwap arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.buildingSwap(arguments.getBuildingId(), arguments.getBuildingUid(),
                arguments.getCredits(), arguments.getMaterials(), arguments.getContraband(),
                time);

        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerBuildingSwap parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerBuildingSwap.class);
    }

    @Override
    public String getAction() {
        return "player.building.swap";
    }

    public String getBuildingId() {
        return buildingId;
    }

    public String getBuildingUid() {
        return buildingUid;
    }
}
