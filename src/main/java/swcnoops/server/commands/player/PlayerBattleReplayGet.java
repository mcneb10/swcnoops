package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerBattleReplayGetResult;
import swcnoops.server.datasource.WarBattle;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;

public class PlayerBattleReplayGet extends AbstractCommandAction<PlayerBattleReplayGet, PlayerBattleReplayGetResult> {
    private String battleId;
    private String participantId;

    @Override
    protected PlayerBattleReplayGetResult execute(PlayerBattleReplayGet arguments, long time) throws Exception {
        WarBattle warBattle = ServiceFactory.instance().getPlayerDatasource().getWarBattle(arguments.getBattleId());

        BattleReplay battleReplay = warBattle.getBattleReplay();
        stopClientCrash(battleReplay);
        return new PlayerBattleReplayGetResult(battleReplay);
    }

    private void stopClientCrash(BattleReplay battleReplay) {
        // client does not really support war replays, it will crash if the attacker won stars
        // so for replay we make it 0 stars
        if (battleReplay.replayData.battleType == BattleType.PvpAttackSquadWar)
            battleReplay.battleLog.stars = 0;
    }

    @Override
    protected PlayerBattleReplayGet parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerBattleReplayGet.class);
    }

    @Override
    public String getAction() {
        return "player.battle.replay.get";
    }

    public String getBattleId() {
        return battleId;
    }

    public String getParticipantId() {
        return participantId;
    }
}
