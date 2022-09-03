package swcnoops.server.session.map;

import swcnoops.server.ServiceFactory;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.BuildingType;
import swcnoops.server.model.Building;
import swcnoops.server.session.PlayerSession;

public class HeadQuarter extends MapItemImpl {
    public HeadQuarter(Building building, BuildingData buildingData)
    {
        super(building, buildingData);
    }

    @Override
    public void upgradeComplete(PlayerSession playerSession, String unitId, String tag, long endTime) {
        super.upgradeComplete(playerSession, unitId, tag, endTime);

        // if we have upgraded to prestige then we need to change the droidHut and add a prestige droid to our inventory
        if (this.getBuildingData().getLevel() == 11) {
            MapItem droidHut = playerSession.getPlayerMapItems().getMapItemByType(BuildingType.droid_hut);
            if (droidHut.getBuildingData().getLevel() == 1) {
                BuildingData nextDroidHut = ServiceFactory.instance().getGameDataManager().getBuildingData(droidHut.getBuildingData().getType(), droidHut
                        .getBuildingData().getFaction(), droidHut.getBuildingData().getLevel() + 1);

                droidHut.upgradeComplete(playerSession, nextDroidHut.getUid(), null, endTime);

                // the inventory needs to have these already there as the game needs it otherwise it wont make a prestige droid
                // the value also needs to start off as 0, if it has a value then it will do prestige droid collect even if
                // the player is not at HQ prestige
                playerSession.getPlayerSettings().getInventoryStorage().droids_prestige.amount = 1;
                playerSession.getPlayerSettings().getInventoryStorage().droids_prestige.scale = 1;
                playerSession.getPlayerSettings().getInventoryStorage().droids_prestige.capacity = 1;
            }
        }
    }
}
