package swcnoops.server.session.buildings;

import swcnoops.server.game.BuildingData;
import swcnoops.server.model.Building;

public class HeadQuarter implements MapItem {
    private Building building;
    private BuildingData buildingData;

    public HeadQuarter(Building building, BuildingData buildingData) {
        this.building = building;
        this.buildingData = buildingData;
    }

    @Override
    public String getBuildingId() {
        return building.uid;
    }

    public Building getBuilding() {
        return building;
    }

    @Override
    public BuildingData getBuildingData() {
        return buildingData;
    }
}
