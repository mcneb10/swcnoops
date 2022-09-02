package swcnoops.server.session.inventory;

public class TroopUpgrade {
    private int upgradeCost;
    private String buildingKey;
    private String troopUId;
    private long endTime;

    public TroopUpgrade() {
    }

    public TroopUpgrade(String buildingKey, String troopUId, long endTime, int upgradeCost) {
        this.buildingKey = buildingKey;
        this.troopUId = troopUId;
        this.endTime = endTime;
        this.upgradeCost = upgradeCost;
    }

    public String getBuildingKey() {
        return buildingKey;
    }

    public String getTroopUId() {
        return troopUId;
    }

    public long getEndTime() {
        return endTime;
    }

    public void buyout(long time) {
        this.endTime = time;
    }

    public int getUpgradeCost() {
        return upgradeCost;
    }
}
