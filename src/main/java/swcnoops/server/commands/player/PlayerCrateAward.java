package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;

// TODO - needs finishing
public class PlayerCrateAward extends AbstractCommandAction<PlayerCrateAward, CommandResult> {
    private String crateUid;

    @Override
    protected CommandResult execute(PlayerCrateAward arguments, long time) throws Exception {
        // TODO - probably needs to remove the crate and transfer content to player
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerCrateAward parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerCrateAward.class);
    }

    @Override
    public String getAction() {
        return "player.crate.award";
    }

    public String getCrateUid() {
        return crateUid;
    }
}
