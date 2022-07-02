package swcnoops.server.session.map;

import swcnoops.server.game.BuildingData;
import swcnoops.server.model.Building;

public class MoveableBuilding extends MoveableMapItem {
    public MoveableBuilding(Building building, BuildingData buildingData) {
        super(building, buildingData);
    }
}
