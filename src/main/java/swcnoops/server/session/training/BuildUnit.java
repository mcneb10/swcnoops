package swcnoops.server.session.training;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Unit build details, equivalent to the Contracts model in the game.
 * This has references to the builder/building that is building it, as well
 * as the build slot it was put in.
 */
public class BuildUnit {
    @JsonIgnore
    private Builder parent;
    private String buildingId;
    private String unitTypeId;
    private long endTime;
    @JsonIgnore
    private BuildSlot buildSlot;

    public BuildUnit() {
    }

    public BuildUnit(Builder parent, String buildingId, String unitTypeId) {
        this.buildingId = buildingId;
        this.unitTypeId = unitTypeId;
        this.parent = parent;
    }

    protected void setParent(Builder parent) {
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

    public void setBuildSlot(BuildSlot buildSlot) {
        this.buildSlot = buildSlot;
    }

    public BuildSlot getBuildSlot() {
        return buildSlot;
    }

    @JsonIgnore
    public Builder getBuilder() {
        return parent;
    }

    public int compareEndTime(BuildUnit b) {
        if (this.getEndTime() < b.getEndTime())
            return -1;

        if (this.getEndTime() == b.getEndTime())
            return 0;

        return 1;
    }
}
