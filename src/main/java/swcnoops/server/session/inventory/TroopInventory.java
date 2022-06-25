package swcnoops.server.session.inventory;

import swcnoops.server.game.TroopData;

public interface TroopInventory {
    TroopData getTroopByUnitId(String unitId);

    void addTroopUid(String uId);

    void addTroopByUnitIdAndLevel(String unitId, int valueOf);

    void upgradeStart(String buildingId, String troopUid, long time);

    Troops getTroops();

    void setTroops(Troops troops);
}
