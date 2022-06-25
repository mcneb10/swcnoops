package swcnoops.server.session.inventory;

import swcnoops.server.ServiceFactory;
import swcnoops.server.game.GameDataManager;
import swcnoops.server.game.TroopData;
import swcnoops.server.session.PlayerSession;

import java.util.HashMap;
import java.util.Map;

public class TroopInventoryImpl implements TroopInventory {
    private Map<String,TroopData> playersTroopsByUnitId = new HashMap<>();
    final private PlayerSession playerSession;

    public TroopInventoryImpl(PlayerSession playerSession) {
        this.playerSession = playerSession;
    }

    @Override
    public TroopData getTroopByUnitId(String unitId) {
        TroopData troopData = this.playersTroopsByUnitId.get(unitId);

        if (troopData == null) {
            GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
            troopData = gameDataManager.getLowestLevelTroopDataByUnitId(unitId);
        }

        return troopData;
    }

    @Override
    public void addTroopUid(String uId) {
        TroopData troopData = ServiceFactory.instance().getGameDataManager().getTroopDataByUid(uId);
        if (troopData != null)
            this.playersTroopsByUnitId.put(troopData.getUnitId(), troopData);
    }

    @Override
    public void addTroopByUnitIdAndLevel(String unitId, int level) {
        TroopData troopData = ServiceFactory.instance().getGameDataManager().getTroopDataByUnitId(unitId, level);
        if (troopData != null)
            this.playersTroopsByUnitId.put(unitId, troopData);
    }
}
