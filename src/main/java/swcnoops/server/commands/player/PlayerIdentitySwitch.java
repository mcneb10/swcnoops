package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerIdentitySwitchResult;
import swcnoops.server.json.JsonParser;

// TODO - to finish off
// need to do something in AuthPreauthGeneratePlayerWithFacebook and GeneratePlayer
public class PlayerIdentitySwitch extends AbstractCommandAction<PlayerIdentitySwitch, PlayerIdentitySwitchResult> {
    @Override
    protected PlayerIdentitySwitchResult execute(PlayerIdentitySwitch arguments, long time) throws Exception {
        return new PlayerIdentitySwitchResult(ServiceFactory.createRandomUUID());
    }

    @Override
    protected PlayerIdentitySwitch parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerIdentitySwitch.class);
    }

    @Override
    public String getAction() {
        return "player.identity.switch";
    }
}
