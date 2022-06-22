package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.Player;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.GameDataManager;
import swcnoops.server.model.*;

import java.util.*;

public class PlayerSessionImpl implements PlayerSession {
    final private Player player;
    final private TroopsTransport troopsTransport;
    final private TroopsTransport specialAttackTransport;
    final private TroopsTransport heroTransport;
    final private TroopsTransport championTransport;

    final private ContractManager contractManager;

    public PlayerSessionImpl(Player player) {
        this.player = player;
        this.troopsTransport = new TroopsTransport();
        this.specialAttackTransport = new TroopsTransport();
        this.heroTransport = new TroopsTransport(3);
        this.championTransport = new TroopsTransport(2);
        this.contractManager = new ContractManagerImpl(this.troopsTransport,
                this.specialAttackTransport, this.heroTransport, this.championTransport);
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

    @Override
    public void buyOutTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        this.contractManager.buyOutTrainTroops(buildingId, unitTypeId, quantity, time);
    }

    @Override
    public void removeDeployedTroops(Map<String, Integer> deployablesToRemove, long time) {
        if (deployablesToRemove != null) {
            this.troopsTransport.removeTroopsOnBoard(deployablesToRemove);
            this.specialAttackTransport.removeTroopsOnBoard(deployablesToRemove);
            this.heroTransport.removeTroopsOnBoard(deployablesToRemove);
            this.championTransport.removeTroopsOnBoard(deployablesToRemove);
        }
    }
//CreatureDeployed
    @Override
    public void removeDeployedTroops(List<DeploymentRecord> deployablesToRemove, long time) {
        if (deployablesToRemove != null) {
            for (DeploymentRecord deploymentRecord : deployablesToRemove) {
                switch (deploymentRecord.getAction()) {
                    case "HeroDeployed":
                        this.heroTransport.removeTroopsOnBoard(deploymentRecord.getUid(), Integer.valueOf(1));
                        break;
                    case "TroopPlaced":
                        this.troopsTransport.removeTroopsOnBoard(deploymentRecord.getUid(), Integer.valueOf(1));
                        break;
                    case "SpecialAttackDeployed":
                        this.specialAttackTransport.removeTroopsOnBoard(deploymentRecord.getUid(), Integer.valueOf(1));
                        break;
                    case "ChampionDeployed":
                        this.championTransport.removeTroopsOnBoard(deploymentRecord.getUid(), Integer.valueOf(1));
                        break;
                }
            }
        }
    }

    @Override
    public void playerBattleStart(long time) {
        this.contractManager.moveCompletedTroops(time);
    }

    @Override
    public void onboardTransports(long time) {
        this.contractManager.moveCompletedTroops(time);
    }

    @Override
    public void loadTransports(SubStorage subStorage) {
        this.loadTroopsForTransport(this.troopsTransport, subStorage.troop.storage);
        this.loadTroopsForTransport(this.specialAttackTransport, subStorage.specialAttack.storage);
        this.loadTroopsForTransport(this.heroTransport, subStorage.hero.storage);
        this.loadTroopsForTransport(this.championTransport, subStorage.champion.storage);
    }

    private void loadTroopsForTransport(TroopsTransport transport, Map<String, StorageAmount> storage) {
        storage.clear();
        Iterator<Map.Entry<String,Integer>> troopIterator = transport.getTroopsOnBoard().entrySet().iterator();
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
        // TODO - redo this as at the moment no nice way of knowing when
        // to initialise the players session
        this.troopsTransport.resetStorage();
        this.specialAttackTransport.resetStorage();

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
            case "starport":
                this.troopsTransport.addStorage(buildingData.getStorage());
                break;
            case "fleet_command":
                this.specialAttackTransport.addStorage(buildingData.getStorage());
                break;
        }
    }
}
