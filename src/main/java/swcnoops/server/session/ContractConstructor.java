package swcnoops.server.session;

import swcnoops.server.game.BuildableData;
import swcnoops.server.game.BuildingData;

import java.util.*;

/**
 * This class represents one of the buildings on the map used to build something.
 * It simulates the behavior of what you see in the game.
 * This is so the server can keep the data in sync when the client reloads.
 * Each unit type being built is grouped in the game, the order of the build of a single unit
 * is dependent on the order of its group, and not when the unit was selected to be built.
 */
public class ContractConstructor {
    final private String buildingId;
    final private ContractBuildQueue contractBuildQueue = new ContractBuildQueue();
    private long startTime;
    final private BuildingData buildingData;
    private TroopsTransport transport;

    public ContractConstructor(String buildingId, BuildingData buildingData, TroopsTransport transport) {
        this.buildingId = buildingId;
        this.buildingData = buildingData;
        this.transport = transport;
    }

    public String getBuildingId() {
        return this.buildingId;
    }

    public BuildingData getBuildingData() {
        return buildingData;
    }

    protected void addContracts(List<BuildContract> buildContracts, long startTime) {
        // if first item in the queue, then we set the startTime
        if (this.contractBuildQueue.isEmpty())
            this.startTime = startTime;

        this.contractBuildQueue.addToBuildQueue(buildContracts);
        this.recalculateContractEndTimes(startTime);
    }

    protected List<BuildContract> removeContracts(String unitTypeId, int quantity, long time, boolean fromBack) {
        List<BuildContract> removeContracts =
                this.contractBuildQueue.removeContracts(unitTypeId, quantity, fromBack);
        this.recalculateContractEndTimes(time);
        return removeContracts;
    }

    protected void removeCompletedContract(BuildContract troopContract) {
        // remove it from its group first, then see if the whole group can be removed
        troopContract.getContractGroup().removeCompletedContract(troopContract);
        this.contractBuildQueue.removeContractGroupIfEmpty(troopContract.getContractGroup());
    }

    private void recalculateContractEndTimes(long timeFromClient) {
        this.startTime = determineStartTime(timeFromClient);
        // TODO - might need to change this for 2nd in the queue if the head
        // is blocked and has not moved to complete yet.
        // this can be detected by looking to see if there is enough space
        // and if the the clients time is after the heads endTime.
        // if no room, then from 2nd contract its endTime starts from the clients time
        this.contractBuildQueue.recalculateContractEndTimes(this.startTime);
    }

    private long determineStartTime(long timeFromClient) {
        long startTimeForQueue = this.startTime;

        // we work out if the start time has changed for this queue
        // done by looking at the head of the queue to see if its end time needs to be adjusted
        if (this.contractBuildQueue.getBuildQueue().size() > 0) {
            ContractGroup firstContractGroup = this.contractBuildQueue.getBuildQueue().getFirst();
            BuildableData buildableData = firstContractGroup.getBuildableData();
            List<BuildContract> buildContracts = firstContractGroup.getFirstEndTime();
            if (buildContracts.size() > 0) {
                long firstEndTime = buildContracts.get(0).getEndTime();
                if (firstEndTime != 0) {
                    long startDateForContract = firstEndTime - buildableData.getBuildingTime();

                    // this means the head was removed earlier so all contracts should also start earlier
                    if (timeFromClient < startDateForContract) {
                        startTimeForQueue = timeFromClient;
                    } else if (timeFromClient > startDateForContract) {
                        // was in the middle of building we adjust our start to match the first contract
                        startTimeForQueue = startDateForContract;
                    }
                }
            }
        }

        return startTimeForQueue;
    }

    protected void loadContract(BuildContract buildContract) {
        buildContract.setParent(this);
        this.contractBuildQueue.loadToBuildQueue(buildContract);
    }

    public TroopsTransport getTransport() {
        return transport;
    }
}
