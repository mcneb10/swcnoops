package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerPvpBattleCompleteCommandResult;
import swcnoops.server.json.JsonParser;

public class PlayerPvpBattleComplete extends AbstractCommandAction<PlayerPvpBattleComplete, PlayerPvpBattleCompleteCommandResult> {
    @Override
    protected PlayerPvpBattleCompleteCommandResult execute(PlayerPvpBattleComplete arguments, long time) throws Exception {
        PlayerPvpBattleCompleteCommandResult response =
                ServiceFactory.instance().getJsonParser()
                        .toObjectFromResource("templates/playerPvpBattleComplete.json", PlayerPvpBattleCompleteCommandResult.class);

        response.attackerTournament.uid = ServiceFactory.createRandomUUID();
        return response;
    }

    @Override
    protected PlayerPvpBattleComplete parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerPvpBattleComplete.class);
    }

    @Override
    public String getAction() {
        return "player.pvp.battle.complete";
    }
}
