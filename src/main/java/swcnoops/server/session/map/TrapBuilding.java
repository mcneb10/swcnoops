package swcnoops.server.session.map;

import swcnoops.server.game.BuildingData;
import swcnoops.server.model.Building;

public class TrapBuilding extends MapItemImpl {
    public TrapBuilding(Building building, BuildingData buildingData) {
        super(building, buildingData);
    }

    @Override
    public void setupForConstruction() {
        this.building.currentStorage = 1;
    }
}