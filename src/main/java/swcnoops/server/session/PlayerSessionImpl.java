package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.Player;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.model.*;

import java.util.*;

public class PlayerSessionImpl implements PlayerSession {
    final private Player player;
    private TroopsTransport troopsTransport;
    private TroopsTransport specialAttackTransport;
    private TroopsTransport heroTransport;
    private TroopsTransport championTransport;
    private ContractManager contractManager;
    final private PlayerSettings playerSettings;

    static final private ContractManagerLoader contractManagerLoader = new ContractManagerLoader();

    public PlayerSessionImpl(Player player, PlayerSettings playerSettings) {
        this.player = player;
        this.playerSettings = playerSettings;

        this.contractManager = this.contractManagerLoader.createForMap(this.getBaseMap());
        this.contractManager.initialise(playerSettings);

        this.troopsTransport = this.contractManager.getTroopsTransport();
        this.specialAttackTransport = this.contractManager.getSpecialAttackTransport();
        this.heroTransport = this.contractManager.getHeroTransport();
        this.championTransport = this.contractManager.getChampionTransport();
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

    /**
     * Removing completed contracts during base building
     * @param deployablesToRemove
     * @param time
     */
    @Override
    public void removeDeployedTroops(Map<String, Integer> deployablesToRemove, long time) {
        if (deployablesToRemove != null) {
            this.troopsTransport.removeTroopsOnBoard(deployablesToRemove);
            this.specialAttackTransport.removeTroopsOnBoard(deployablesToRemove);
            this.heroTransport.removeTroopsOnBoard(deployablesToRemove);
            this.championTransport.removeTroopsOnBoard(deployablesToRemove);
        }
    }

    /**
     * This is removal during a battle to remove troops that were used.
     * TODO - CreatureDeployed
     * @param deployablesToRemove
     * @param time
     */
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

    /**
     * Before a battle we move all completed troops to their transport, as those are the troops going to war.
     * We do this as during the battle deployment records are sent which we used to remove what are in the
     * transports.
     * @param time
     */
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

    @Override
    public PlayerMap getBaseMap() {
        return playerSettings.getBaseMap();
    }
}
