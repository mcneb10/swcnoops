package swcnoops.server.commands.player.response;

import swcnoops.server.model.BattleReplayResponse;
import swcnoops.server.requests.AbstractCommandResult;

public class PlayerBattleReplayGetResult extends AbstractCommandResult {
    private BattleReplayResponse battleReplayResponse;
    public PlayerBattleReplayGetResult(BattleReplayResponse battleReplayResponse) {
        this.battleReplayResponse = battleReplayResponse;
    }

    @Override
    public Object getResult() {
        return this.battleReplayResponse;
    }
}
