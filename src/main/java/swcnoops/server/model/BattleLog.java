package swcnoops.server.model;

public class BattleLog {
    public String battleId;
    public BattleParticipant attacker;
    public BattleParticipant defender;
    public String missionId;
    public Long attackDate;
    public Earned looted;
    public Earned earned;
    public Earned maxLootable;
    public JsonStringIntegerMap troopsExpended;
    public JsonStringIntegerMap attackerGuildTroopsExpended;
    public JsonStringIntegerMap defenderGuildTroopsExpended;
    public JsonStringIntegerMap numVisitors;
    public int baseDamagePercent;
    public int stars;
    public Long manifestVersion;
    public int potentialMedalGain;
    public int defenderPotentialMedalGain;
    public boolean revenged;
    public String battleVersion;
    public String cmsVersion;
    public boolean server;
    public JsonStringArrayList attackerEquipment;
    public JsonStringArrayList defenderEquipment;
    public String planetId;
}
