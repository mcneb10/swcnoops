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
        // TODO - support PvP replays, only war for now

        BattleType battleType = ServiceFactory.instance().getPlayerDatasource().getBattleType(arguments.battleId);

        BattleReplay battleReplay = null;

        switch (battleType) {
            case Pvp:
                battleReplay = ServiceFactory.instance().getPlayerDatasource().pvpReplay(arguments.battleId);
                break;
            case PvpAttackSquadWar:
                WarBattle warBattle = ServiceFactory.instance().getPlayerDatasource().getWarBattle(arguments.getBattleId());
                battleReplay = warBattle.getBattleReplay();
                stopClientCrash(battleReplay);
                break;
            default:
                return new PlayerBattleReplayGetResult(ResponseHelper.REPLAY_DATA_NOT_FOUND);
        }

        if (battleReplay.replayData == null)
            return new PlayerBattleReplayGetResult(ResponseHelper.REPLAY_DATA_NOT_FOUND);
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
