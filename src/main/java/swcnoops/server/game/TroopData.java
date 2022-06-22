package swcnoops.server.game;

public class TroopData implements BuildableData {
    final private String uid;
    private String faction;
    private int level;
    private String unitId;
    private String type;
    private int size;
    private int trainingTime;
    private String upgradeShardUid;
    private int upgradeShards;

    public TroopData(String uid) {
        this.uid = uid;
    }

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

    public String getType() {
        return type;
    }

    protected void setSize(int size) {
        this.size = size;
    }

    @Override
    public int getSize() {
        return size;
    }

    protected void setTrainingTime(int trainingTime) {
        this.trainingTime = trainingTime;
    }

    public int getTrainingTime() {
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

    @Override
    public long getBuildingTime() {
        return this.getTrainingTime();
    }
}
