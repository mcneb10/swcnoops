package swcnoops.server.game;

import swcnoops.server.model.CurrencyType;
import swcnoops.server.model.FactionType;

public class BuildingData implements GameData {
    final private String uid;
    private FactionType faction;
    private int level;
    private BuildingType type;
    private int storage;
    private int time;
    private String buildingID;
    private String trapId;
    private String linkedUnit;
    private StoreTab storeTab;
    private BuildingSubType subType;
    private long crossTime;
    private CurrencyType currency;
    private float produce;
    private float cycleTime;

    public BuildingData(String uid) {
        this.uid = uid;
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

    public void setType(BuildingType type) {
        this.type = type;
    }

    public BuildingType getType() {
        return type;
    }

    protected void setStorage(int storage) {
        this.storage = storage;
    }

    public int getStorage() {
        return storage;
    }

    protected void setTime(int time) {
        this.time = time;
    }

    public int getTime() {
        return time;
    }

    public String getBuildingID() {
        return buildingID;
    }

    protected void setBuildingID(String buildingID) {
        this.buildingID = buildingID;
    }
    @Override
    public String getUid() {
        return this.uid;
    }

    public void setTrapId(String trapId) {
        this.trapId = trapId;
    }

    public String getTrapId() {
        return trapId;
    }

    public String getLinkedUnit() {
        return linkedUnit;
    }

    public void setLinkedUnit(String linkedUnit) {
        this.linkedUnit = linkedUnit;
    }

    public void setStoreTab(StoreTab storeTab) {
        this.storeTab = storeTab;
    }

    public StoreTab getStoreTab() {
        return storeTab;
    }

    public BuildingSubType getSubType() {
        return subType;
    }

    public void setSubType(BuildingSubType subType) {
        this.subType = subType;
    }

    public long getCrossTime() {
        return crossTime;
    }

    public void setCrossTime(long crossTime) {
        this.crossTime = crossTime;
    }

    public void setCurrency(CurrencyType currency) {
        this.currency = currency;
    }

    public CurrencyType getCurrency() {
        return currency;
    }

    public void setProduce(float produce) {
        this.produce = produce;
    }

    public float getProduce() {
        return produce;
    }

    public void setCycleTime(float cycleTime) {
        this.cycleTime = cycleTime;
    }

    public float getCycleTime() {
        return cycleTime;
    }
}
