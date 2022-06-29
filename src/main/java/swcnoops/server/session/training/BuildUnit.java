package swcnoops.server.session.training;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Unit build details, equivalent to the Contracts model in the game.
 * This has references to the builder/building that is building it, as well
 * as the build slot it was put in.
 */
public class BuildUnit {
    @JsonIgnore
    private Builder builder;
    private String buildingId;
    private String unitId;
    private long startTime;
    private long endTime;
    @JsonIgnore
    private BuildSlot buildSlot;

    public BuildUnit() {
    }

    public BuildUnit(Builder builder, String buildingId, String unitId) {
        this.buildingId = buildingId;
        this.unitId = unitId;
        this.builder = builder;
    }

    protected void setBuilder(Builder builder) {
        this.builder = builder;
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

    public Builder getBuilder() {
        return builder;
    }

    public int compareEndTime(BuildUnit b) {
        return Long.compare(this.getEndTime(), b.getEndTime());
    }
}