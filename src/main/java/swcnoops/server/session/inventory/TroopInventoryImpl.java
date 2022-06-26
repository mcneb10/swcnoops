package swcnoops.server.session.inventory;

import swcnoops.server.ServiceFactory;
import swcnoops.server.game.GameDataManager;
import swcnoops.server.game.TroopData;
import swcnoops.server.session.PlayerSession;

import java.util.HashMap;
import java.util.Map;

public class TroopInventoryImpl implements TroopInventory {
    final private Map<String,TroopData> playersTroopsByUnitId = new HashMap<>();
    final private PlayerSession playerSession;
    private Troops troops;

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
    public TroopData getTroopByUnitIdEffectiveFrom(String unitId, long fromTime) {
        TroopData troopData = getTroopByUnitId(unitId);

        // see when was this troop was effective for this player
        TroopRecord troopRecord = this.getTroops().getTroopRecords().get(unitId);
        if (troopRecord != null) {
            if (troopRecord.getEffectiveTime() <= fromTime) {
                if (troopData.getLevel() != troopRecord.getLevel()) {
                    int previousLevel = troopRecord.getLevel() - 1;
                    if (previousLevel <= 0)
                        previousLevel = 1;

                    troopData = ServiceFactory.instance().getGameDataManager()
                            .getTroopDataByUnitId(unitId, previousLevel);
                }
            }
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
        upgradeTroop(troopData);
    }

    @Override
    public Troops getTroops() {
        return troops;
    }

    @Override
    public void setTroops(Troops troops) {
        this.troops = troops;
    }

    @Override
    public void upgradeTroop(TroopData troopData) {
        if (troopData != null)
            this.playersTroopsByUnitId.put(troopData.getUnitId(), troopData);
    }
}
