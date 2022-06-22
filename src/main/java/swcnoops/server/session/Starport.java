package swcnoops.server.session;

import swcnoops.server.game.BuildableData;

import java.util.HashMap;
import java.util.Map;

public class Starport {
    private int storage;
    private int seatsTaken;
    final private Map<String, Integer> troops = new HashMap<>();

    public int getStorage() {
        return storage;
    }

    public void addStorage(int storage) {
        this.storage += storage;
    }

    // TODO
    public int getAvailableCapacity() {
        return this.storage - this.seatsTaken;
    }

    public void addTroopContract(AbstractBuildContract troopContract) {
        BuildableData buildableData = troopContract.getContractGroup().getBuildableData();
        this.seatsTaken += buildableData.getSize();
        Integer numberOfTroops = this.troops.get(troopContract.getUnitTypeId());
        if (numberOfTroops == null)
            numberOfTroops = Integer.valueOf(0);

        numberOfTroops = Integer.valueOf(numberOfTroops.intValue() + 1);
        this.troops.put(troopContract.getUnitTypeId(), numberOfTroops);
    }

    public Map<String, Integer> getTroops() {
        return troops;
    }
}
