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
    protected ContractManager createForMap(PlayerMap baseMap) {
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
                contractManager.addContractConstructor(building, buildingData);
                break;
            case "hero_mobilizer":
            case "champion_platform":
                contractManager.addContractConstructor(building, buildingData);
                break;
            case "starport":
                contractManager.getTroopsTransport().addStorage(buildingData.getStorage());
                contractManager.addContractConstructor(building, buildingData);
                break;
            case "fleet_command":
                contractManager.getSpecialAttackTransport().addStorage(buildingData.getStorage());
                contractManager.addContractConstructor(building, buildingData);
                break;
        }
    }

    protected void loadPlayerSettings(ContractManager contractManager, PlayerSettings playerSettings) {
        loadContracts(contractManager, playerSettings.getBuildContracts());

        // TODO - load troops that are ready
        SubStorage subStorage = playerSettings.getTroopsOnTransport();
    }

    private void loadContracts(ContractManager contractManager, BuildContracts buildContracts) {
        if (buildContracts != null) {
            // we sort it by endTime as that would of been the order in each transports queue
            buildContracts.stream().sorted((a,b) -> a.compareEndTime(b));
            for (BuildContract buildContract : buildContracts) {
                this.loadContract(contractManager, buildContract);
            }
        }
    }

    private void loadContract(ContractManager contractManager, BuildContract buildContract) {
        contractManager.loadBuildContract(buildContract);
    }
}
