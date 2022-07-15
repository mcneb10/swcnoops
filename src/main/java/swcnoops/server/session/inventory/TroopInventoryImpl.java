package swcnoops.server.session.inventory;

import swcnoops.server.ServiceFactory;
import swcnoops.server.game.GameDataManager;
import swcnoops.server.game.TroopData;
import swcnoops.server.session.PlayerSession;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a map of the troops keyed by the unitId instead of the uid.
 * This is to allow us to change the TroopData to effect already built troops for them to be upgraded too.
 */
public class TroopInventoryImpl implements TroopInventory {
    final private Map<String,TroopData> playersTroopsByUnitId = new HashMap<>();
    final private PlayerSession playerSession;
    private Troops playerTroopSettings;

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
            // if this version has the effect then we want this one
            if (troopRecord.getEffectiveTime() <= fromTime) {
                if (troopData.getLevel() != troopRecord.getLevel()) {
                    troopData = ServiceFactory.instance().getGameDataManager()
                            .getTroopDataByUnitId(unitId, troopRecord.getLevel());
                }
            } else {
                // if this version is not the effective one then we want the previous version
                int previousLevel = troopRecord.getLevel() - 1;
                if (previousLevel <= 0)
                    previousLevel = 1;

                troopData = ServiceFactory.instance().getGameDataManager()
                        .getTroopDataByUnitId(unitId, previousLevel);
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
    public Troops getTroops() {
        return playerTroopSettings;
    }

    @Override
    public void initialise(Troops troops) {
        this.playerTroopSettings = troops;
        this.initialise();
    }

    private void initialise() {
        this.playerTroopSettings.getTroopRecords().forEach((a, b) -> this.initialiseTroopRecord(a, b));
    }

    private void initialiseTroopRecord(String unitId, TroopRecord troopRecord) {
        TroopData troopData = ServiceFactory.instance().getGameDataManager()
                .getTroopDataByUnitId(unitId, troopRecord.getLevel());
        this.playersTroopsByUnitId.put(troopData.getUnitId(), troopData);
    }

    @Override
    public void upgradeTroop(TroopData troopData, long endTime) {
        if (troopData != null) {
            this.playerTroopSettings.addTroop(troopData, endTime);
            this.playersTroopsByUnitId.put(troopData.getUnitId(), troopData);
        }
    }
}
