package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerDeviceRegisterResult;
import swcnoops.server.json.JsonParser;

public class PlayerDeviceRegister extends AbstractCommandAction<PlayerDeviceRegister, PlayerDeviceRegisterResult> {

    @Override
    final public String getAction() {
        return "player.device.register";
    }

    @Override
    protected PlayerDeviceRegister parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerDeviceRegister.class);
    }

    @Override
    protected PlayerDeviceRegisterResult execute(PlayerDeviceRegister arguments) throws Exception {
        PlayerDeviceRegisterResult commandResult = new PlayerDeviceRegisterResult();
        return commandResult;
    }
}
