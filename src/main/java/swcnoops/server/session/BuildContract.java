package swcnoops.server.session;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class BuildContract {
    @JsonIgnore
    private ContractConstructor parent;
    private String buildingId;
    private String unitTypeId;
    private long endTime;
    @JsonIgnore
    private ContractGroup contractGroup;

    public BuildContract() {
    }

    public BuildContract(ContractConstructor parent, String buildingId, String unitTypeId) {
        this.buildingId = buildingId;
        this.unitTypeId = unitTypeId;
        this.parent = parent;
        this.endTime = endTime;
    }

    protected void setParent(ContractConstructor parent) {
        this.parent = parent;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public String getUnitTypeId() {
        return unitTypeId;
    }

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

    public int compareEndTime(BuildContract b) {
        if (this.getEndTime() < b.getEndTime())
            return -1;

        if (this.getEndTime() == b.getEndTime())
            return 0;

        return 1;
    }
}
