package swcnoops.server.session;

import swcnoops.server.datasource.Player;
import swcnoops.server.model.Contract;
import swcnoops.server.model.PlayerMap;
import swcnoops.server.model.StorageAmount;

import java.util.List;
import java.util.Map;

public interface PlayerSession {
    Player getPlayer();

    String getPlayerId();

    void trainTroops(String constructor, String unitTypeId, int quantity, long time);

    void loadTroopsForTransport(Map<String, StorageAmount> storage);

    void cancelTrainTroops(String constructor, String unitTypeId, int quantity, long time);

    void buyOutTrainTroops(String constructor, String unitTypeId, int quantity, long time);

    void loadContracts(List<Contract> contracts, long time);

    void configureForMap(PlayerMap map);
}
