package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.player.response.PlayerPvpBattleCompleteCommandResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.session.PlayerSession;

public class PlayerPvpBattleComplete extends PlayerBattleComplete<PlayerPvpBattleComplete, PlayerPvpBattleCompleteCommandResult> {
    @Override
    protected PlayerPvpBattleCompleteCommandResult execute(PlayerPvpBattleComplete arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        playerSession.battleComplete(arguments.getBattleId(), arguments.getStars(), arguments.getAttackingUnitsKilled(), time);

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
