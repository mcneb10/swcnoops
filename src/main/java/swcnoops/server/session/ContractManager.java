package swcnoops.server.session;

import java.util.List;

public interface ContractManager {
    void trainTroops(String buildingId, String unitTypeId, int quantity, long startTime);
    void cancelTrainTroops(String buildingId, String unitTypeId, int quantity, long time);
    void buyOutTrainTroops(String buildingId, String unitTypeId, int quantity, long time);
    void moveCompletedTroops(long clientTime);
    List<AbstractBuildContract> getAllTroopContracts();
}
