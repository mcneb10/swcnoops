package swcnoops.server.session.buildings;

import swcnoops.server.game.BuildingData;
import swcnoops.server.model.Building;

public class SquadBuilding implements MapItem {
    private Building building;
    private BuildingData buildingData;

    public SquadBuilding(Building building, BuildingData buildingData) {
        this.building = building;
        this.buildingData = buildingData;
    }

    @Override
    public String getBuildingId() {
        return building.uid;
    }

    @Override
    public BuildingData getBuildingData() {
        return this.buildingData;
    }
}
