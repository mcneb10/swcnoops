package swcnoops.server.session.inventory;

public class TroopUpgrade {
    private String buildingKey;
    private String troopUnitId;
    private long endTime;

    public TroopUpgrade() {
    }

    public TroopUpgrade(String buildingKey, String troopUnitId, long endTime) {
        this.buildingKey = buildingKey;
        this.troopUnitId = troopUnitId;
        this.endTime = endTime;
    }

    public String getBuildingKey() {
        return buildingKey;
    }

    public String getTroopUnitId() {
        return troopUnitId;
    }

    public long getEndTime() {
        return endTime;
    }

    public void buyout(long time) {
        this.endTime = time;
    }
}
