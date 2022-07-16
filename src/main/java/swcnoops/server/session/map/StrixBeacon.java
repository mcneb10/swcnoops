package swcnoops.server.session.map;

import swcnoops.server.game.BuildingData;
import swcnoops.server.model.Building;
import swcnoops.server.session.PlayerSession;

/**
 * On completion of the strix beacon a creature is defaults and activated
 */
public class StrixBeacon extends MapItemImpl {
    public StrixBeacon(Building building, BuildingData buildingData) {
        super(building, buildingData);
    }

    @Override
    public void buildComplete(PlayerSession playerSession, String unitId, String tag, long endTime) {
        upgradeComplete(playerSession, unitId, tag, endTime);

        // enable creature and default creature
        playerSession.getCreatureManager().creatureTrapComplete(this, endTime);
    }
}
