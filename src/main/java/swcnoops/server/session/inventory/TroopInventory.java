package swcnoops.server.session.inventory;

import swcnoops.server.game.TroopData;

public interface TroopInventory {
    TroopData getTroopByUnitId(String unitId);
    TroopData getTroopByUnitIdEffectiveFrom(String unitId, long fromTime);

    void addTroopUid(String uId);

    Troops getTroops();

    void initialise(Troops troops);

    void upgradeTroop(TroopData troopData, long endTime);
}
