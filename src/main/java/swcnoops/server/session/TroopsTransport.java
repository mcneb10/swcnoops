package swcnoops.server.session;

import swcnoops.server.game.BuildableData;

import java.util.*;

/**
 * This is to model the games transports to hold the troops that has finished building.
 * Also used for special attacks (air) for its contracts.
 * Used for hero building, not the same as how the client behaves but close enough.
 */
public class TroopsTransport {
    private int storage;
    private int seatsTaken;
    final private Map<String, Integer> troopsOnBoard = new HashMap<>();
    final private List<BuildContract> troopsInQueue = new ArrayList<>();

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

    public void onBoardTroop(BuildContract troopContract) {
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
            removeTroopsOnBoard(entry.getKey(), entry.getValue());
        }
    }

    public void removeTroopsOnBoard(String key, Integer value) {
        if (this.troopsOnBoard.containsKey(key)) {
            int onBoard = value.intValue();
            // number from client is negative
            int numberToRemove = Math.abs(value.intValue());
            if (onBoard < numberToRemove) {
                numberToRemove = onBoard;
            }

            onBoard = onBoard - numberToRemove;
            this.seatsTaken -= numberToRemove;
            if (onBoard == 0) {
                this.troopsOnBoard.remove(key);
            } else {
                this.troopsOnBoard.put(key, Integer.valueOf(onBoard));
            }
        }
    }

    public void onBoardCompletedTroops(long clientTime) {
        Iterator<BuildContract> troopContractIterator = this.troopsInQueue.iterator();
        while(troopContractIterator.hasNext()) {
            BuildContract troopContract = troopContractIterator.next();
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

    public void moveToStarport(List<BuildContract> buildContracts) {
        if (buildContracts != null) {
            for (BuildContract buildContract : buildContracts) {
                moveToStarport(buildContract);
            }
        }
    }

    private void moveToStarport(BuildContract buildContract) {
        this.troopsInQueue.remove(buildContract);
        this.onBoardTroop(buildContract);
    }

    protected void sortTroopsInQueue() {
        this.troopsInQueue.sort((a, b) -> a.compareEndTime(b));
    }

    public List<BuildContract> getTroopsInQueue() {
        return this.troopsInQueue;
    }

    public void addTroopsToQueue(List<BuildContract> troopBuildContracts) {
        this.troopsInQueue.addAll(troopBuildContracts);
    }

    public void removeTroopsFromQueue(List<BuildContract> buildContracts) {
        if (buildContracts != null)
            this.troopsInQueue.removeAll(buildContracts);
    }
}
