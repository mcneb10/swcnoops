package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerBuildingCapture extends AbstractCommandAction<PlayerBuildingCapture, CommandResult> {

    /**
     * This is the building ID of the creature trap
     */
    private String instanceId;
    private String creatureTroopUid;

    @Override
    protected CommandResult execute(PlayerBuildingCapture arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.captureCreature(arguments.getInstanceId(), arguments.getCreatureTroopUid(), time);
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerBuildingCapture parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerBuildingCapture.class);
    }

    @Override
    public String getAction() {
        return "player.building.capture";
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getCreatureTroopUid() {
        return creatureTroopUid;
    }
}
