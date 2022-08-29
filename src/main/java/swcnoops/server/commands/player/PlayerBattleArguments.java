package swcnoops.server.commands.player;

import swcnoops.server.commands.CommandArguments;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;

public class PlayerBattleArguments extends PlayerBattleComplete {
    public PlayerBattleArguments() {
    }

    @Override
    protected CommandResult execute(CommandArguments arguments, long time) throws Exception {
        return null;
    }

    @Override
    protected CommandArguments parseArgument(JsonParser jsonParser, Object argumentObject) {
        return null;
    }

    @Override
    public String getAction() {
        return null;
    }
}
