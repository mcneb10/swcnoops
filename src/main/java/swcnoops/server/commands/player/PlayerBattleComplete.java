package swcnoops.server.commands.player;

import swcnoops.server.model.ReplayData;
import swcnoops.server.requests.CommandResult;

import java.util.List;
import java.util.Map;

abstract public class PlayerBattleComplete<A extends PlayerBattleComplete, B extends CommandResult>
        extends PlayerChecksum<A,B>
{
    private String cmsVersion;
    private String battleVersion;
    private String battleId;
    private String battleUid;
    private Map<String,Integer> attackingUnitsKilled;
    private Map<String,Integer> defenderGuildTroopsSpent;
    private Map<String,Integer> attackerGuildTroopsSpent;
    private Map<String,Integer> seededTroopsDeployed;
    private Map<String,Integer> defendingUnitsKilled;
    private Map<String,Integer> damagedBuildings;
    private List<String> unarmedTraps;
    private int baseDamagePercent;
    private Map<String,Integer> numVisitors;
    private int stars;
    private String planetId;
    private ReplayData replayData;
    private boolean isUserEnded;

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

    public String getCmsVersion() {
        return cmsVersion;
    }

    public String getBattleVersion() {
        return battleVersion;
    }

    public int getBaseDamagePercent() {
        return baseDamagePercent;
    }
}
