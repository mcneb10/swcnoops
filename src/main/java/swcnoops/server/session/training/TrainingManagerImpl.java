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
        this.troopTransport = new DeployableQueue(playerSession);
        this.specialAttackTransport = new DeployableQueue(playerSession);
        this.heroTransport = new DeployableQueue(playerSession);
        this.championTransport = new DeployableQueue(playerSession);
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
            InventoryStorage inventoryStorage = playerSession.getInventoryManager().getObjectForReading();
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
     * Any remaining troops will be taken from the deployable queue.
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

        int remainingCancel = 0;
        if (cancelledContracts.size() != quantity) {
            LOG.warn("Number of " + unitTypeId + " units to cancel, removed " + cancelledContracts.size() + " but expected "
                    + quantity + " " + this.playerSession.getPlayerId());

            remainingCancel = quantity - cancelledContracts.size();
            Integer deployedCount = builder.getDeployableQueue().getDeployableUnits().get(troopData.getUnitId());
            if (deployedCount == null || deployedCount < remainingCancel) {
                LOG.warn("There are none or not enough " + unitTypeId + " in deployable " + remainingCancel + " for cancelling "
                        + this.playerSession.getPlayerId());
            }

            if (deployedCount != null) {
                int deployableCancel = Math.min(remainingCancel, deployedCount);
                int deployableRemoved = builder.getDeployableQueue().removeDeployable(troopData.getUnitId(), deployableCancel);
                LOG.warn("Removing " + unitTypeId + " in deployable " + deployableCancel + " for cancelling, and did "
                        + deployableRemoved + " " + this.playerSession.getPlayerId());
            }
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

        if (remainingCancel > 0) {
            LOG.warn("Could not correctly calculate refund as there are uncounted units that were not in the build queue "
                    + remainingCancel + " " + this.playerSession.getPlayerId());
        }

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

        processObjectives(boughtOutContracts);

        int remainingBuyOut = 0;
        if (boughtOutContracts.size() != quantity) {
            LOG.warn("Number of units " + unitTypeId + " to buy out " + boughtOutContracts.size() + " but expected "
                    + quantity + " " + this.playerSession.getPlayerId());

            // the remaining would off been considered completed and be sent as deployable
            remainingBuyOut = quantity - boughtOutContracts.size();
            Integer deployedCount = builder.getDeployableQueue().getDeployableUnits().get(troopData.getUnitId());
            if (deployedCount == null || deployedCount < remainingBuyOut) {
                LOG.warn("There are none or not enough " + unitTypeId + " in deployable " + remainingBuyOut + " for buyout "
                        + this.playerSession.getPlayerId());
            }

            // TODO - we dont do the ones in deployable because if they got in there then they would of been
            // accounted for when moving completed contracts
            // maybe we change this to keep the contracts and have markers on them to indicate
            // if they have been counted towards the objectives
        }

        // calculate how much time is being bought out with crystals
        int expectedCrystals = 0;
        if (boughtOutContracts != null && boughtOutContracts.size() > 0) {
            long contractEnd = boughtOutContracts.get(boughtOutContracts.size() - 1).getEndTime();
            int secondsToBuy = (int) (contractEnd - time);
            expectedCrystals = CrystalHelper.secondsToCrystals(secondsToBuy, troopData);
        }

        if (remainingBuyOut > 0) {
            LOG.warn("Could not correctly calculate buyout as there are remaining units that is in the build queue "
                    + remainingBuyOut + " " + this.playerSession.getPlayerId());
        }

        int givenCrystalsDelta = CrystalHelper.calculateGivenCrystalDeltaToRemove(this.playerSession, crystals);
        return new CurrencyDelta(givenCrystalsDelta, expectedCrystals, CurrencyType.crystals, true);
    }

    @Override
    public void moveCompletedBuildUnits(long clientTime) {
        List<BuildUnit> completed1 = this.troopTransport.findAndMoveCompletedUnitsToDeployable(clientTime);
        List<BuildUnit> completed2 = this.specialAttackTransport.findAndMoveCompletedUnitsToDeployable(clientTime);
        List<BuildUnit> completed3 = this.heroTransport.findAndMoveCompletedUnitsToDeployable(clientTime);
        List<BuildUnit> completed4 = this.championTransport.findAndMoveCompletedUnitsToDeployable(clientTime);

        processObjectives(completed1);
        processObjectives(completed2);
        processObjectives(completed3);
        processObjectives(completed4);
    }

    private void processObjectives(List<BuildUnit> buildUnits) {
        // TODO - find matching objectives
        String planetId = this.getPlayerSession().getPlayerSettings().getBaseMap().planet;
        ObjectiveGroup objectiveGroup = this.getPlayerSession().getPlayerObjectivesManager()
                .getObjectForReading().get(planetId);

        if (buildUnits.size() > 0) {
            if (objectiveGroup != null && objectiveGroup.progress != null) {
                for (ObjectiveProgress objectiveProgress : objectiveGroup.progress) {
                    if (objectiveProgress.state == ObjectiveState.active) {
                        ObjTableData objTableData = getObjTableData(objectiveProgress);
                        if (objTableData.getType() == GoalType.TrainTroopID) {
                            buildUnits.forEach(u -> {
                                if (objTableData.getItem() != null && objTableData.getItem().equals(u.getUnitId())) {
                                    if (objectiveProgress.count < objectiveProgress.target) {
                                        objectiveProgress.count++;
                                        if (objectiveProgress.count == objectiveProgress.target) {
                                            objectiveProgress.state = ObjectiveState.complete;
                                        }
                                    }
                                    this.getPlayerSession().getPlayerObjectivesManager().getObjectForWriting();
                                }
                            });
                        } else if (objTableData.getType() == GoalType.TrainTroopType) {
                            buildUnits.forEach(u -> {
                                TroopData troopData = this.getPlayerSession().getTroopInventory().getTroopByUnitId(u.getUnitId());

                                if (troopData != null && troopData.getType().name().equals(objTableData.getItem())) {
                                    if (objectiveProgress.count < objectiveProgress.target) {
                                        objectiveProgress.count++;
                                        if (objectiveProgress.count == objectiveProgress.target) {
                                            objectiveProgress.state = ObjectiveState.complete;
                                        }
                                    }
                                    this.getPlayerSession().getPlayerObjectivesManager().getObjectForWriting();
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    private ObjTableData getObjTableData(ObjectiveProgress objectiveProgress) {
        ObjTableData objTableData = ServiceFactory.instance().getGameDataManager().getPatchData().getMap(ObjTableData.class)
                .get(objectiveProgress.uid);
        return objTableData;
    }

    @Override
    public void removeDeployedTroops(Map<String, Integer> deployablesToRemove, long time) {
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        Set<Builder> adjustedBuilders = new HashSet<>();
        for (Map.Entry<String, Integer> entry : deployablesToRemove.entrySet()) {
            TroopData troopData = gameDataManager.getTroopDataByUid(entry.getKey());
            DeployableQueue deployableQueue = getDeployable(troopData);
            Set<Builder> builders = this.removeDeployedTroopsWithBuilderAdjustment(deployableQueue, troopData, entry.getValue());
            adjustedBuilders.addAll(builders);
        }

        recalculateBuilders(adjustedBuilders, time);
    }

    /**
     * This will remove troops from deployable queue and any remaining counts will be taken from the builder.
     * This is to handle the scenario that the client has different states to the server.
     * This should keep the server and client in sync in most cases.
     * @param deployableQueue
     * @param troopData
     * @param value
     * @return
     */
    private Set<Builder> removeDeployedTroopsWithBuilderAdjustment(DeployableQueue deployableQueue, TroopData troopData, Integer value) {
        value = Math.abs(value);
        int removed = deployableQueue.removeDeployable(troopData.getUnitId(), value);

        Set<Builder> amendedBuilder = new HashSet<>();

        // does not match going have try and adjust
        if (value != removed) {
            int remaining = value - removed;
            List<BuildUnit> buildUnits = deployableQueue.getNearestBuildUnits(troopData.getUnitId(), remaining);
            for (BuildUnit buildUnit : buildUnits) {
                Builder builder = getBuilder(buildUnit.getBuildingId());
                amendedBuilder.add(builder);
                builder.removeCompletedBuildUnit(buildUnit);
                deployableQueue.removeUnits(buildUnit);
            }

            LOG.warn("Adjusting deployable queue, removing from a builder for unit " + troopData.getUnitId() + ", removing " + remaining +
                    " and did " + buildUnits.size() + " " + this.playerSession.getPlayerId());
        }

        return amendedBuilder;
    }

    private DeployableQueue getDeployable(TroopData troopData) {
        DeployableQueue queue;

        if (troopData.isSpecialAttack()) {
            queue = this.specialAttackTransport;
        } else {
            switch (troopData.getType()) {
                case infantry:
                case vehicle:
                case mercenary:
                    queue = this.troopTransport;
                    break;
                case hero:
                    queue = this.heroTransport;
                    break;
                case champion:
                    queue = this.championTransport;
                    break;
                default:
                    throw new RuntimeException("Unsupported type for removal " + troopData.getType());
            }
        }

        return queue;
    }

    /**
     * Removal during a battle.
     * We do not remove champions because if they stay alive then we want to keep them.
     * need to remove them only when they have been killed.
     * @param deployablesToRemove - list of deployment records from client
     */
    @Override
    public void removeSpentTroops(List<DeploymentRecord> deployablesToRemove, long time) {
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        Set<Builder> adjustedBuilders = new HashSet<>();
        for (DeploymentRecord deploymentRecord : deployablesToRemove) {
            DeployableQueue deployableQueue = getSpentDeployable(deploymentRecord);
            if (deployableQueue != null) {
                TroopData troopData = gameDataManager.getTroopDataByUid(deploymentRecord.getUid());
                Set<Builder> builders = removeDeployedTroopsWithBuilderAdjustment(deployableQueue, troopData, Integer.valueOf(1));
                adjustedBuilders.addAll(builders);
            }
        }

        recalculateBuilders(adjustedBuilders, time);
    }

    private void recalculateBuilders(Set<Builder> adjustedBuilders, long time) {
        Set<DeployableQueue> queues = new HashSet<>();
        adjustedBuilders.forEach(b -> {
            b.recalculateBuildUnitTimes(time);
            queues.add(b.getDeployableQueue());
        });

        queues.forEach( q -> q.sortUnitsInQueue());
    }

    private DeployableQueue getSpentDeployable(DeploymentRecord deploymentRecord) {
        DeployableQueue deployableQueue = null;

        switch (deploymentRecord.getAction()) {
            case "HeroDeployed":
                deployableQueue = this.heroTransport;
                break;
            case "TroopPlaced":
                deployableQueue = this.troopTransport;
                break;
            case "SpecialAttackDeployed":
                deployableQueue = this.specialAttackTransport;
                break;
        }

        return deployableQueue;
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
