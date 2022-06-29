package swcnoops.server.session.map;

import swcnoops.server.game.BuildingData;
import swcnoops.server.model.Building;
import swcnoops.server.model.Position;

abstract public class MoveableMapItem implements MapItem {
    private Building building;
    private BuildingData buildingData;

    public MoveableMapItem(Building building, BuildingData buildingData) {
        this.building = building;
        this.buildingData = buildingData;
    }

    @Override
    public String getBuildingKey() {
        return building.key;
    }

    @Override
    public String getBuildingUid() {
        return building.uid;
    }

    @Override
    public BuildingData getBuildingData() {
        return this.buildingData;
    }

    public void moveTo(Position newPosition) {
        this.building.x = newPosition.x;
        this.building.z = newPosition.z;
    }
}
