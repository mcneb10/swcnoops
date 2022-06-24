package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerBuildingBuyout extends AbstractCommandAction<PlayerBuildingBuyout, CommandResult> {
    private String instanceId;
    private String tag;

    @Override
    protected CommandResult execute(PlayerBuildingBuyout arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.buildingBuyout(arguments.getInstanceId(), arguments.getTag(), time);
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerBuildingBuyout parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerBuildingBuyout.class);
    }

    @Override
    public String getAction() {
        return "player.building.buyout";
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getTag() {
        return tag;
    }
}
