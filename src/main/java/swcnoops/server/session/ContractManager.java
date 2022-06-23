package swcnoops.server.session;

import swcnoops.server.game.BuildingData;
import swcnoops.server.model.Building;
import swcnoops.server.model.DeploymentRecord;

import java.util.List;
import java.util.Map;

public interface ContractManager {
    void trainTroops(String buildingId, String unitTypeId, int quantity, long startTime);
    void cancelTrainTroops(String buildingId, String unitTypeId, int quantity, long time);
    void buyOutTrainTroops(String buildingId, String unitTypeId, int quantity, long time);
    void moveCompletedTroops(long clientTime);

    TroopsTransport getTroopsTransport();
    TroopsTransport getSpecialAttackTransport();
    TroopsTransport getHeroTransport();
    TroopsTransport getChampionTransport();

    void initialiseContractConstructor(Building building, BuildingData buildingData, TroopsTransport troopsTransport);
    void initialiseBuildContract(BuildContract buildContract);

    void removeDeployedTroops(Map<String, Integer> deployablesToRemove);

    void removeDeployedTroops(List<DeploymentRecord> deployablesToRemove);
}
