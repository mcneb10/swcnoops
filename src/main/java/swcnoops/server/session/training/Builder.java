package swcnoops.server.session.training;

import swcnoops.server.game.BuildingData;
import swcnoops.server.game.ContractType;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.buildings.MapItem;

import java.util.*;

/**
 * This class represents one of the buildings on the map used to build something.
 * Each building has its own queue of work that it can perform that has its own capacity which is not modelled.
 * When a unit/troop has finished building it gets moved to a shared deployable queue that will only take the troop
 * if there is space available for that troops size.
 */
public class Builder implements MapItem {
    final private String buildingId;
    final private BuildQueue buildQueue;
    private long startTime;
    final private BuildingData buildingData;
    final private DeployableQueue deployableQueue;
    final private ContractType contractType;

    public Builder(PlayerSession playerSession, String buildingId, BuildingData buildingData,
                   DeployableQueue deployableQueue, ContractType contractType)
    {
        this.buildingId = buildingId;
        this.buildingData = buildingData;
        this.deployableQueue = deployableQueue;
        this.contractType = contractType;
        this.buildQueue = new BuildQueue(playerSession);
    }

    @Override
    public BuildingData getBuildingData() {
        return buildingData;
    }

    @Override
    public String getBuildingId() {
        return this.buildingId;
    }

    protected void train(List<BuildUnit> buildUnits, long startTime) {
        // if first item in the queue, then we set the startTime
        if (this.buildQueue.isEmpty())
            this.startTime = startTime;

        this.buildQueue.add(buildUnits);
        this.recalculateBuildUnitTimes(startTime);
    }

    protected List<BuildUnit> remove(String unitTypeId, int quantity, long time, boolean fromBack) {
        List<BuildUnit> removed = this.buildQueue.remove(unitTypeId, quantity, fromBack);
        this.recalculateBuildUnitTimes(time);
        return removed;
    }

    protected void removeCompletedBuildUnit(BuildUnit buildUnit) {
        // remove it from its slot first, then see if the whole slot can be removed
        buildUnit.getBuildSlot().removeBuildUnit(buildUnit);
        this.buildQueue.removeBuildSlotIfEmpty(buildUnit.getBuildSlot());
    }

    protected void recalculateBuildUnitTimes(long timeFromClient) {
        this.startTime = determineStartTime(timeFromClient);
        // TODO - might need to change this for 2nd in the queue if the head
        // is blocked and has not moved to complete yet.
        // this can be detected by looking to see if there is enough space
        // and if the the clients time is after the heads endTime.
        // if no room, then from 2nd unit its endTime starts from the clients time
        this.buildQueue.recalculateBuildUnitTimes(this.startTime);
    }

    private long determineStartTime(long timeFromClient) {
        long startTimeForBuilder = this.startTime;

        // we work out if the start time has changed for this queue
        // done by looking at the head of the queue to see if its end time needs to be adjusted
        if (this.buildQueue.getBuildQueue().size() > 0) {
            BuildSlot firstBuildSlot = this.buildQueue.getBuildQueue().getFirst();
            BuildUnit buildUnit = firstBuildSlot.getFirstBuildUnit();
            if (buildUnit != null && buildUnit.getStartTime() != 0) {
                // this means the head was removed earlier so all contracts should also start earlier
                if (timeFromClient < buildUnit.getStartTime()) {
                    startTimeForBuilder = timeFromClient;
                } else if (timeFromClient > buildUnit.getStartTime()) {
                    // was in the middle of building we adjust our start to match the first contract
                    startTimeForBuilder = buildUnit.getStartTime();
                }
            }
        }

        return startTimeForBuilder;
    }

    protected void load(BuildUnit buildUnit) {
        buildUnit.setBuilder(this);
        this.buildQueue.add(buildUnit);
    }

    public DeployableQueue getDeployableQueue() {
        return deployableQueue;
    }

    public ContractType getContractType() {
        return this.contractType;
    }
}
