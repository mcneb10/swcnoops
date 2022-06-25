package swcnoops.server.session.training;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.Deployables;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.ContractType;
import swcnoops.server.game.GameDataManager;
import swcnoops.server.game.TroopData;
import swcnoops.server.model.*;
import swcnoops.server.session.PlayerSession;

import java.util.*;

public class TrainingManagerImpl implements TrainingManager {
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
    public void trainTroops(String buildingId, String unitTypeId, int quantity, long startTime) {
        moveCompletedBuildUnits(startTime);

        // we create build units using the unitId and not the uid of the troop
        // this is to handle upgrades done in the middle of training
        TroopData troopData = ServiceFactory.instance().getGameDataManager().getTroopDataByUid(unitTypeId);
        Builder builder = getBuilder(buildingId);
        List<BuildUnit> buildUnits = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) {
            BuildUnit buildUnit = new BuildUnit(builder, buildingId, troopData.getUnitId());
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
        TroopData troopData = ServiceFactory.instance().getGameDataManager().getTroopDataByUid(unitTypeId);
        Builder builder = getBuilder(buildingId);
        List<BuildUnit> cancelledContracts =
                builder.remove(troopData.getUnitId(), quantity, time, true);
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
        TroopData troopData = ServiceFactory.instance().getGameDataManager().getTroopDataByUid(unitTypeId);
        Builder builder = getBuilder(buildingId);
        List<BuildUnit> boughtOutContracts =
                builder.remove(troopData.getUnitId(), quantity, time, false);
        DeployableQueue transport = builder.getDeployableQueue();
        if (transport != null) {
            transport.moveUnitToDeployable(boughtOutContracts);
            transport.sortUnitsInQueue();
            transport.findAndMoveCompletedUnitsToDeployable(time);
        }
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
        Map<String,Integer> remapped = remapTroopUidToUnitId(deployablesToRemove);
        this.troopTransport.removeDeployable(remapped);
        this.specialAttackTransport.removeDeployable(remapped);
        this.heroTransport.removeDeployable(remapped);
        this.championTransport.removeDeployable(remapped);
    }

    private Map<String,Integer> remapTroopUidToUnitId(Map<String, Integer> troopUids) {
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
    public void initialiseBuilder(Building building, BuildingData buildingData, DeployableQueue deployableQueue,
                                  ContractType contractType) {
        if (!this.builders.containsKey(building.key)) {
            Builder builder = new Builder(this.playerSession, building.key, buildingData, deployableQueue, contractType);
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

    @Override
    public void initialiseDeployables(Deployables deployables) {
        initialiseDeployables(this.getDeployableTroops(), deployables.troop);
        initialiseDeployables(this.getDeployableChampion(), deployables.champion);
        initialiseDeployables(this.getDeployableHero(), deployables.hero);
        initialiseDeployables(this.getDeployableSpecialAttack(), deployables.specialAttack);
    }

    private void initialiseDeployables(DeployableQueue deployableQueue, Map<String, Integer> storage) {
        deployableQueue.getDeployableUnits().clear();
        deployableQueue.getDeployableUnits().putAll(storage);
    }
}
