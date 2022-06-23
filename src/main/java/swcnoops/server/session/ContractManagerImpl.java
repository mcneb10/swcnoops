package swcnoops.server.session;

import swcnoops.server.game.BuildingData;
import swcnoops.server.model.Building;
import swcnoops.server.model.DeploymentRecord;

import java.util.*;

public class ContractManagerImpl implements ContractManager {
    final private Map<String, ContractConstructor> contractConstructors = new HashMap<>();
    final private DeployableQueue deployableQueue;
    final private DeployableQueue specialAttackTransport;
    final private DeployableQueue heroTransport;
    final private DeployableQueue championTransport;

    public ContractManagerImpl()
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
        moveCompletedTroops(startTime);

        // create contracts for the things that we want to build
        ContractConstructor contractConstructor = getContractConstructor(buildingId);
        List<BuildContract> troopBuildContracts = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) {
            BuildContract buildContract = new BuildContract(contractConstructor, buildingId, unitTypeId);
            troopBuildContracts.add(buildContract);
        }

        contractConstructor.addContracts(troopBuildContracts, startTime);
        DeployableQueue transport = contractConstructor.getDeployableQueue();
        if (transport != null) {
            transport.addUnitsToQueue(troopBuildContracts);
            transport.sortUnitsInQueue();
        }
    }

    @Override
    public void cancelTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        // we move completed troops last because if they managed to remove contracts
        // at the head of the queue, then they must of got there before it was completed
        ContractConstructor contractConstructor = getContractConstructor(buildingId);
        List<BuildContract> cancelledContracts =
                contractConstructor.removeContracts(unitTypeId, quantity, time, true);
        DeployableQueue transport = contractConstructor.getDeployableQueue();
        if (transport != null) {
            transport.removeUnitsFromQueue(cancelledContracts);
            transport.sortUnitsInQueue();
            transport.findAndMoveCompletedUnitsToDeployable(time);
        }
    }

    @Override
    public void buyOutTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        //we move completed troops last because if they managed to buy out contracts
        //then they must of got there before it was completed
        ContractConstructor contractConstructor = getContractConstructor(buildingId);
        List<BuildContract> boughtOutContracts =
                contractConstructor.removeContracts(unitTypeId, quantity, time, false);
        DeployableQueue transport = contractConstructor.getDeployableQueue();
        if (transport != null) {
            transport.moveUnitToDeployable(boughtOutContracts);
            transport.sortUnitsInQueue();
            transport.findAndMoveCompletedUnitsToDeployable(time);
        }
    }

    @Override
    public void moveCompletedTroops(long clientTime) {
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

    private ContractConstructor getContractConstructor(String buildingId) {
        ContractConstructor contractConstructor = this.contractConstructors.get(buildingId);
        if (contractConstructor == null)
            throw new RuntimeException("Constructor for building has not been initialised " + buildingId);
        return contractConstructor;
    }

    @Override
    public void initialiseContractConstructor(Building building, BuildingData buildingData, DeployableQueue deployableQueue) {
        if (!this.contractConstructors.containsKey(building.key)) {
            ContractConstructor contractConstructor = new ContractConstructor(building.key, buildingData, deployableQueue);
            this.contractConstructors.put(contractConstructor.getBuildingId(), contractConstructor);
        }
    }

    @Override
    public void initialiseBuildContract(BuildContract buildContract) {
        ContractConstructor contractConstructor = this.getContractConstructor(buildContract.getBuildingId());
        contractConstructor.loadContract(buildContract);
        DeployableQueue transport = contractConstructor.getDeployableQueue();
        transport.addUnitsToQueue(buildContract);
    }
}
