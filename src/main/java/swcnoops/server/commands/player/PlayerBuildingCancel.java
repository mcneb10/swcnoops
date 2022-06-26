package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerBuildingCancel extends AbstractCommandAction<PlayerBuildingCancel, CommandResult> {
    private String instanceId;
    private String tag;

    @Override
    protected CommandResult execute(PlayerBuildingCancel arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.buildingCancel(arguments.getInstanceId(), arguments.getTag(), time);
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerBuildingCancel parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerBuildingCancel.class);
    }

    @Override
    public String getAction() {
        return "player.building.cancel";
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getTag() {
        return tag;
    }
}
