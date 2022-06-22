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
    final private Starport starport;

    final private ContractManager contractManager;

    public PlayerSessionImpl(Player player) {
        this.player = player;
        this.starport = new Starport();
        this.contractManager = new ContractManagerImpl(this.starport);

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
        this.contractManager.trainTroops(buildingId, unitTypeId, quantity, startTime);
    }

    @Override
    public void cancelTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        this.contractManager.cancelTrainTroops(buildingId, unitTypeId, quantity, time);
    }

    // TODO
    @Override
    public void buyOutTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        this.contractManager.buyOutTrainTroops(buildingId, unitTypeId, quantity, time);
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
        for (AbstractBuildContract buildContract : this.contractManager.getAllTroopContracts()) {
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
