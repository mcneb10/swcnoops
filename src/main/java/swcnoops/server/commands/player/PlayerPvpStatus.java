package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.datasource.PvpAttack;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerPvpStatus extends AbstractCommandAction<PlayerPvpStatus, CommandResult> {
    @Override
    protected CommandResult execute(PlayerPvpStatus arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId());

        playerSession.getCurrentPvPDefence().setDirty();
        PvpAttack pvpAttack = playerSession.getCurrentPvPDefence().getObjectForReading();

        CommandResult result;

        if (pvpAttack != null)
            result = ResponseHelper.newStatusResult(ResponseHelper.STATUS_CODE_PVP_TARGET_IS_UNDER_ATTACK);
        else
            result = ResponseHelper.newStatusResult(ResponseHelper.UNSYNCHRONIZED);

        return result;
    }

    @Override
    protected PlayerPvpStatus parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerPvpStatus.class);
    }

    @Override
    public String getAction() {
        return "player.pvp.status";
    }
}
