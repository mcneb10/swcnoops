package swcnoops.server.session;

public class TroopContract {
    final private String unitTypeId;
    final private long startTime;
    private int quantity;

    protected TroopContract(String unitTypeId, int quantity, long startTime) {
        this.unitTypeId = unitTypeId;
        this.startTime = startTime;
        this.quantity = quantity;
    }

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    public String getUnitTypeId() {
        return unitTypeId;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getQuantity() {
        return quantity;
    }
}
