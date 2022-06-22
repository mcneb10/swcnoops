package swcnoops.server.session;

import swcnoops.server.datasource.Player;
import swcnoops.server.model.Contract;
import swcnoops.server.model.DeploymentRecord;
import swcnoops.server.model.PlayerMap;
import swcnoops.server.model.SubStorage;

import java.util.List;
import java.util.Map;

public interface PlayerSession {
    Player getPlayer();

    String getPlayerId();

    void trainTroops(String constructor, String unitTypeId, int quantity, long time);

    void cancelTrainTroops(String constructor, String unitTypeId, int quantity, long time);

    void buyOutTrainTroops(String constructor, String unitTypeId, int quantity, long time);

    void loadContracts(List<Contract> contracts, long time);

    void configureForMap(PlayerMap map);

    void removeDeployedTroops(Map<String, Integer> deployablesToRemove, long time);
    void removeDeployedTroops(List<DeploymentRecord> deployablesToRemove, long time);

    void playerBattleStart(long time);
    void onboardTransports(long time);

    void loadTransports(SubStorage subStorage);

}
