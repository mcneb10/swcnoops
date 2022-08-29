package swcnoops.server.datasource;

import swcnoops.server.commands.player.PlayerBattleComplete;

import java.util.ArrayList;

public class WarBattle {
    private String battleId;
    private String attackerId;
    private String defenderId;
    private PlayerBattleComplete battleResponse;
    private long battleCompleteTime;

    public WarBattle(String battleId, String attackerId, String defenderId, PlayerBattleComplete battleResponse,
                     long battleCompleteTime)
    {
        this.battleId = battleId;
        this.attackerId = attackerId;
        this.defenderId = defenderId;
        this.battleResponse = battleResponse;
        this.battleCompleteTime = battleCompleteTime;

        fixNullProperties(this.battleResponse);
    }

    private void fixNullProperties(PlayerBattleComplete battleResponse) {
        if (battleResponse.getReplayData().defenderCreatureTraps == null)
            battleResponse.getReplayData().defenderCreatureTraps = new ArrayList<>();

        if (battleResponse.getReplayData().attackerCreatureTraps == null)
            battleResponse.getReplayData().attackerCreatureTraps = new ArrayList<>();
    }

    public String getBattleId() {
        return battleId;
    }

    public String getAttackerId() {
        return attackerId;
    }

    public String getDefenderId() {
        return defenderId;
    }

    public PlayerBattleComplete getBattleResponse() {
        return battleResponse;
    }

    public long getBattleCompleteTime() {
        return battleCompleteTime;
    }
}
