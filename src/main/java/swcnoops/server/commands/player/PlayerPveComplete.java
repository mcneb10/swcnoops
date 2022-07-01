package swcnoops.server.commands.player;

import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;

public class PlayerPveComplete extends PlayerBattleComplete<PlayerPveComplete, CommandResult> {
    @Override
    protected CommandResult execute(PlayerPveComplete arguments, long time) throws Exception {
        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerPveComplete parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerPveComplete.class);
    }

    @Override
    public String getAction() {
        return "player.pve.complete";
    }
}
