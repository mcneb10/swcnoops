package swcnoops.server.session.map;

import swcnoops.server.ServiceFactory;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.TroopData;
import swcnoops.server.model.Building;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.inventory.TroopRecord;
import swcnoops.server.session.inventory.Troops;

public class ChampionPlatform extends MapItemImpl {
    public ChampionPlatform(Building building, BuildingData buildingData) {
        super(building, buildingData);
    }

    @Override
    public void upgradeComplete(PlayerSession playerSession, String unitId, String tag, long endTime) {
        super.upgradeComplete(playerSession, unitId, tag, endTime);

        // set the deployable for it to be alive after build and upgrade our troop inventory for the deka
        TroopData troopData = ServiceFactory.instance().getGameDataManager()
                .getTroopDataByUid(this.getBuildingData().getLinkedUnit());

        playerSession.getTrainingManager().getDeployableChampion().getDeployableUnits()
                .put(troopData.getUnitId(), new Integer(1));

        Troops troops = playerSession.getTroopInventory().getTroops();
        TroopRecord troopRecord = new TroopRecord(troopData.getLevel(), endTime);
        troops.getTroops().put(troopData.getUnitId(), troopRecord);
        troops.getTroopRecords().put(troopData.getUnitId(), troopRecord);
        playerSession.getTroopInventory().upgradeTroop(troopData);
    }

    @Override
    public void buildComplete(PlayerSession playerSession, String unitId, String tag, long endTime) {
        upgradeComplete(playerSession, unitId, tag, endTime);
    }
}
