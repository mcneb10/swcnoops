package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.Player;
import swcnoops.server.datasource.PlayerSettings;
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
    final private PlayerSettings playerSettings;

    public PlayerSessionImpl(Player player, PlayerSettings playerSettings) {
        this.player = player;
        this.playerSettings = playerSettings;
        this.troopsTransport = new TroopsTransport();
        this.specialAttackTransport = new TroopsTransport();
        this.heroTransport = new TroopsTransport(3);
        this.championTransport = new TroopsTransport(2);
        this.contractManager = new ContractManagerImpl(this.troopsTransport,
                this.specialAttackTransport, this.heroTransport, this.championTransport);

        initialiseSession();
    }

    private void initialiseSession() {
        this.setupTransports(this.getBaseMap());
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
        saveSession();
    }

    private void saveSession() {
        ServiceFactory.instance().getPlayerDatasource().savePlayerSession(this);
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
            storageAmount.scale = ServiceFactory.instance().getGameDataManager()
                    .getTroopDataByUid(entry.getKey()).getSize();
            storage.put(entry.getKey(), storageAmount);
        }
    }

    @Override
    public void loadContracts(List<Contract> contracts, long time) {
        contracts.clear();
        for (BuildContract buildContract : this.contractManager.getAllTroopContracts()) {
            Contract contract = new Contract();
            contract.contractType = buildContract.getContractGroup().getBuildableData().getContractType();
            contract.buildingId = buildContract.getBuildingId();
            contract.uid = buildContract.getUnitTypeId();
            contract.endTime = buildContract.getEndTime();
            contracts.add(contract);
        }
    }

    public List<BuildContract> getAllBuildContracts() {
        return this.contractManager.getAllTroopContracts();
    }

    /**
     * Transports are the queues and contracts for items that can be built
     * @param map
     */
    private void setupTransports(PlayerMap map) {
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        for (Building building : map.buildings) {
            BuildingData buildingData = gameDataManager.getBuildingDataByUid(building.uid);
            if (buildingData != null) {
                configureForBuilding(building, buildingData);
            }
        }

        setupContracts();
    }

    private void setupContracts() {
//        this.loadTroopsForTransport(this.troopsTransport, subStorage.troop.storage);
//        this.loadTroopsForTransport(this.specialAttackTransport, subStorage.specialAttack.storage);
//        this.loadTroopsForTransport(this.heroTransport, subStorage.hero.storage);
//        this.loadTroopsForTransport(this.championTransport, subStorage.champion.storage);
        List<BuildContract> emptyContracts = new ArrayList<>();
        Map<String, Integer> emptyOnBoard = new HashMap<>();
        this.troopsTransport.getTroopsInQueue().clear();
        this.troopsTransport.getTroopsInQueue().addAll(emptyContracts);
        this.troopsTransport.getTroopsOnBoard().clear();
        this.troopsTransport.getTroopsOnBoard().putAll(emptyOnBoard);
    }

    private void configureForBuilding(Building building, BuildingData buildingData) {
        switch (buildingData.getType()) {
            case "factory":
            case "barracks":
                this.contractManager.addContractConstructor(building, buildingData);
                break;
            case "hero_mobilizer":
            case "champion_platform":
                this.contractManager.addContractConstructor(building, buildingData);
                break;
            case "starport":
                this.troopsTransport.addStorage(buildingData.getStorage());
                this.contractManager.addContractConstructor(building, buildingData);
                break;
            case "fleet_command":
                this.specialAttackTransport.addStorage(buildingData.getStorage());
                this.contractManager.addContractConstructor(building, buildingData);
                break;
        }
    }

    @Override
    public PlayerMap getBaseMap() {
        return playerSettings.getBaseMap();
    }
}
