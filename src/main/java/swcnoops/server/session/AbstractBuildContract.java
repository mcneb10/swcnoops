package swcnoops.server.session;

abstract public class AbstractBuildContract {
    final private ContractConstructor parent;
    final private String buildingId;
    final private String unitTypeId;
    private long endTime;
    private ContractGroup contractGroup;

    public AbstractBuildContract(String buildingId, String unitTypeId, ContractConstructor parent) {
        this.buildingId = buildingId;
        this.unitTypeId = unitTypeId;
        this.parent = parent;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public String getUnitTypeId() {
        return unitTypeId;
    }

    abstract public String getContractType();

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setContractGroup(ContractGroup contractGroup) {
        this.contractGroup = contractGroup;
    }

    public ContractGroup getContractGroup() {
        return contractGroup;
    }

    public ContractConstructor getParent() {
        return parent;
    }

    public int compareEndTime(AbstractBuildContract b) {
        if (this.getEndTime() < b.getEndTime())
            return -1;

        if (this.getEndTime() == b.getEndTime())
            return 0;

        return 1;
    }
}
