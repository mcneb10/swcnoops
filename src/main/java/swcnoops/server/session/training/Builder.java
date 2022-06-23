package swcnoops.server.session.training;

import swcnoops.server.game.BuildableData;
import swcnoops.server.game.BuildingData;

import java.util.*;

/**
 * This class represents one of the buildings on the map used to build something.
 * Each building has its own queue of work that it can perform that has its own capacity which is not modelled.
 * When a unit/troop has finished building it gets moved to a shared deployable queue that will only take the troop
 * if there is space available for that troops size.
 */
public class Builder {
    final private String buildingId;
    final private BuildQueue buildQueue = new BuildQueue();
    private long startTime;
    final private BuildingData buildingData;
    private DeployableQueue deployableQueue;

    public Builder(String buildingId, BuildingData buildingData, DeployableQueue deployableQueue) {
        this.buildingId = buildingId;
        this.buildingData = buildingData;
        this.deployableQueue = deployableQueue;
    }

    public String getBuildingId() {
        return this.buildingId;
    }

    protected void train(List<BuildUnit> buildUnits, long startTime) {
        // if first item in the queue, then we set the startTime
        if (this.buildQueue.isEmpty())
            this.startTime = startTime;

        this.buildQueue.add(buildUnits);
        this.recalculateEndTimes(startTime);
    }

    protected List<BuildUnit> remove(String unitTypeId, int quantity, long time, boolean fromBack) {
        List<BuildUnit> removed = this.buildQueue.remove(unitTypeId, quantity, fromBack);
        this.recalculateEndTimes(time);
        return removed;
    }

    protected void removeCompletedBuildUnit(BuildUnit buildUnit) {
        // remove it from its slot first, then see if the whole slot can be removed
        buildUnit.getBuildSlot().removeBuildUnit(buildUnit);
        this.buildQueue.removeBuildSlotIfEmpty(buildUnit.getBuildSlot());
    }

    private void recalculateEndTimes(long timeFromClient) {
        this.startTime = determineStartTime(timeFromClient);
        // TODO - might need to change this for 2nd in the queue if the head
        // is blocked and has not moved to complete yet.
        // this can be detected by looking to see if there is enough space
        // and if the the clients time is after the heads endTime.
        // if no room, then from 2nd unit its endTime starts from the clients time
        this.buildQueue.recalculateEndTimes(this.startTime);
    }

    private long determineStartTime(long timeFromClient) {
        long startTimeForBuilder = this.startTime;

        // we work out if the start time has changed for this queue
        // done by looking at the head of the queue to see if its end time needs to be adjusted
        if (this.buildQueue.getBuildQueue().size() > 0) {
            BuildSlot firstBuildSlot = this.buildQueue.getBuildQueue().getFirst();
            BuildableData buildableData = firstBuildSlot.getBuildableData();
            List<BuildUnit> buildUnits = firstBuildSlot.getFirstEndTime();
            if (buildUnits.size() > 0) {
                long firstUnitEndTime = buildUnits.get(0).getEndTime();
                if (firstUnitEndTime != 0) {
                    long startTimeForUnit = firstUnitEndTime - buildableData.getBuildingTime();

                    // this means the head was removed earlier so all contracts should also start earlier
                    if (timeFromClient < startTimeForUnit) {
                        startTimeForBuilder = timeFromClient;
                    } else if (timeFromClient > startTimeForUnit) {
                        // was in the middle of building we adjust our start to match the first contract
                        startTimeForBuilder = startTimeForUnit;
                    }
                }
            }
        }

        return startTimeForBuilder;
    }

    protected void load(BuildUnit buildUnit) {
        buildUnit.setParent(this);
        this.buildQueue.add(buildUnit);
    }

    public DeployableQueue getDeployableQueue() {
        return deployableQueue;
    }
}
