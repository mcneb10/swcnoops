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
        this.heroTransport = new TroopsTransport();
        this.championTransport = new TroopsTransport();
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
        TroopsTransport transport = contractConstructor.getTransport();
        if (transport != null) {
            transport.addTroopsToQueue(troopBuildContracts);
            transport.sortTroopsInQueue();
        }
    }

    @Override
    public void cancelTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        // we move completed troops last because if they managed to remove contracts
        // at the head of the queue, then they must of got there before it was completed
        ContractConstructor contractConstructor = getContractConstructor(buildingId);
        List<BuildContract> cancelledContracts =
                contractConstructor.removeContracts(unitTypeId, quantity, time, true);
        TroopsTransport transport = contractConstructor.getTransport();
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
        TroopsTransport transport = contractConstructor.getTransport();
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

    private ContractConstructor getContractConstructor(String buildingId) {
        ContractConstructor contractConstructor = this.contractConstructors.get(buildingId);
        if (contractConstructor == null)
            throw new RuntimeException("Constructor for building has not been initialised " + buildingId);
        return contractConstructor;
    }

    @Override
    public void initialiseContractConstructor(Building building, BuildingData buildingData, TroopsTransport transport) {
        if (!this.contractConstructors.containsKey(building.key)) {
            ContractConstructor contractConstructor = new ContractConstructor(building.key, buildingData, transport);
            this.contractConstructors.put(contractConstructor.getBuildingId(), contractConstructor);
        }
    }

    @Override
    public void initialiseBuildContract(BuildContract buildContract) {
        ContractConstructor contractConstructor = this.getContractConstructor(buildContract.getBuildingId());
        contractConstructor.loadContract(buildContract);
        TroopsTransport transport = contractConstructor.getTransport();
        transport.addTroopsToQueue(buildContract);
    }
}
