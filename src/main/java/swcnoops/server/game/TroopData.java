package swcnoops.server.game;

import swcnoops.server.model.FactionType;
import swcnoops.server.model.TroopType;

/**
 * This is used to hold Troops and SpecialAttacks merged together.
 * Not all properties will exists, as specialAttacks dont have troopType etc...
 */
public class TroopData implements GameData {
    final private String uid;
    private FactionType faction;
    private int level;
    private String unitId;
    private TroopType type;
    private int size;
    private long trainingTime;
    private String upgradeShardUid;
    private int upgradeShards;
    private long upgradeTime;
    private String specialAttackID;
    private int upgradeCredits;
    private int upgradeMaterials;
    private int upgradeContraband;
    private int credits;
    private int materials;
    private int contraband;

    public TroopData(String uid) {
        this.uid = uid;
    }
    @Override
    public String getUid() {
        return uid;
    }

    protected void setFaction(FactionType faction) {
        this.faction = faction;
    }

    public FactionType getFaction() {
        return faction;
    }

    protected void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    protected void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getUnitId() {
        return unitId;
    }

    protected void setType(TroopType type) {
        this.type = type;
    }

    protected void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    protected void setTrainingTime(long trainingTime) {
        this.trainingTime = trainingTime;
    }

    public long getTrainingTime() {
        return trainingTime;
    }

    protected void setUpgradeShardUid(String upgradeShardUid) {
        this.upgradeShardUid = upgradeShardUid;
    }

    public String getUpgradeShardUid() {
        return upgradeShardUid;
    }

    protected void setUpgradeShards(int upgradeShards) {
        this.upgradeShards = upgradeShards;
    }

    public int getUpgradeShards() {
        return upgradeShards;
    }

    public void setUpgradeTime(long upgradeTime) {
        this.upgradeTime = upgradeTime;
    }

    public long getUpgradeTime() {
        return upgradeTime;
    }

    public TroopType getType() {
        return this.type;
    }

    public void setSpecialAttackID(String specialAttackID) {
        this.specialAttackID = specialAttackID;
    }

    public String getSpecialAttackID() {
        return specialAttackID;
    }

    public boolean isSpecialAttack() {
        return this.getSpecialAttackID() != null;
    }

    public void setUpgradeCredits(int upgradeCredits) {
        this.upgradeCredits = upgradeCredits;
    }

    public int getUpgradeCredits() {
        return upgradeCredits;
    }

    public void setUpgradeMaterials(int upgradeMaterials) {
        this.upgradeMaterials = upgradeMaterials;
    }

    public int getUpgradeMaterials() {
        return upgradeMaterials;
    }

    public void setUpgradeContraband(int upgradeContraband) {
        this.upgradeContraband = upgradeContraband;
    }

    public int getUpgradeContraband() {
        return upgradeContraband;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public int getCredits() {
        return credits;
    }

    public void setMaterials(int materials) {
        this.materials = materials;
    }

    public int getMaterials() {
        return materials;
    }

    public void setContraband(int contraband) {
        this.contraband = contraband;
    }

    public int getContraband() {
        return contraband;
    }
}
