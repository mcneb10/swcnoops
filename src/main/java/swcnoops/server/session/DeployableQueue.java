package swcnoops.server.session;

import swcnoops.server.game.BuildableData;

import java.util.*;

/**
 * This is to model the games queuing system for its troops, when they are built, completed and moved to being
 * a deployable unit ready for war.
 */
public class DeployableQueue {
    private int storage;
    private int totalDeployable;
    final private Map<String, Integer> deployableUnits = new HashMap<>();
    final private List<BuildContract> unitsInQueue = new ArrayList<>();

    public DeployableQueue() {
        this(0);
    }

    public DeployableQueue(int storage) {
        this.storage = storage;
    }

    public void addStorage(int storage) {
        this.storage += storage;
    }

    private int getAvailableCapacity() {
        return this.storage - this.totalDeployable;
    }

    private void moveToDeployable(BuildContract buildContract) {
        BuildableData buildableData = buildContract.getContractGroup().getBuildableData();
        this.totalDeployable += buildableData.getSize();
        Integer numberOfUnits = this.deployableUnits.get(buildContract.getUnitTypeId());
        if (numberOfUnits == null)
            numberOfUnits = Integer.valueOf(0);

        numberOfUnits = Integer.valueOf(numberOfUnits.intValue() + 1);
        this.deployableUnits.put(buildContract.getUnitTypeId(), numberOfUnits);
    }

    public Map<String, Integer> getDeployableUnits() {
        return deployableUnits;
    }

    public void removeDeployable(Map<String, Integer> deployables) {
        deployables.entrySet().iterator();

        Iterator<Map.Entry<String,Integer>> troopIterator = deployables.entrySet().iterator();
        while(troopIterator.hasNext()) {
            Map.Entry<String,Integer> entry = troopIterator.next();
            removeDeployable(entry.getKey(), entry.getValue());
        }
    }

    public void removeDeployable(String key, Integer value) {
        if (this.deployableUnits.containsKey(key)) {
            int onBoard = value.intValue();
            // number from client is negative
            int numberToRemove = Math.abs(value.intValue());
            if (onBoard < numberToRemove) {
                numberToRemove = onBoard;
            }

            onBoard = onBoard - numberToRemove;
            this.totalDeployable -= numberToRemove;
            if (onBoard == 0) {
                this.deployableUnits.remove(key);
            } else {
                this.deployableUnits.put(key, Integer.valueOf(onBoard));
            }
        }
    }

    public void findAndMoveCompletedUnitsToDeployable(long clientTime) {
        Iterator<BuildContract> buildContractIterator = this.unitsInQueue.iterator();
        while(buildContractIterator.hasNext()) {
            BuildContract buildContract = buildContractIterator.next();
            // troopContracts are sorted in endTime order
            if (buildContract.getEndTime() > clientTime) {
                break;
            }

            // is there enough space to move this completed troop to the transport
            if (buildContract.getContractGroup().getBuildableData().getSize() < this.getAvailableCapacity()) {
                buildContractIterator.remove();
                buildContract.getParent().removeCompletedContract(buildContract);
                this.moveUnitToDeployable(buildContract);
            }
        }
    }

    public void moveUnitToDeployable(List<BuildContract> buildContracts) {
        if (buildContracts != null) {
            for (BuildContract buildContract : buildContracts) {
                moveUnitToDeployable(buildContract);
            }
        }
    }

    private void moveUnitToDeployable(BuildContract buildContract) {
        this.unitsInQueue.remove(buildContract);
        this.moveToDeployable(buildContract);
    }

    protected void sortUnitsInQueue() {
        this.unitsInQueue.sort((a, b) -> a.compareEndTime(b));
    }

    public List<BuildContract> getUnitsInQueue() {
        return this.unitsInQueue;
    }

    public void addUnitsToQueue(List<BuildContract> buildContracts) {
        this.unitsInQueue.addAll(buildContracts);
    }

    public void addUnitsToQueue(BuildContract buildContract) {
        this.unitsInQueue.add(buildContract);
    }

    public void removeUnitsFromQueue(List<BuildContract> buildContracts) {
        if (buildContracts != null)
            this.unitsInQueue.removeAll(buildContracts);
    }
}
