package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerBattleReplayGetResult;
import swcnoops.server.datasource.WarBattle;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;
import swcnoops.server.requests.ResponseHelper;

public class PlayerBattleReplayGet extends AbstractCommandAction<PlayerBattleReplayGet, PlayerBattleReplayGetResult> {
    private String battleId;
    private String participantId;

    @Override
    protected PlayerBattleReplayGetResult execute(PlayerBattleReplayGet arguments, long time) throws Exception {
        BattleReplay battleReplay =
                ServiceFactory.instance().getPlayerDatasource().getBattleReplay(arguments.battleId);

        if (battleReplay != null) {
            switch (battleReplay.battleType) {
                case PvpAttackSquadWar:
                    stopClientCrash(battleReplay);
                    break;
                default:
                    break;
            }
        }

        if (battleReplay.replayData == null)
            return new PlayerBattleReplayGetResult(ResponseHelper.REPLAY_DATA_NOT_FOUND);
        return new PlayerBattleReplayGetResult(battleReplay);
    }

    private void stopClientCrash(BattleReplay battleReplay) {
        // we force it to be a PvP replay as for some reason it can crash on iOS but fine on the others
        battleReplay.replayData.battleType = BattleType.Pvp;

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
