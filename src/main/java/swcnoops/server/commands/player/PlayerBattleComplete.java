package swcnoops.server.commands.player;

import swcnoops.server.model.ReplayData;
import swcnoops.server.requests.CommandResult;

import java.util.Map;

abstract public class PlayerBattleComplete<A extends PlayerBattleComplete, B extends CommandResult>
        extends PlayerChecksum<A,B>
{
    private String battleId;
    private String battleUid;
    private Map<String,Integer> attackingUnitsKilled;
    private Map<String,Integer> defenderGuildTroopsSpent;
    private Map<String,Integer> attackerGuildTroopsSpent;
    private int stars;
    private String planetId;
    private ReplayData replayData;

    public Map<String, Integer> getAttackerGuildTroopsSpent() {
        return attackerGuildTroopsSpent;
    }

    public String getBattleId() {
        return battleId;
    }

    public String getBattleUid() {
        return battleUid;
    }

    public Map<String, Integer> getAttackingUnitsKilled() {
        return attackingUnitsKilled;
    }

    public Map<String, Integer> getDefenderGuildTroopsSpent() {
        return defenderGuildTroopsSpent;
    }

    public int getStars() {
        return stars;
    }

    public String getPlanetId() {
        return planetId;
    }

    public ReplayData getReplayData() {
        return replayData;
    }
}
