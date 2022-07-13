package swcnoops.server.session.map;

import swcnoops.server.ServiceFactory;
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

    public Building getBuilding() {
        return building;
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

    public void collect(long time) {
        this.building.currentStorage = 0;
        this.building.lastCollectTime = time;

        // TODO - work out how much to collect and move to inventory
    }

    public void upgradeComplete(String unitId) {
        BuildingData upgradeBuildingData = ServiceFactory.instance().getGameDataManager()
                .getBuildingDataByUid(unitId);
        changeBuildingData(upgradeBuildingData);
    }

    public void changeBuildingData(BuildingData buildingData) {
        this.getBuilding().uid = buildingData.getUid();
        this.buildingData = buildingData;
    }
}
