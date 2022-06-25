package swcnoops.server.game;

public class TroopData implements GameData {
    final private String uid;
    private String faction;
    private int level;
    private String unitId;
    private String type;
    private int size;
    private long trainingTime;
    private String upgradeShardUid;
    private int upgradeShards;
    private long upgradeTime;

    public TroopData(String uid) {
        this.uid = uid;
    }
    @Override
    public String getUid() {
        return uid;
    }

    protected void setFaction(String faction) {
        this.faction = faction;
    }

    public String getFaction() {
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

    protected void setType(String type) {
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
}
