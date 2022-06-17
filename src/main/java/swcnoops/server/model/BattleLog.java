package swcnoops.server.model;

import java.util.List;
import java.util.Map;

public class BattleLog {
    public Long attackDate;
    public Attacker attacker;
    public List<String> attackerEquipment;
    public Map<String, Long> attackerGuildTroopsExpended;
    public Long baseDamagePercent;
    public String battleId;
    public String battleVersion;
    public String cmsVersion;
    public Defender defender;
    public List<String> defenderEquipment;
    public Map<String, Long> defenderGuildTroopsExpended;
    public Long defenderPotentialMedalGain;
    public Earned earned;
    public Earned looted;
    public Long manifestVersion;
    public Earned maxLootable;
    public String missionId;
    public Map<String, Long> numVisitors;
    public String planetId;
    public Long potentialMedalGain;
    public boolean revenged;
    public boolean server;
    public Long stars;
    public Map<String, Long> troopsExpended;
}
