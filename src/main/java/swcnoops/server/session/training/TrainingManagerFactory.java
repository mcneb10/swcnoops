package swcnoops.server.session.training;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.game.*;
import swcnoops.server.model.Building;
import swcnoops.server.model.PlayerMap;

/**
 * A separate class that creates, configures and loads a players TrainingManager.
 * This is to keep the contractManager smaller as it was getting too big.
 */
public class TrainingManagerFactory {
    public TrainingManager createForPlayer(PlayerSettings playerSettings) {
        TrainingManager trainingManager = this.createForMap(playerSettings.getBaseMap());
        initialiseFromPlayerSettings(trainingManager, playerSettings);
        return trainingManager;
    }

    private TrainingManager createForMap(PlayerMap baseMap) {
        TrainingManager trainingManager = new TrainingManagerImpl();
        initialise(trainingManager, baseMap);
        return trainingManager;
    }

    /**
     * Transports are the queues and contracts for items that can be built
     *
     * @param trainingManager
     * @param map
     */
    private void initialise(TrainingManager trainingManager, PlayerMap map) {
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        for (Building building : map.buildings) {
            BuildingData buildingData = gameDataManager.getBuildingDataByUid(building.uid);
            if (buildingData != null) {
                configureForBuilding(trainingManager, building, buildingData);
            }
        }
    }

    private void configureForBuilding(TrainingManager trainingManager, Building building, BuildingData buildingData) {
        switch (buildingData.getType()) {
            case "factory":
            case "barracks":
            case "cantina":
                trainingManager.initialiseBuilder(building, buildingData, trainingManager.getDeployableTroops(),
                        ContractType.Troop);
                break;
            case "hero_mobilizer":
                trainingManager.getDeployableHero().addStorage(buildingData.getStorage());
                trainingManager.initialiseBuilder(building, buildingData, trainingManager.getDeployableHero(),
                        ContractType.Hero);
                break;
            case "champion_platform":
                trainingManager.getDeployableChampion().addStorage(buildingData.getStorage());
                trainingManager.initialiseBuilder(building, buildingData, trainingManager.getDeployableChampion(),
                        ContractType.Champion);
                break;
            case "starport":
                trainingManager.getDeployableTroops().addStorage(buildingData.getStorage());
                break;
            case "fleet_command":
                trainingManager.getDeployableSpecialAttack().addStorage(buildingData.getStorage());
                trainingManager.initialiseBuilder(building, buildingData, trainingManager.getDeployableSpecialAttack(),
                        ContractType.SpecialAttack);
                break;
        }
    }

    private void initialiseFromPlayerSettings(TrainingManager trainingManager, PlayerSettings playerSettings) {
        initialiseBuildContracts(trainingManager, playerSettings.getBuildContracts());
        trainingManager.initialiseDeployables(playerSettings.getDeployableTroops());
    }

    private void initialiseBuildContracts(TrainingManager trainingManager, BuildUnits buildUnits) {
        if (buildUnits != null) {
            buildUnits.stream().sorted((a, b) -> a.compareEndTime(b));
            for (BuildUnit buildUnit : buildUnits) {
                trainingManager.initialiseBuildUnit(buildUnit);
            }
        }
    }
}
