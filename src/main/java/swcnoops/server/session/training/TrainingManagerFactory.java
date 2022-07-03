package swcnoops.server.session.training;

import swcnoops.server.game.*;
import swcnoops.server.model.Building;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.PlayerMapItems;
import swcnoops.server.session.map.MoveableMapItem;

/**
 * A separate class that creates, configures and loads a players TrainingManager.
 * This is to keep the contractManager smaller as it was getting too big.
 */
public class TrainingManagerFactory {
    public TrainingManager createForPlayer(PlayerSession playerSession) {
        TrainingManager trainingManager = this.create(playerSession);
        initialiseFromPlayerSettings(trainingManager, playerSession);
        return trainingManager;
    }

    private TrainingManager create(PlayerSession playerSession) {
        TrainingManager trainingManager = new TrainingManagerImpl(playerSession);
        initialise(trainingManager, playerSession.getPlayerMapItems());
        return trainingManager;
    }

    /**
     * Transports are the queues and contracts for items that can be built
     *
     * @param trainingManager
     * @param map
     */
    private void initialise(TrainingManager trainingManager, PlayerMapItems map) {
        if (map != null) {
            for (MoveableMapItem moveableMapItem : map.getMapItems()) {
                BuildingData buildingData = moveableMapItem.getBuildingData();
                configureForBuilding(trainingManager, moveableMapItem.getBuilding(), buildingData);
            }
        }
    }

    private void configureForBuilding(TrainingManager trainingManager, Building building, BuildingData buildingData) {
        switch (buildingData.getType()) {
            case factory:
            case barracks:
            case cantina:
                trainingManager.initialiseBuilder(building, buildingData, trainingManager.getDeployableTroops(),
                        ContractType.Troop);
                break;
            case hero_mobilizer:
                trainingManager.getDeployableHero().addStorage(buildingData.getStorage());
                trainingManager.initialiseBuilder(building, buildingData, trainingManager.getDeployableHero(),
                        ContractType.Hero);
                break;
            case champion_platform:
                trainingManager.getDeployableChampion().addStorage(buildingData.getStorage());
                trainingManager.initialiseBuilder(building, buildingData, trainingManager.getDeployableChampion(),
                        ContractType.Champion);
                break;
            case starport:
                trainingManager.getDeployableTroops().addStorage(buildingData.getStorage());
                break;
            case fleet_command:
                trainingManager.getDeployableSpecialAttack().addStorage(buildingData.getStorage());
                trainingManager.initialiseBuilder(building, buildingData, trainingManager.getDeployableSpecialAttack(),
                        ContractType.SpecialAttack);
                break;
        }
    }

    private void initialiseFromPlayerSettings(TrainingManager trainingManager, PlayerSession playerSession) {
        initialiseBuildContracts(trainingManager, playerSession.getPlayerSettings().getBuildContracts());
        trainingManager.initialiseDeployables(playerSession.getPlayerSettings().getDeployableTroops());
    }

    private void initialiseBuildContracts(TrainingManager trainingManager, BuildUnits buildUnits) {
        if (buildUnits != null) {
            buildUnits.stream().sorted((a, b) -> a.compareEndTime(b));
            for (BuildUnit buildUnit : buildUnits) {
                if (!isBuilding(buildUnit.getContractType()))
                    trainingManager.initialiseBuildUnit(buildUnit);
            }
        }
    }

    private boolean isBuilding(ContractType contractType) {
        if (contractType == ContractType.Build)
            return true;
        if (contractType == ContractType.Upgrade)
            return true;

        return false;
    }
}
