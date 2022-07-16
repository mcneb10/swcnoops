package swcnoops.server.model;

public class SupplyData {
    public String supplyId;
    public String supplyPoolId;

    public SupplyData() {
    }

    public SupplyData(String supplyId, String supplyPoolId) {
        this.supplyId = supplyId;
        this.supplyPoolId = supplyPoolId;
    }
}
