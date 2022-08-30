package swcnoops.server.datasource;

import swcnoops.server.model.BattleReplay;

import java.util.ArrayList;

public class WarBattle {
    private String battleId;
    private String attackerId;
    private String defenderId;
    private BattleReplay battleResponse;
    private long battleCompleteTime;

    public WarBattle(String battleId, String attackerId, String defenderId, BattleReplay battleReplay,
                     long battleCompleteTime)
    {
        this.battleId = battleId;
        this.attackerId = attackerId;
        this.defenderId = defenderId;
        this.battleResponse = battleReplay;
        this.battleCompleteTime = battleCompleteTime;

        fixNullProperties(this.battleResponse);
    }

    private void fixNullProperties(BattleReplay battleReplay) {
        if (battleReplay.replayData.defenderCreatureTraps == null)
            battleReplay.replayData.defenderCreatureTraps = new ArrayList<>();

        if (battleReplay.replayData.attackerCreatureTraps == null)
            battleReplay.replayData.attackerCreatureTraps = new ArrayList<>();
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

    public BattleReplay getBattleReplay() {
        return battleResponse;
    }

    public long getBattleCompleteTime() {
        return battleCompleteTime;
    }
}
