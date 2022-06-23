package swcnoops.server.session.training;

import swcnoops.server.game.BuildingData;
import swcnoops.server.model.Building;
import swcnoops.server.model.DeploymentRecord;

import java.util.*;

public class TrainingManagerImpl implements TrainingManager {
    final private Map<String, Builder> builders = new HashMap<>();
    final private DeployableQueue deployableQueue;
    final private DeployableQueue specialAttackTransport;
    final private DeployableQueue heroTransport;
    final private DeployableQueue championTransport;

    protected TrainingManagerImpl()
    {
        this.deployableQueue = new DeployableQueue();
        this.specialAttackTransport = new DeployableQueue();
        this.heroTransport = new DeployableQueue();
        this.championTransport = new DeployableQueue();
    }

    public DeployableQueue getDeployableTroops() {
        return deployableQueue;
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
    public void trainTroops(String buildingId, String unitTypeId, int quantity, long startTime) {
        moveCompletedBuildUnits(startTime);

        // create build units
        Builder builder = getBuilder(buildingId);
        List<BuildUnit> buildUnits = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) {
            BuildUnit buildUnit = new BuildUnit(builder, buildingId, unitTypeId);
            buildUnits.add(buildUnit);
        }

        builder.train(buildUnits, startTime);
        DeployableQueue transport = builder.getDeployableQueue();
        if (transport != null) {
            transport.addUnitsToQueue(buildUnits);
            transport.sortUnitsInQueue();
        }
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
    public void cancelTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        Builder builder = getBuilder(buildingId);
        List<BuildUnit> cancelledContracts =
                builder.remove(unitTypeId, quantity, time, true);
        DeployableQueue transport = builder.getDeployableQueue();
        if (transport != null) {
            transport.removeUnitsFromQueue(cancelledContracts);
            transport.sortUnitsInQueue();
            transport.findAndMoveCompletedUnitsToDeployable(time);
        }
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
    public void buyOutTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        // we move completed troops last because its possible they managed to buy it out
        // while we think it had completed and already moved to be a deployable
        Builder builder = getBuilder(buildingId);
        List<BuildUnit> boughtOutContracts =
                builder.remove(unitTypeId, quantity, time, false);
        DeployableQueue transport = builder.getDeployableQueue();
        if (transport != null) {
            transport.moveUnitToDeployable(boughtOutContracts);
            transport.sortUnitsInQueue();
            transport.findAndMoveCompletedUnitsToDeployable(time);
        }
    }

    @Override
    public void moveCompletedBuildUnits(long clientTime) {
        this.deployableQueue.findAndMoveCompletedUnitsToDeployable(clientTime);
        this.specialAttackTransport.findAndMoveCompletedUnitsToDeployable(clientTime);
        this.heroTransport.findAndMoveCompletedUnitsToDeployable(clientTime);
        this.championTransport.findAndMoveCompletedUnitsToDeployable(clientTime);
    }

    @Override
    public void removeDeployedTroops(Map<String, Integer> deployablesToRemove) {
        this.deployableQueue.removeDeployable(deployablesToRemove);
        this.specialAttackTransport.removeDeployable(deployablesToRemove);
        this.heroTransport.removeDeployable(deployablesToRemove);
        this.championTransport.removeDeployable(deployablesToRemove);
    }

    @Override
    public void removeDeployedTroops(List<DeploymentRecord> deployablesToRemove) {
        for (DeploymentRecord deploymentRecord : deployablesToRemove) {
            switch (deploymentRecord.getAction()) {
                case "HeroDeployed":
                    this.heroTransport.removeDeployable(deploymentRecord.getUid(), Integer.valueOf(1));
                    break;
                case "TroopPlaced":
                    this.deployableQueue.removeDeployable(deploymentRecord.getUid(), Integer.valueOf(1));
                    break;
                case "SpecialAttackDeployed":
                    this.specialAttackTransport.removeDeployable(deploymentRecord.getUid(), Integer.valueOf(1));
                    break;
                case "ChampionDeployed":
                    this.championTransport.removeDeployable(deploymentRecord.getUid(), Integer.valueOf(1));
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
    public void initialiseBuilder(Building building, BuildingData buildingData, DeployableQueue deployableQueue) {
        if (!this.builders.containsKey(building.key)) {
            Builder builder = new Builder(building.key, buildingData, deployableQueue);
            this.builders.put(builder.getBuildingId(), builder);
        }
    }

    @Override
    public void initialiseBuildUnit(BuildUnit buildUnit) {
        Builder builder = this.getBuilder(buildUnit.getBuildingId());
        builder.load(buildUnit);
        DeployableQueue transport = builder.getDeployableQueue();
        transport.addUnitsToQueue(buildUnit);
    }
}
