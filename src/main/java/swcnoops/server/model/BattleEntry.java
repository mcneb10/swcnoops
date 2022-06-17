package swcnoops.server.model;

import java.util.List;
import java.util.Map;

public class BattleEntry {
    public long attackDate;
    public Attacker attacker;
    public List<String> attackerEquipment;
    public Map<String, Long> attackerGuildTroopsExpended;
    public long baseDamagePercent;
    public String battleId;
    public String battleVersion;
    public String cmsVersion;
    public Defender defender;
    public List<String> defenderEquipment;
    public Map<String, Long> defenderGuildTroopsExpended;
    public long defenderPotentialMedalGain;
    public Earned earned;
    public Earned looted;
    public long manifestVersion;
    public Earned maxLootable;
    public String missionId;
    public Map<String, Long> numVisitors;
    public String planetId;
    public long potentialMedalGain;
    public boolean revenged;
    public boolean server;
    public long stars;
    public Map<String, Long> troopsExpended;
}
