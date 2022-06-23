package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.GameDataManager;
import swcnoops.server.model.Building;
import swcnoops.server.model.PlayerMap;
import swcnoops.server.model.SubStorage;

/**
 * A separate class that configures and loads a players contractManager.
 * This is to keep the contractManager smaller as it was getting too big.
 */
public class ContractManagerLoader {
    public ContractManager createForPlayer(PlayerSettings playerSettings) {
        ContractManager contractManager = this.createForMap(playerSettings.getBaseMap());
        initialiseFromPlayerSettings(contractManager, playerSettings);
        return contractManager;
    }

    private ContractManager createForMap(PlayerMap baseMap) {
        ContractManager  contractManager = new ContractManagerImpl();
        initialiseContractManager(contractManager, baseMap);
        return contractManager;
    }

    /**
     * Transports are the queues and contracts for items that can be built
     *
     * @param contractManager
     * @param map
     */
    private void initialiseContractManager(ContractManager contractManager, PlayerMap map) {
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        for (Building building : map.buildings) {
            BuildingData buildingData = gameDataManager.getBuildingDataByUid(building.uid);
            if (buildingData != null) {
                configureForBuilding(contractManager, building, buildingData);
            }
        }
    }

    private void configureForBuilding(ContractManager contractManager, Building building, BuildingData buildingData) {
        switch (buildingData.getType()) {
            case "factory":
            case "barracks":
            case "cantina":
                contractManager.initialiseContractConstructor(building, buildingData, contractManager.getDeployableTroops());
                break;
            case "hero_mobilizer":
                contractManager.getDeployableHero().addStorage(buildingData.getStorage());
                contractManager.initialiseContractConstructor(building, buildingData, contractManager.getDeployableHero());
                break;
            case "champion_platform":
                contractManager.getDeployableChampion().addStorage(buildingData.getStorage());
                contractManager.initialiseContractConstructor(building, buildingData, contractManager.getDeployableChampion());
                break;
            case "starport":
                contractManager.getDeployableTroops().addStorage(buildingData.getStorage());
                break;
            case "fleet_command":
                contractManager.getDeployableSpecialAttack().addStorage(buildingData.getStorage());
                contractManager.initialiseContractConstructor(building, buildingData, contractManager.getDeployableSpecialAttack());
                break;
        }
    }

    private void initialiseFromPlayerSettings(ContractManager contractManager, PlayerSettings playerSettings) {
        initialiseContracts(contractManager, playerSettings.getBuildContracts());

        // TODO - load troops that are ready
        SubStorage subStorage = playerSettings.getTroopsOnTransport();
    }

    private void initialiseContracts(ContractManager contractManager, BuildContracts buildContracts) {
        if (buildContracts != null) {
            // we sort it by endTime as that would of been the order in each transports queue
            buildContracts.stream().sorted((a,b) -> a.compareEndTime(b));
            for (BuildContract buildContract : buildContracts) {
                this.initialiseContract(contractManager, buildContract);
            }
        }
    }

    private void initialiseContract(ContractManager contractManager, BuildContract buildContract) {
        contractManager.initialiseBuildContract(buildContract);
    }
}
