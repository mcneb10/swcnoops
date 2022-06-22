package swcnoops.server.session;

import swcnoops.server.game.BuildableData;

import java.util.*;

/**
 * This is to model the games transports to hold the troops that has finished building.
 * Also used for special attacks (air) for its contracts.
 */
public class TroopsTransport {
    private int storage;
    private int seatsTaken;
    final private Map<String, Integer> troopsOnBoard = new HashMap<>();
    final private List<AbstractBuildContract> troopsInQueue = new ArrayList<>();

    public TroopsTransport() {
        this(0);
    }

    public TroopsTransport(int storage) {
        this.storage = storage;
    }

    public int getStorage() {
        return storage;
    }

    public void addStorage(int storage) {
        this.storage += storage;
    }

    public int getAvailableCapacity() {
        return this.storage - this.seatsTaken;
    }

    public void onBoardTroop(AbstractBuildContract troopContract) {
        BuildableData buildableData = troopContract.getContractGroup().getBuildableData();
        this.seatsTaken += buildableData.getSize();
        Integer numberOfTroops = this.troopsOnBoard.get(troopContract.getUnitTypeId());
        if (numberOfTroops == null)
            numberOfTroops = Integer.valueOf(0);

        numberOfTroops = Integer.valueOf(numberOfTroops.intValue() + 1);
        this.troopsOnBoard.put(troopContract.getUnitTypeId(), numberOfTroops);
    }

    public Map<String, Integer> getTroopsOnBoard() {
        return troopsOnBoard;
    }

    public void removeTroopsOnBoard(Map<String, Integer> deployablesToRemove) {
        deployablesToRemove.entrySet().iterator();

        Iterator<Map.Entry<String,Integer>> troopIterator = deployablesToRemove.entrySet().iterator();
        while(troopIterator.hasNext()) {
            Map.Entry<String,Integer> entry = troopIterator.next();
            if (this.troopsOnBoard.containsKey(entry.getKey())) {
                int onBoard = this.troopsOnBoard.get(entry.getKey()).intValue();
                // number from client is negative
                int numberToRemove = Math.abs(entry.getValue().intValue());
                if (onBoard < numberToRemove) {
                    numberToRemove = onBoard;
                }

                onBoard = onBoard - numberToRemove;
                this.seatsTaken -= numberToRemove;
                if (onBoard == 0) {
                    this.troopsOnBoard.remove(entry.getKey());
                } else {
                    this.troopsOnBoard.put(entry.getKey(), Integer.valueOf(onBoard));
                }
            }
        }
    }

    public void onBoardCompletedTroops(long clientTime) {
        Iterator<AbstractBuildContract> troopContractIterator = this.troopsInQueue.iterator();
        while(troopContractIterator.hasNext()) {
            AbstractBuildContract troopContract = troopContractIterator.next();
            // troopContracts are sorted in endTime order
            if (troopContract.getEndTime() > clientTime) {
                break;
            }

            // is there enough space to move this completed troop to the transport
            if (troopContract.getContractGroup().getBuildableData().getSize() < this.getAvailableCapacity()) {
                troopContractIterator.remove();
                troopContract.getParent().removeCompletedContract(troopContract);
                this.moveToStarport(troopContract);
            }
        }
    }

    public void moveToStarport(List<AbstractBuildContract> buildContracts) {
        if (buildContracts != null) {
            for (AbstractBuildContract buildContract : buildContracts) {
                moveToStarport(buildContract);
            }
        }
    }

    private void moveToStarport(AbstractBuildContract buildContract) {
        this.troopsInQueue.remove(buildContract);
        this.onBoardTroop(buildContract);
    }

    protected void sortTroopsInQueue() {
        this.troopsInQueue.sort((a, b) -> a.compareEndTime(b));
    }

    public List<AbstractBuildContract> getTroopsInQueue() {
        return this.troopsInQueue;
    }

    public void addTroopsToQueue(List<AbstractBuildContract> troopBuildContracts) {
        this.troopsInQueue.addAll(troopBuildContracts);
    }

    public void removeTroopsFromQueue(List<AbstractBuildContract> buildContracts) {
        if (buildContracts != null)
            this.troopsInQueue.removeAll(buildContracts);
    }
}
