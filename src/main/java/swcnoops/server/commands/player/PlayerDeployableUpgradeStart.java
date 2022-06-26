package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerDeployableUpgradeStart extends AbstractCommandAction<PlayerDeployableUpgradeStart, CommandResult> {
    private String buildingId;
    private String troopUid;

    @Override
    protected CommandResult execute(PlayerDeployableUpgradeStart arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.deployableUpgradeStart(arguments.getBuildingId(), arguments.getTroopUid(), time);
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerDeployableUpgradeStart parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerDeployableUpgradeStart.class);
    }

    @Override
    public String getAction() {
        return "player.deployable.upgrade.start";
    }

    public String getBuildingId() {
        return buildingId;
    }

    public String getTroopUid() {
        return troopUid;
    }
}
