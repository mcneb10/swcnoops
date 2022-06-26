package swcnoops.server.session.training;

import swcnoops.server.datasource.Deployables;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.ContractType;
import swcnoops.server.model.Building;
import swcnoops.server.model.DeploymentRecord;

import java.util.List;
import java.util.Map;

public interface TrainingManager {
    void trainTroops(String buildingId, String unitTypeId, int quantity, long startTime);
    void cancelTrainTroops(String buildingId, String unitTypeId, int quantity, long time);
    void buyOutTrainTroops(String buildingId, String unitTypeId, int quantity, long time);
    void moveCompletedBuildUnits(long clientTime);

    DeployableQueue getDeployableTroops();
    DeployableQueue getDeployableSpecialAttack();
    DeployableQueue getDeployableHero();
    DeployableQueue getDeployableChampion();

    void initialiseBuilder(Building building, BuildingData buildingData, DeployableQueue deployableQueue,
                           ContractType contractType);
    void initialiseBuildUnit(BuildUnit buildUnit);

    void removeDeployedTroops(Map<String, Integer> deployablesToRemove);

    void removeDeployedTroops(List<DeploymentRecord> deployablesToRemove);

    void initialiseDeployables(Deployables deployables);

    void recalculateContracts(long time);
}
