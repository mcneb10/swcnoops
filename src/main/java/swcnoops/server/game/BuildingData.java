package swcnoops.server.game;

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
}
