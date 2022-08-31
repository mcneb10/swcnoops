package swcnoops.server.commands.player.response;

import swcnoops.server.model.BattleReplay;
import swcnoops.server.requests.AbstractCommandResult;
import swcnoops.server.requests.ResponseHelper;

public class PlayerBattleReplayGetResult extends AbstractCommandResult {
    private BattleReplay battleReplay;
    private int statusCode;
    public PlayerBattleReplayGetResult(BattleReplay battleReplay) {
        this(ResponseHelper.RECEIPT_STATUS_COMPLETE);
        this.battleReplay = battleReplay;
    }

    public PlayerBattleReplayGetResult(int replayDataNotFound) {
        this.statusCode = replayDataNotFound;
    }

    @Override
    public Integer getStatus() {
        return this.statusCode;
    }

    @Override
    public Object getResult() {
        return this.battleReplay;
    }
}
