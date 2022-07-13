package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;

// TODO - to finish, return proper response
public class PlayerIdentityGet extends AbstractCommandAction<PlayerIdentityGet, CommandResult> {
    private int identityIndex;

    @Override
    protected CommandResult execute(PlayerIdentityGet arguments, long time) throws Exception {
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerIdentityGet parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerIdentityGet.class);
    }

    @Override
    public String getAction() {
        return "player.identity.get";
    }

    public int getIdentityIndex() {
        return identityIndex;
    }
}
