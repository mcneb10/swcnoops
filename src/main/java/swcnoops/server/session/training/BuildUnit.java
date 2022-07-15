package swcnoops.server.session.training;

import com.fasterxml.jackson.annotation.JsonIgnore;
import swcnoops.server.game.ContractType;
import swcnoops.server.session.Constructor;

/**
 * Unit build details, equivalent to the Contracts model in the game.
 * This has references to the builder/building that is building it, as well
 * as the build slot it was put in.
 */
public class BuildUnit {
    @JsonIgnore
    private Constructor constructor;
    private String buildingId;
    private String unitId;
    private long startTime;
    private long endTime;
    private ContractType contractType;
    @JsonIgnore
    private BuildSlot buildSlot;
    private String tag;

    public BuildUnit() {
    }

    public BuildUnit(Constructor constructor, String buildingId, String unitId, ContractType contractType, String tag) {
        this.buildingId = buildingId;
        this.unitId = unitId;
        this.constructor = constructor;
        this.contractType = contractType;
        this.tag = tag;
    }

    public ContractType getContractType() {
        return contractType;
    }

    protected void setBuilder(Constructor constructor) {
        this.constructor = constructor;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public String getUnitId() {
        return unitId;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setBuildSlot(BuildSlot buildSlot) {
        this.buildSlot = buildSlot;
    }

    public BuildSlot getBuildSlot() {
        return buildSlot;
    }

    public Constructor getConstructor() {
        return constructor;
    }

    public int compareEndTime(BuildUnit b) {
        return Long.compare(this.getEndTime(), b.getEndTime());
    }

    public String getTag() {
        return tag;
    }
}
