package swcnoops.server.model;

import java.util.List;
import java.util.Map;

public class BattleEntry {
    public String battleId;
    public long attackDate;
    public BattleParticipant attacker;
    public List<String> attackerEquipment;
    public Map<String, Integer> attackerGuildTroopsExpended;
    public int baseDamagePercent;
    public String battleVersion;
    public String cmsVersion;
    public BattleParticipant defender;
    public List<String> defenderEquipment;
    public Map<String, Integer> defenderGuildTroopsExpended;
    public long defenderPotentialMedalGain;
    public Earned earned;
    public Earned looted;
    public long manifestVersion;
    public Earned maxLootable;
    public String missionId;
    public Map<String, Integer> numVisitors;
    public String planetId;
    public long potentialMedalGain;
    public boolean revenged;
    public boolean server;
    public int stars;
    public Map<String, Integer> troopsExpended;
    public boolean isUserEnded;
}
