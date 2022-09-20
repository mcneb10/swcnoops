package swcnoops.server.session.training;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.Deployables;
import swcnoops.server.game.*;
import swcnoops.server.model.*;
import swcnoops.server.session.CurrencyDelta;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.map.MapItem;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TrainingManagerImpl implements TrainingManager {
    private static final Logger LOG = LoggerFactory.getLogger(TrainingManagerImpl.class);

    final private Map<String, Builder> builders = new HashMap<>();
    final private DeployableQueue troopTransport;
    final private DeployableQueue specialAttackTransport;
    final private DeployableQueue heroTransport;
    final private DeployableQueue championTransport;
    final private PlayerSession playerSession;

    public TrainingManagerImpl(PlayerSession playerSession) {
        this.playerSession = playerSession;
        this.troopTransport = new DeployableQueue();
        this.specialAttackTransport = new DeployableQueue();
        this.heroTransport = new DeployableQueue();
        this.championTransport = new DeployableQueue();
    }

    @Override
    public DeployableQueue getDeployableTroops() {
        return troopTransport;
    }

    @Override
    public DeployableQueue getDeployableSpecialAttack() {
        return specialAttackTransport;
    }

    @Override
    public DeployableQueue getDeployableHero() {
        return heroTransport;
    }

    @Override
    public DeployableQueue getDeployableChampion() {
        return championTransport;
    }

    @Override
    public CurrencyDelta trainTroops(String buildingId, String unitTypeId, int quantity, int credits, int contraband, long startTime)
    {
        // we create build units using the unitId and not the uid of the troop
        // this is to handle upgrades done in the middle of training
        TroopData troopData = ServiceFactory.instance().getGameDataManager().getTroopDataByUid(unitTypeId);
        Builder builder = getBuilder(buildingId);
        CurrencyType trainingCurrency = getTrainingCurrency(troopData);
        int trainCost = getTrainCost(trainingCurrency, troopData);
        int givenTrainCost = calculateGivenTrainingCost(this.playerSession, credits, contraband, trainingCurrency);
        List<BuildUnit> buildUnits = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) {
            BuildUnit buildUnit =
                    new BuildUnit(builder, buildingId, troopData.getUnitId(), trainCost,
                            builder.getContractType(), null);
            buildUnits.add(buildUnit);
        }

        builder.train(buildUnits, startTime);

        return new CurrencyDelta(givenTrainCost, trainCost, trainingCurrency, true);
    }

    private int calculateGivenTrainingCost(PlayerSession playerSession, int credits, int contraband, CurrencyType trainingCurrency) {
        int givenCost = 0;
        if (trainingCurrency != null) {
            InventoryStorage inventoryStorage = playerSession.getInventoryStorage();
            switch (trainingCurrency) {
                case credits:
                    givenCost = inventoryStorage.credits.amount - credits;
                    break;
                case contraband:
                    givenCost = inventoryStorage.contraband.amount - contraband;
                    break;
            }
        }

        return givenCost;
    }

    private CurrencyType getTrainingCurrency(TroopData troopData) {
        CurrencyType currencyType = CurrencyType.credits;
        if (troopData.getType() != null) {
            switch (troopData.getType()) {
                case mercenary:
                    currencyType = CurrencyType.contraband;
                    break;
                default:
                    currencyType = CurrencyType.credits;
                    break;
            }
        }
        return currencyType;
    }

    private int getTrainCost(CurrencyType currencyType, TroopData troopData) {
        int cost = 0;
        if (currencyType != null) {
            switch (currencyType) {
                case contraband:
                    cost = troopData.getContraband();
                    break;
                case credits:
                default:
                    cost = troopData.getCredits();
                    break;
            }
        }
        return cost;
    }

    /**
     * we move completed troops last because its possible they managed to remove it
     * while we think it had completed and already moved to be a deployable
     * troops are cancelled from the back of their slot.
     * @param buildingId
     * @param unitTypeId
     * @param quantity
     * @param time
     */
    @Override
    public CurrencyDelta cancelTrainTroops(String buildingId, String unitTypeId, int quantity, int credits, int materials,
                                           int contraband, long time)
    {
        TroopData troopData = ServiceFactory.instance().getGameDataManager().getTroopDataByUid(unitTypeId);
        Builder builder = getBuilder(buildingId);
        List<BuildUnit> cancelledContracts =
                builder.remove(troopData.getUnitId(), quantity, time, false);

        if (cancelledContracts.size() != quantity) {
            LOG.warn("Number of units to cancel " + unitTypeId + " removed " + cancelledContracts.size() + " but expected " + quantity);
        }

        CurrencyType trainingCurrency = getTrainingCurrency(troopData);
        AtomicInteger totalRefund = new AtomicInteger(0);
        int givenDelta = CurrencyHelper.calculateGivenRefund(this.playerSession, credits, materials, contraband, trainingCurrency);
        cancelledContracts.forEach(c -> totalRefund.addAndGet(c.getCost()));
        GameConstants constants = ServiceFactory.instance().getGameDataManager().getGameConstants();
        int expectedRefund = (int) ((float)totalRefund.get() * constants.contract_refund_percentage_troops / 100f);
        int availableStorage = CurrencyHelper.calculateStorageAvailable(trainingCurrency, playerSession);
        if (expectedRefund > availableStorage)
            expectedRefund = availableStorage;
        return new CurrencyDelta(givenDelta, expectedRefund, trainingCurrency, false);
    }

    /**
     * we move completed troops last because its possible they managed to buy it out
     * while we think it had completed and already moved to be a deployable
     * troops bought out are from the front of the slot
     * @param buildingId
     * @param unitTypeId
     * @param quantity
     * @param time
     */
    @Override
    public CurrencyDelta buyOutTrainTroops(String buildingId, String unitTypeId, int quantity, int crystals, long time) {
        TroopData troopData = ServiceFactory.instance().getGameDataManager().getTroopDataByUid(unitTypeId);
        Builder builder = getBuilder(buildingId);
        List<BuildUnit> boughtOutContracts =
                builder.remove(troopData.getUnitId(), quantity, time, true);

        // calculate how much time is being bought out with crystals
        int expectedCrystals = 0;
        if (boughtOutContracts != null && boughtOutContracts.size() > 0) {
            long contractEnd = boughtOutContracts.get(boughtOutContracts.size() - 1).getEndTime();
            int secondsToBuy = (int) (contractEnd - time);
            expectedCrystals = CrystalHelper.secondsToCrystals(secondsToBuy, troopData);
        }

        int givenCrystalsDelta = CrystalHelper.calculateGivenCrystalDeltaToRemove(this.playerSession, crystals);
        return new CurrencyDelta(givenCrystalsDelta,expectedCrystals, CurrencyType.crystals, true);
    }

    @Override
    public void moveCompletedBuildUnits(long clientTime) {
        this.troopTransport.findAndMoveCompletedUnitsToDeployable(clientTime);
        this.specialAttackTransport.findAndMoveCompletedUnitsToDeployable(clientTime);
        this.heroTransport.findAndMoveCompletedUnitsToDeployable(clientTime);
        this.championTransport.findAndMoveCompletedUnitsToDeployable(clientTime);
    }

    @Override
    public void removeDeployedTroops(Map<String, Integer> deployablesToRemove) {
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        Map<String, Integer> aTroopType = new HashMap<>();
        for (Map.Entry<String, Integer> entry : deployablesToRemove.entrySet()) {
            TroopData troopData = gameDataManager.getTroopDataByUid(entry.getKey());
            aTroopType.put(entry.getKey(), entry.getValue());

            if (troopData.isSpecialAttack()) {
                this.specialAttackTransport.removeDeployable(remapTroopUidToUnitId(aTroopType));
            } else {
                switch (troopData.getType()) {
                    case infantry:
                    case vehicle:
                    case mercenary:
                        this.troopTransport.removeDeployable(remapTroopUidToUnitId(aTroopType));
                        break;
                    case hero:
                        this.heroTransport.removeDeployable(remapTroopUidToUnitId(aTroopType));
                        break;
                    case champion:
                        this.championTransport.removeDeployable(remapTroopUidToUnitId(aTroopType));
                        break;
                    default:
                        throw new RuntimeException("Unsupported type for removal " + troopData.getType());
                }
            }

            aTroopType.clear();
        }
    }

    @Override
    public Map<String,Integer> remapTroopUidToUnitId(Map<String, Integer> troopUids) {
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        Map<String,Integer> remapped = new HashMap<>();
        troopUids.forEach((a,b) -> remapped.put(gameDataManager.getTroopDataByUid(a).getUnitId(), b));
        return remapped;
    }

    /**
     * Removal during a battle.
     * TODO - We do not remove champions because if they stay alive then we want to keep them.
     * need to remove them only when they have been killed.
     * @param deployablesToRemove - list of deployment records from client
     */
    @Override
    public void removeDeployedTroops(List<DeploymentRecord> deployablesToRemove) {
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        for (DeploymentRecord deploymentRecord : deployablesToRemove) {
            TroopData troopData = gameDataManager.getTroopDataByUid(deploymentRecord.getUid());
            switch (deploymentRecord.getAction()) {
                case "HeroDeployed":
                    this.heroTransport.removeDeployable(troopData.getUnitId(), Integer.valueOf(1));
                    break;
                case "TroopPlaced":
                    this.troopTransport.removeDeployable(troopData.getUnitId(), Integer.valueOf(1));
                    break;
                case "SpecialAttackDeployed":
                    this.specialAttackTransport.removeDeployable(troopData.getUnitId(), Integer.valueOf(1));
                    break;
            }
        }
    }

    private Builder getBuilder(String buildingId) {
        Builder builder = this.builders.get(buildingId);
        if (builder == null)
            throw new RuntimeException("Constructor for building has not been initialised " + buildingId);
        return builder;
    }

    @Override
    public void initialiseBuilder(MapItem mapItem, DeployableQueue deployableQueue,
                                  ContractType contractType) {
        if (!this.builders.containsKey(mapItem.getBuildingKey())) {
            Builder builder;
            if (mapItem.getBuildingData().getType() == BuildingType.champion_platform)
                builder = new ChampionBuilder(this.playerSession, mapItem, deployableQueue, contractType);
            else
                builder = new Builder(this.playerSession, mapItem, deployableQueue, contractType);
            this.builders.put(builder.getBuildingKey(), builder);
        }
    }

    @Override
    public void initialiseBuildUnit(BuildUnit buildUnit) {
        Builder builder = this.getBuilder(buildUnit.getBuildingId());
        if (builder != null) {
            builder.load(buildUnit);
        }
    }

    @Override
    public void initialiseDeployables(Deployables deployables) {
        initialiseDeployables(this.getDeployableTroops(), deployables.troop);
        initialiseDeployables(this.getDeployableChampion(), deployables.champion);
        initialiseDeployables(this.getDeployableHero(), deployables.hero);
        initialiseDeployables(this.getDeployableSpecialAttack(), deployables.specialAttack);
    }

    private void initialiseDeployables(DeployableQueue deployableQueue, Map<String, Integer> storage) {
        deployableQueue.initialiseDeployableUnits(storage);
    }

    @Override
    public void recalculateContracts(long time) {
        for (Builder builder : this.builders.values()) {
           builder.recalculateBuildUnitTimes(time);
        }
    }

    @Override
    public PlayerSession getPlayerSession() {
        return this.playerSession;
    }
}
