package swcnoops.server.session.training;

import swcnoops.server.game.TroopData;

import java.util.*;

/**
 * This is to model the games queuing system for its troops, when they are built, completed and moved to being
 * a deployable unit ready for war.
 */
public class DeployableQueue {
    private int storage;
    private int totalDeployable;
    final private Map<String, Integer> deployableUnits = new HashMap<>();
    final private List<BuildUnit> unitsInQueue = new ArrayList<>();

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

    private void moveToDeployable(BuildUnit buildUnit) {
        TroopData troopData = buildUnit.getBuildSlot().getTroopData();
        this.totalDeployable += troopData.getSize();
        Integer numberOfUnits = this.deployableUnits.get(buildUnit.getUnitId());
        if (numberOfUnits == null)
            numberOfUnits = Integer.valueOf(0);

        numberOfUnits = Integer.valueOf(numberOfUnits.intValue() + 1);
        this.deployableUnits.put(buildUnit.getUnitId(), numberOfUnits);
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
        Iterator<BuildUnit> buildUnitsIterator = this.unitsInQueue.iterator();
        while(buildUnitsIterator.hasNext()) {
            BuildUnit buildUnit = buildUnitsIterator.next();
            // units are sorted by endTime
            if (buildUnit.getEndTime() > clientTime) {
                break;
            }

            // is there enough space to move this completed troop to the transport
            if (buildUnit.getBuildSlot().getTroopData().getSize() < this.getAvailableCapacity()) {
                buildUnitsIterator.remove();
                buildUnit.getConstructor().removeCompletedBuildUnit(buildUnit);
                this.moveUnitToDeployable(buildUnit);
            }
        }
    }

    public void moveUnitToDeployable(List<BuildUnit> buildUnits) {
        if (buildUnits != null) {
            for (BuildUnit buildUnit : buildUnits) {
                moveUnitToDeployable(buildUnit);
            }
        }
    }

    private void moveUnitToDeployable(BuildUnit buildUnit) {
        this.unitsInQueue.remove(buildUnit);
        this.moveToDeployable(buildUnit);
    }

    protected void sortUnitsInQueue() {
        this.unitsInQueue.sort((a, b) -> a.compareEndTime(b));
    }

    public List<BuildUnit> getUnitsInQueue() {
        return this.unitsInQueue;
    }

    public void addUnitsToQueue(List<BuildUnit> buildUnits) {
        this.unitsInQueue.addAll(buildUnits);
    }

    public void addUnitsToQueue(BuildUnit buildUnit) {
        this.unitsInQueue.add(buildUnit);
    }

    public void removeUnitsFromQueue(List<BuildUnit> buildUnits) {
        if (buildUnits != null)
            this.unitsInQueue.removeAll(buildUnits);
    }
}
