package swcnoops.server.session.map;

import swcnoops.server.game.BuildingData;
import swcnoops.server.model.Building;

public class NavigationCenter extends MoveableMapItem{
    public NavigationCenter(Building building, BuildingData buildingData) {
        super(building, buildingData);
    }
}
