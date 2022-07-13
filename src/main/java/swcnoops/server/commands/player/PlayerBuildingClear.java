package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerBuildingClear extends AbstractCommandAction<PlayerBuildingClear, CommandResult> {
    private String instanceId;
    private boolean payWithHardCurrency;

    @Override
    protected CommandResult execute(PlayerBuildingClear arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.buildingClear(arguments.getInstanceId(), time);
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerBuildingClear parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerBuildingClear.class);
    }

    @Override
    public String getAction() {
        return "player.building.clear";
    }

    public String getInstanceId() {
        return instanceId;
    }
}
