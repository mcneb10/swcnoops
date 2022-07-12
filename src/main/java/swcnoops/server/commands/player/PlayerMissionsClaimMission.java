package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerMissionsClaimMission extends AbstractCommandAction<PlayerMissionsClaimMission, CommandResult> {
    private String missionUid;

    @Override
    protected CommandResult execute(PlayerMissionsClaimMission arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.missionsClaimMission(arguments.getMissionUid(), time);
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerMissionsClaimMission parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerMissionsClaimMission.class);
    }

    @Override
    public String getAction() {
        return "player.missions.claimMission";
    }

    public String getMissionUid() {
        return missionUid;
    }
}
