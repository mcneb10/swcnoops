package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.Player;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.GameDataManager;
import swcnoops.server.model.Building;
import swcnoops.server.model.Contract;
import swcnoops.server.model.PlayerMap;
import swcnoops.server.model.StorageAmount;
import java.util.*;

public class PlayerSessionImpl implements PlayerSession {
    final private Player player;
    private Starport starport = new Starport();

    final private Map<String, ContractConstructor> contractConstructors = new HashMap<>();
    final private List<AbstractBuildContract> troopContracts = new ArrayList<>();

    public PlayerSessionImpl(Player player) {
        this.player = player;

        // TODO
        //initialiseStartport(this.player.getPlayerSettings().getTroopsOnTransport());
    }

    @Override
    public String getPlayerId() {
        return this.player.getPlayerId();
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void trainTroops(String buildingId, String unitTypeId, int quantity, long startTime) {
        moveCompletedTroopsToStarport(startTime);

        // create contracts for the things that we want to build
        ContractConstructor contractConstructor = getOrCreateContractConstructor(buildingId);
        List<AbstractBuildContract> troopBuildContracts = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) {
            TroopBuildContract troopBuildContract = new TroopBuildContract(buildingId, unitTypeId, contractConstructor);
            troopBuildContracts.add(troopBuildContract);
        }

        contractConstructor.addContracts(troopBuildContracts, startTime);
        this.troopContracts.addAll(troopBuildContracts);
        this.troopContracts.sort((a,b) -> a.compareEndTime(b) );
        contractConstructor.recalculateContractEndTimes(startTime);
    }

    @Override
    public void cancelTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        // we move completed troops last because if they managed to remove contracts
        // at the head of the queue, then they must of got there before it was completed
        ContractConstructor contractConstructor = getOrCreateContractConstructor(buildingId);
        contractConstructor.cancelContract(unitTypeId, quantity);
        contractConstructor.recalculateContractEndTimes(time);
        moveCompletedTroopsToStarport(time);
    }

    @Override
    public void moveCompletedTroopsToStarport(long clientTime) {
        Iterator<AbstractBuildContract> troopContractIterator = this.troopContracts.iterator();
        while(troopContractIterator.hasNext()) {
            AbstractBuildContract troopContract = troopContractIterator.next();
            if (troopContract.getEndTime() > clientTime) {
                break;
            }

            if (troopContract.getContractGroup().getBuildableData().getSize() < this.starport.getAvailableCapacity()) {
                troopContract.getContractGroup().removeCompletedContract(troopContract);
                this.starport.addTroopContract(troopContract);
                troopContract.getParent().removeContractGroupIfEmpty(troopContract.getContractGroup());
                troopContractIterator.remove();
            }
        }
    }

    private ContractConstructor getOrCreateContractConstructor(String buildingId) {
        ContractConstructor contractConstructor = this.contractConstructors.get(buildingId);
        if (contractConstructor == null) {
            contractConstructor = new ContractConstructor(buildingId);
            this.contractConstructors.put(contractConstructor.getBuildingId(), contractConstructor);
        }
        return contractConstructor;
    }

    // TODO
    @Override
    public void buyOutTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        // we move completed troops last because if they managed to buy out contracts
        //then they must of got there before it was completed
        ContractConstructor contractConstructor = getOrCreateContractConstructor(buildingId);
        List<AbstractBuildContract> boughtOutContracts = contractConstructor.buyOutContract(unitTypeId, quantity, time);
        if (boughtOutContracts != null)
            moveBoughtOutContractsToStarport(boughtOutContracts);
        contractConstructor.recalculateContractEndTimes(time);
        moveCompletedTroopsToStarport(time);
    }

    private void moveBoughtOutContractsToStarport(List<AbstractBuildContract> boughtOutContracts) {
        for (AbstractBuildContract buildContract : boughtOutContracts) {
            this.troopContracts.remove(buildContract);
            this.starport.addTroopContract(buildContract);
        }
    }

    @Override
    public void loadTroopsForTransport(Map<String, StorageAmount> storage) {
        storage.clear();
        Iterator<Map.Entry<String,Integer>> troopIterator = this.starport.getTroops().entrySet().iterator();
        while(troopIterator.hasNext()) {
            Map.Entry<String,Integer> entry = troopIterator.next();
            StorageAmount storageAmount = new StorageAmount();
            storageAmount.amount = entry.getValue().intValue();
            storageAmount.capacity = -1;
            storageAmount.scale = ServiceFactory.instance().getGameDataManager().getTroopDataByUid(entry.getKey()).getSize();
            storage.put(entry.getKey(), storageAmount);
        }
    }

    @Override
    public void loadContracts(List<Contract> contracts, long time) {
        contracts.clear();
        for (AbstractBuildContract buildContract : this.troopContracts) {

            Contract contract = new Contract();
            contract.contractType = buildContract.getContractType();
            contract.buildingId = buildContract.getBuildingId();
            contract.uid = buildContract.getUnitTypeId();
            contract.endTime = buildContract.getEndTime();
            contracts.add(contract);
        }
    }

    @Override
    public void configureForMap(PlayerMap map) {
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        for (Building building : map.buildings) {
            BuildingData buildingData = gameDataManager.getBuildingDataByUid(building.uid);
            if (buildingData != null) {
                configureTransports(buildingData);
            } else {
                throw new RuntimeException("Failed to find building data for " + building.uid);
            }
        }
    }

    private void configureTransports(BuildingData buildingData) {
        switch (buildingData.getType()) {
            case "starport" :
                this.starport.addStorage(buildingData.getStorage());
                break;
        }
    }
}
