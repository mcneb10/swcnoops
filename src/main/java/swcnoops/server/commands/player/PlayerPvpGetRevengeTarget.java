package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.game.PvpMatch;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.session.PlayerSession;

public class PlayerPvpGetRevengeTarget extends AbstractCommandAction<PlayerPvpGetRevengeTarget, CommandResult> {
    private String opponentId;

    @Override
    protected CommandResult execute(PlayerPvpGetRevengeTarget arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId());
        PvpMatch pvpMatch = playerSession.getPvpSession().getRevengeMatch(arguments.getOpponentId(), time);

        CommandResult result = PlayerPvpGetNextTarget.setupResponse(playerSession, pvpMatch, time);
        return result;
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
