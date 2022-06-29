package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.PositionMap;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerBuildingMultimove extends AbstractCommandAction<PlayerBuildingMultimove, CommandResult> {
    private PositionMap positions;

    @Override
    protected CommandResult execute(PlayerBuildingMultimove arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.buildingMultimove(arguments.getPositions(), time);
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerBuildingMultimove parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerBuildingMultimove.class);
    }

    @Override
    public String getAction() {
        return "player.building.multimove";
    }

    public PositionMap getPositions() {
        return positions;
    }
}
