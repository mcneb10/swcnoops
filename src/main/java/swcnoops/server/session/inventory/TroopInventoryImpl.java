package swcnoops.server.session.inventory;

import swcnoops.server.ServiceFactory;
import swcnoops.server.game.GameDataManager;
import swcnoops.server.game.TroopData;
import swcnoops.server.session.PlayerSession;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TroopInventoryImpl implements TroopInventory {
    private Map<String,TroopData> playersTroopsByUnitId = new HashMap<>();
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

    @Override
    public void upgradeStart(String buildingId, String troopUid, long time) {
        if (this.troops != null) {
            TroopData troopData = ServiceFactory.instance().getGameDataManager().getTroopDataByUid(troopUid);
            long endTime = time + troopData.getUpgradeTime();
            TroopUpgrade troopUpgrade = new TroopUpgrade(buildingId, troopUid, endTime);
            this.troops.getUpgrades().add(troopUpgrade);
        }
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
    public void processCompletedUpgrades(long time) {
        this.getTroops().getUpgrades().sort((a,b) -> Long.compare(a.getEndTime(), b.getEndTime()));
        Iterator<TroopUpgrade> troopUpgradeIterator = this.getTroops().getUpgrades().iterator();
        while(troopUpgradeIterator.hasNext()) {
            TroopUpgrade troopUpgrade = troopUpgradeIterator.next();
            if (troopUpgrade.getEndTime() <= time) {
                troopUpgradeIterator.remove();
                TroopData troopData = ServiceFactory.instance().getGameDataManager()
                        .getTroopDataByUid(troopUpgrade.getTroopUnitId());

                // TODO - I feel something has to be done before this for this to work properly for all scenarios
                // will need to update all the builds to properly reflect this upgrade
                // just not sure what, needs to work on playerLogin, as well as normal running
                if (troopData.isSpecialAttack())
                    this.troops.getSpecialAttacks().put(troopData.getUnitId(), Integer.valueOf(troopData.getLevel()));
                else
                    this.troops.getTroops().put(troopData.getUnitId(), Integer.valueOf(troopData.getLevel()));
            }
        }
    }
}
