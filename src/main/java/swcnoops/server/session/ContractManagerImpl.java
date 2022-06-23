package swcnoops.server.session;

import swcnoops.server.game.BuildingData;
import swcnoops.server.model.Building;
import java.util.*;

public class ContractManagerImpl implements ContractManager {
    final private Map<String, ContractConstructor> contractConstructors = new HashMap<>();
    final private TroopsTransport troopsTransport;
    final private TroopsTransport specialAttackTransport;
    final private TroopsTransport heroTransport;
    final private TroopsTransport championTransport;

    public ContractManagerImpl()
    {
        this.troopsTransport = new TroopsTransport();
        this.specialAttackTransport = new TroopsTransport();
        this.heroTransport = new TroopsTransport(3);
        this.championTransport = new TroopsTransport(2);
    }

    public TroopsTransport getTroopsTransport() {
        return troopsTransport;
    }

    @Override
    public TroopsTransport getSpecialAttackTransport() {
        return specialAttackTransport;
    }

    @Override
    public TroopsTransport getHeroTransport() {
        return heroTransport;
    }

    @Override
    public TroopsTransport getChampionTransport() {
        return championTransport;
    }

    @Override
    public void trainTroops(String buildingId, String unitTypeId, int quantity, long startTime) {
        moveCompletedTroops(startTime);

        // create contracts for the things that we want to build
        ContractConstructor contractConstructor = getContractConstructor(buildingId);
        List<BuildContract> troopBuildContracts = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) {
            BuildContract buildContract = new BuildContract(contractConstructor, buildingId, unitTypeId);
            troopBuildContracts.add(buildContract);
        }

        contractConstructor.addContracts(troopBuildContracts, startTime);
        TroopsTransport transport = getTransport(troopBuildContracts.get(0));
        if (transport != null) {
            transport.addTroopsToQueue(troopBuildContracts);
            transport.sortTroopsInQueue();
        }
    }

    private TroopsTransport getTransport(List<BuildContract> buildContracts) {
        if (buildContracts == null || buildContracts.size() == 0)
            return null;

        return getTransport(buildContracts.get(0));
    }

    private TroopsTransport getTransport(BuildContract buildContract) {
        if (buildContract.getContractGroup().getBuildableData().isSpecialAttack())
            return this.specialAttackTransport;

        if (buildContract.getContractGroup().getBuildableData().getType().equals("hero"))
            return this.heroTransport;

        if (buildContract.getContractGroup().getBuildableData().getType().equals("champion"))
            return this.championTransport;

        return this.troopsTransport;
    }

    @Override
    public void cancelTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        // we move completed troops last because if they managed to remove contracts
        // at the head of the queue, then they must of got there before it was completed
        ContractConstructor contractConstructor = getContractConstructor(buildingId);
        List<BuildContract> cancelledContracts =
                contractConstructor.removeContracts(unitTypeId, quantity, time, true);
        TroopsTransport transport = this.getTransport(cancelledContracts);
        if (transport != null) {
            transport.removeTroopsFromQueue(cancelledContracts);
            transport.sortTroopsInQueue();
            transport.onBoardCompletedTroops(time);
        }
    }

    @Override
    public void buyOutTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        //we move completed troops last because if they managed to buy out contracts
        //then they must of got there before it was completed
        ContractConstructor contractConstructor = getContractConstructor(buildingId);
        List<BuildContract> boughtOutContracts =
                contractConstructor.removeContracts(unitTypeId, quantity, time, false);
        TroopsTransport transport = this.getTransport(boughtOutContracts);
        if (transport != null) {
            transport.moveToStarport(boughtOutContracts);
            transport.sortTroopsInQueue();
            transport.onBoardCompletedTroops(time);
        }
    }

    @Override
    public void moveCompletedTroops(long clientTime) {
        this.troopsTransport.onBoardCompletedTroops(clientTime);
        this.specialAttackTransport.onBoardCompletedTroops(clientTime);
        this.heroTransport.onBoardCompletedTroops(clientTime);
        this.championTransport.onBoardCompletedTroops(clientTime);
    }

    @Override
    public List<BuildContract> getAllTroopContracts() {
        List<BuildContract> allContracts = new ArrayList<>();
        allContracts.addAll(this.troopsTransport.getTroopsInQueue());
        allContracts.addAll(this.specialAttackTransport.getTroopsInQueue());
        allContracts.addAll(this.heroTransport.getTroopsInQueue());
        allContracts.addAll(this.championTransport.getTroopsInQueue());
        return allContracts;
    }

    @Override
    public void addContractConstructor(Building building, BuildingData buildingData) {
        if (!this.contractConstructors.containsKey(building.key)) {
            ContractConstructor contractConstructor = new ContractConstructor(building.key, buildingData);
            this.contractConstructors.put(contractConstructor.getBuildingId(), contractConstructor);
        }
    }

    private ContractConstructor getContractConstructor(String buildingId) {
        ContractConstructor contractConstructor = this.contractConstructors.get(buildingId);
        if (contractConstructor == null)
            throw new RuntimeException("Constructor for building has not been initialised " + buildingId);
        return contractConstructor;
    }


    @Override
    public void loadBuildContract(BuildContract buildContract) {
        ContractConstructor contractConstructor = this.getContractConstructor(buildContract.getBuildingId());
        contractConstructor.loadContract(buildContract);
        TroopsTransport transport = this.getTransport(buildContract);
        transport.addTroopsToQueue(buildContract);
    }
}
