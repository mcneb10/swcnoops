package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerBuildingUpgrade extends PlayerChecksum<PlayerBuildingUpgrade, CommandResult> {
    private String instanceId;
    private String tag;

    @Override
    protected CommandResult execute(PlayerBuildingUpgrade arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.buildingUpgrade(arguments.getInstanceId(), arguments.getTag(), time);
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerBuildingUpgrade parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerBuildingUpgrade.class);
    }

    @Override
    public String getAction() {
        return "player.building.upgrade";
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getTag() {
        return tag;
    }
}
