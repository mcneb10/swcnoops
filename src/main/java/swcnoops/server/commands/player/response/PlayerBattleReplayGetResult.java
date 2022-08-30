package swcnoops.server.commands.player.response;

import swcnoops.server.model.BattleReplay;
import swcnoops.server.requests.AbstractCommandResult;

public class PlayerBattleReplayGetResult extends AbstractCommandResult {
    private BattleReplay battleReplay;
    public PlayerBattleReplayGetResult(BattleReplay battleReplay) {
        this.battleReplay = battleReplay;
    }

    @Override
    public Object getResult() {
        return this.battleReplay;
    }
}
