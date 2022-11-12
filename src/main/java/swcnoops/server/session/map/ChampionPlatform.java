package swcnoops.server.session.map;

import swcnoops.server.ServiceFactory;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.TroopData;
import swcnoops.server.model.Building;
import swcnoops.server.session.PlayerSession;

/**
 * Models the dekas platform where if the platform is upgraded then so does the deka.
 */
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

        // TODO - when a new platform is built the deployableQueue.totalDeployable is not including the
        // platform and the deka (although does not seem to impact anything really)
        // should clean this up properly.
        playerSession.getTrainingManager().getDeployableChampion().getDeployableUnits()
                .put(troopData.getUnitId(), new Integer(1));
        playerSession.getTroopInventory().upgradeTroop(troopData, endTime);
    }

    @Override
    public void buildComplete(PlayerSession playerSession, String unitId, String tag, long endTime) {
        upgradeComplete(playerSession, unitId, tag, endTime);
    }
}
