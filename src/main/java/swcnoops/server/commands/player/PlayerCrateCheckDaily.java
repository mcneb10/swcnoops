package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerCrateCheckDailyCommandResult;
import swcnoops.server.json.JsonParser;

public class PlayerCrateCheckDaily extends AbstractCommandAction<PlayerCrateCheckDaily, PlayerCrateCheckDailyCommandResult> {
    @Override
    protected PlayerCrateCheckDailyCommandResult execute(PlayerCrateCheckDaily arguments, long time) throws Exception {
        return new PlayerCrateCheckDailyCommandResult();
    }

    @Override
    protected PlayerCrateCheckDaily parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerCrateCheckDaily.class);
    }

    @Override
    public String getAction() {
        return "player.crate.checkDaily";
    }
}
