package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;

public class PlayerDeviceDeregister extends AbstractCommandAction<PlayerDeviceDeregister, CommandResult> {
    private String deviceToken;
    private String deviceType;


    @Override
    protected CommandResult execute(PlayerDeviceDeregister arguments, long time) throws Exception {
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerDeviceDeregister parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerDeviceDeregister.class);
    }

    @Override
    public String getAction() {
        return "player.device.deregister";
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public String getDeviceType() {
        return deviceType;
    }
}
