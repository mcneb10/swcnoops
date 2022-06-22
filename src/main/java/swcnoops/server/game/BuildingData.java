package swcnoops.server.game;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class BuildingData implements BuildableData {
    final private String uid;
    private String faction;
    private int level;
    private String type;
    private int storage;
    private int time;
    private String buildingID;

    public BuildingData(String uid) {
        this.uid = uid;
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

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
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

    public String getUid() {
        return this.uid;
    }

    @Override
    public long getBuildingTime() {
        return this.getTime();
    }

    @Override
    public int getSize() {
        throw new NotImplementedException();
    }

    @Override
    public boolean isSpecialAttack() {
        return false;
    }
}
