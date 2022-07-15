package swcnoops.server.session.map;

import swcnoops.server.game.BuildingData;
import swcnoops.server.model.Building;
import swcnoops.server.session.PlayerSession;

public class NavigationCenter extends MapItemImpl {
    public NavigationCenter(Building building, BuildingData buildingData) {
        super(building, buildingData);
    }

    @Override
    public void upgradeComplete(PlayerSession playerSession, String unitId, String tag) {
        super.upgradeComplete(playerSession, unitId, tag);
        if (!playerSession.getPlayerSettings().getUnlockedPlanets().contains(tag))
            playerSession.getPlayerSettings().getUnlockedPlanets().add(tag);
    }

    @Override
    public void buildComplete(PlayerSession playerSession, String unitId, String tag) {
        upgradeComplete(playerSession, unitId, tag);
    }
}
