package swcnoops.server.session;

import java.util.*;

public class ContractManagerImpl implements ContractManager {
    final private Map<String, ContractConstructor> contractConstructors = new HashMap<>();
    final private TroopsTransport troopsTransport;
    final private TroopsTransport specialAttackTransport;

    public ContractManagerImpl(TroopsTransport troopsTransport, TroopsTransport specialAttackTransport) {
        this.troopsTransport = troopsTransport;
        this.specialAttackTransport = specialAttackTransport;
    }

    @Override
    public void trainTroops(String buildingId, String unitTypeId, int quantity, long startTime) {
        moveCompletedTroops(startTime);

        // create contracts for the things that we want to build
        ContractConstructor contractConstructor = getOrCreateContractConstructor(buildingId);
        List<AbstractBuildContract> troopBuildContracts = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) {
            TroopBuildContract troopBuildContract = new TroopBuildContract(buildingId, unitTypeId, contractConstructor);
            troopBuildContracts.add(troopBuildContract);
        }

        contractConstructor.addContracts(troopBuildContracts, startTime);
        TroopsTransport transport = getTransport(troopBuildContracts.get(0));
        if (transport != null) {
            transport.addTroopsToQueue(troopBuildContracts);
            transport.sortTroopsInQueue();
        }
    }

    private TroopsTransport getTransport(List<AbstractBuildContract> buildContracts) {
        if (buildContracts == null || buildContracts.size() == 0)
            return null;

        return getTransport(buildContracts.get(0));
    }

    private TroopsTransport getTransport(AbstractBuildContract buildContract) {
        if (buildContract.getContractGroup().getBuildableData().isSpecialAttack())
            return this.specialAttackTransport;

        return this.troopsTransport;
    }

    @Override
    public void cancelTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        // we move completed troops last because if they managed to remove contracts
        // at the head of the queue, then they must of got there before it was completed
        ContractConstructor contractConstructor = getOrCreateContractConstructor(buildingId);
        List<AbstractBuildContract> cancelledContracts =
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
        ContractConstructor contractConstructor = getOrCreateContractConstructor(buildingId);
        List<AbstractBuildContract> boughtOutContracts =
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
    }

    private ContractConstructor getOrCreateContractConstructor(String buildingId) {
        ContractConstructor contractConstructor = this.contractConstructors.get(buildingId);
        if (contractConstructor == null) {
            contractConstructor = new ContractConstructor(buildingId);
            this.contractConstructors.put(contractConstructor.getBuildingId(), contractConstructor);
        }
        return contractConstructor;
    }

    @Override
    public List<AbstractBuildContract> getAllTroopContracts() {
        List<AbstractBuildContract> allContracts = new ArrayList<>();
        allContracts.addAll(this.troopsTransport.getTroopsInQueue());
        allContracts.addAll(this.specialAttackTransport.getTroopsInQueue());
        return allContracts;
    }
}
