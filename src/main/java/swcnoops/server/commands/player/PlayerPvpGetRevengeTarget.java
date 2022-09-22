package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PvpTargetCommandResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;

public class PlayerPvpGetRevengeTarget extends AbstractCommandAction<PlayerPvpGetRevengeTarget, CommandResult> {
    private String opponentId;

    @Override
    protected CommandResult execute(PlayerPvpGetRevengeTarget arguments, long time) throws Exception {
        // TODO - check player status to see if it can be attacked and return a
        // PvpTargetCommandResult
        return ResponseHelper.newErrorResult(ResponseHelper.STATUS_CODE_PVP_TARGET_NOT_FOUND);
    }

    @Override
    protected PlayerPvpGetRevengeTarget parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerPvpGetRevengeTarget.class);
    }

    @Override
    public String getAction() {
        return "player.pvp.getRevengeTarget";
    }

    public String getOpponentId() {
        return opponentId;
    }
}
