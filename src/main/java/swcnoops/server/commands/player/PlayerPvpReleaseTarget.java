package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerPvpReleaseTarget extends AbstractCommandAction<PlayerPvpReleaseTarget, CommandResult> {
    @Override
    protected CommandResult execute(PlayerPvpReleaseTarget arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId());
        playerSession.pvpReleaseTarget();
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerPvpReleaseTarget parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerPvpReleaseTarget.class);
    }

    @Override
    public String getAction() {
        return "player.pvp.releaseTarget";
    }
}
