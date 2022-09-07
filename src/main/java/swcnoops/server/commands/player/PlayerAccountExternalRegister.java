package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerAccountExternalRegisterResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.PlayerIdentityInfo;
import swcnoops.server.requests.ResponseHelper;

public class PlayerAccountExternalRegister extends AbstractCommandAction<PlayerAccountExternalRegister, PlayerAccountExternalRegisterResult> {

    @Override
    protected PlayerAccountExternalRegisterResult execute(PlayerAccountExternalRegister arguments, long time) throws Exception {
        PlayerAccountExternalRegisterResult result = new PlayerAccountExternalRegisterResult();
        PlayerIdentityInfo playerIdentityInfo = new PlayerIdentityInfo();
//        playerIdentityInfo.playerId = "dsadas";
//        playerIdentityInfo.name = "sadas";
//        result.derivedExternalAccountId = "asdsad";
//        result.identities.put("asda", playerIdentityInfo);

        // TODO - it looks like if we set it to this value it will force the client to detect that it needs to recover
        //result.setReturnCode(ResponseHelper.STATUS_CODE_ALREADY_REGISTERED);

        return result;
    }

    @Override
    protected PlayerAccountExternalRegister parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerAccountExternalRegister.class);
    }

    @Override
    public String getAction() {
        return "player.account.external.register";
    }
}
