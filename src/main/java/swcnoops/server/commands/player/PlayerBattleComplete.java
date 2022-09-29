package swcnoops.server.commands.player;

import swcnoops.server.model.CreatureTrapData;
import swcnoops.server.model.CurrencyType;
import swcnoops.server.model.JsonStringIntegerMap;
import swcnoops.server.model.ReplayData;
import swcnoops.server.requests.CommandResult;

import java.util.List;
import java.util.Map;

abstract public class PlayerBattleComplete<A extends PlayerBattleComplete, B extends CommandResult>
        extends PlayerChecksum<A, B> {
    private long cs;
    private String cmsVersion;
    private String battleVersion;
    private String battleId;
    private String battleUid;
    private JsonStringIntegerMap attackingUnitsKilled;
    private JsonStringIntegerMap defenderGuildTroopsSpent;
    private JsonStringIntegerMap attackerGuildTroopsSpent;
    private JsonStringIntegerMap seededTroopsDeployed;
    private JsonStringIntegerMap defendingUnitsKilled;
    private Map<CurrencyType, Integer> loot;
    private Map<String, Integer> damagedBuildings;
    private List<String> unarmedTraps;
    private int baseDamagePercent;
    private JsonStringIntegerMap numVisitors;
    private int stars;
    private String planetId;
    private ReplayData replayData;
    private boolean isUserEnded; //If the user cancelled the battle before the end. Aka rage quit! :p
    private List<CreatureTrapData> defenderCreatureTraps;

    public JsonStringIntegerMap getAttackerGuildTroopsSpent() {
        return attackerGuildTroopsSpent;
    }

    public String getBattleId() {
        return battleId;
    }

    public String getBattleUid() {
        return battleUid;
    }

    public JsonStringIntegerMap getAttackingUnitsKilled() {
        return attackingUnitsKilled;
    }

    public JsonStringIntegerMap getDefenderGuildTroopsSpent() {
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


    public long getCs() {
        return cs;
    }

    public Map<String, Integer> getSeededTroopsDeployed() {
        return seededTroopsDeployed;
    }

    public JsonStringIntegerMap getDefendingUnitsKilled() {
        return defendingUnitsKilled;
    }

    public Map<String, Integer> getDamagedBuildings() {
        return damagedBuildings;
    }

    public List<String> getUnarmedTraps() {
        return unarmedTraps;
    }

    public JsonStringIntegerMap getNumVisitors() {
        return numVisitors;
    }

    public boolean getIsUserEnded() {
        return isUserEnded;
    }

    public void setIsUserEnded(boolean userEnded) {
        isUserEnded = userEnded;
    }

    public Map<CurrencyType, Integer> getLoot() {
        return loot;
    }

    public List<CreatureTrapData> getDefenderCreatureTraps() {
        return defenderCreatureTraps;
    }

    public void setDefenderCreatureTraps(List<CreatureTrapData> defenderCreatureTraps) {
        this.defenderCreatureTraps = defenderCreatureTraps;
    }
}
