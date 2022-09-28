package swcnoops.server.session.training;

import swcnoops.server.datasource.Deployables;
import swcnoops.server.game.ContractType;
import swcnoops.server.model.DeploymentRecord;
import swcnoops.server.session.CurrencyDelta;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.map.MapItem;

import java.util.List;
import java.util.Map;

public interface TrainingManager {
    CurrencyDelta trainTroops(String buildingId, String unitTypeId, int quantity, int credits, int contraband, long startTime);
    CurrencyDelta cancelTrainTroops(String buildingId, String unitTypeId, int quantity, int credits, int materials, int contraband, long time);
    CurrencyDelta buyOutTrainTroops(String buildingId, String unitTypeId, int quantity, int crystals, long time);
    void moveCompletedBuildUnits(long clientTime);

    DeployableQueue getDeployableTroops();
    DeployableQueue getDeployableSpecialAttack();
    DeployableQueue getDeployableHero();
    DeployableQueue getDeployableChampion();

    void initialiseBuilder(MapItem mapItem, DeployableQueue deployableQueue,
                           ContractType contractType);
    void initialiseBuildUnit(BuildUnit buildUnit);

    void removeDeployedTroops(Map<String, Integer> deployablesToRemove);

    void removeDeployedTroops(List<DeploymentRecord> deployablesToRemove);

    void initialiseDeployables(Deployables deployables);

    void recalculateContracts(long time);

    PlayerSession getPlayerSession();
}
