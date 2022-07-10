package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerMissionsActivateMission extends AbstractCommandAction<PlayerMissionsActivateMission, CommandResult> {
    private String missionUid;
    private String battleUid;

    @Override
    protected CommandResult execute(PlayerMissionsActivateMission arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.activateMission(arguments.getMissionUid(), time);
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerMissionsActivateMission parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerMissionsActivateMission.class);
    }

    @Override
    public String getAction() {
        return "player.missions.activateMission";
    }

    public String getMissionUid() {
        return missionUid;
    }

    public String getBattleUid() {
        return battleUid;
    }
}
