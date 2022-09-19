package swcnoops.server.game;

import swcnoops.server.model.BattleParticipant;
import swcnoops.server.model.FactionType;
import swcnoops.server.model.JsonStringArrayList;

public class PvpMatch {
    private String battleId;
    private String playerId;
    private String participantId;
    private String name;
    private long battleDate;

    private FactionType factionType;
    private boolean isDevBase;
    private int level;
    private int defenderXp;

    private int potentialScoreWin;
    private int potentialScoreLose;
    private BattleParticipant attacker;
    private BattleParticipant defender;

    private JsonStringArrayList attackerEquipment;
    private JsonStringArrayList defenderEquipment;
    private String guildName;
    private String guildId;


    public PvpMatch() {
    }

    public String getBattleId() {
        return battleId;
    }

    public void setBattleId(String battleId) {
        this.battleId = battleId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getBattleDate() {
        return battleDate;
    }

    public void setBattleDate(long battleDate) {
        this.battleDate = battleDate;
    }

    public FactionType getFactionType() {
        return factionType;
    }

    public void setFactionType(FactionType factionType) {
        this.factionType = factionType;
    }

    public boolean isDevBase() {
        return isDevBase;
    }

    public void setDevBase(boolean devBase) {
        isDevBase = devBase;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getDefenderXp() {
        return defenderXp;
    }

    public void setDefenderXp(int defenderXp) {
        this.defenderXp = defenderXp;
    }

    public BattleParticipant getAttacker() {
        return attacker;
    }

    public void setAttacker(BattleParticipant attacker) {
        this.attacker = attacker;
    }

    public BattleParticipant getDefender() {
        return defender;
    }

    public void setDefender(BattleParticipant defender) {
        this.defender = defender;
    }

    public int getPotentialScoreWin() {
        return potentialScoreWin;
    }

    public void setPotentialScoreWin(int potentialScoreWin) {
        this.potentialScoreWin = potentialScoreWin;
    }

    public int getPotentialScoreLose() {
        return potentialScoreLose;
    }

    public void setPotentialScoreLose(int potentialScoreLose) {
        this.potentialScoreLose = potentialScoreLose;
    }

    public void setAttackerDelta(int delta) {
        this.attacker.attackRatingDelta = delta;
    }

    public void setDefenderDelta(int delta) {
        this.defender.defenseRatingDelta = delta;
    }

    public JsonStringArrayList getAttackerEquipment() {
        return attackerEquipment;
    }

    public void setAttackerEquipment(JsonStringArrayList attackerEquipment) {
        this.attackerEquipment = attackerEquipment;
    }

    public JsonStringArrayList getDefenderEquipment() {
        return defenderEquipment;
    }

    public void setDefenderEquipment(JsonStringArrayList defenderEquipment) {
        this.defenderEquipment = defenderEquipment;
    }

    public String getGuildName() {
        return guildName;
    }

    public void setGuildName(String guildName) {
        this.guildName = guildName;
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }
}



