package swcnoops.server.session.map;

import swcnoops.server.ServiceFactory;
import swcnoops.server.game.BuildingData;
import swcnoops.server.model.Building;
import swcnoops.server.model.Position;
import swcnoops.server.session.PlayerSession;

abstract public class AbstractMapItem implements MapItem {
    private Building building;
    private BuildingData buildingData;

    public AbstractMapItem(Building building, BuildingData buildingData) {
        this.building = building;
        this.buildingData = buildingData;
    }

    @Override
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

    public void upgradeComplete(PlayerSession playerSession, String unitId, String tag) {
        BuildingData upgradeBuildingData = ServiceFactory.instance().getGameDataManager()
                .getBuildingDataByUid(unitId);
        changeBuildingData(upgradeBuildingData);
    }

    @Override
    public void buildComplete(PlayerSession playerSession, String unitId, String tag) {

    }

    @Override
    public void changeBuildingData(BuildingData buildingData) {
        this.getBuilding().uid = buildingData.getUid();
        this.buildingData = buildingData;
    }
}
