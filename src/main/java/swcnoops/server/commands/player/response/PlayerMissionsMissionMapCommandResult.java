package swcnoops.server.commands.player.response;

import swcnoops.server.requests.AbstractCommandResult;

public class PlayerMissionsMissionMapCommandResult extends AbstractCommandResult {
    public String battleId;

    public String getBattleId() {
        return battleId;
    }

    public void setBattleId(String battleId) {
        this.battleId = battleId;
    }
}
