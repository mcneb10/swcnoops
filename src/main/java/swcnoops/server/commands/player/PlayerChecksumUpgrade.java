package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerChecksumUpgrade extends PlayerChecksum<PlayerChecksumUpgrade, CommandResult> {
    private String instanceId;
    private String tag;

    @Override
    protected CommandResult execute(PlayerChecksumUpgrade arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.buildingUpgrade(arguments.getInstanceId(), time);
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerChecksumUpgrade parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerChecksumUpgrade.class);
    }

    @Override
    public String getAction() {
        return "player.building.upgrade";
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getTag() {
        return tag;
    }
}
