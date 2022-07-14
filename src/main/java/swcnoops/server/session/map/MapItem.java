package swcnoops.server.session.map;

import swcnoops.server.game.BuildingData;
import swcnoops.server.model.Building;
import swcnoops.server.model.Position;

public interface MapItem {
    String getBuildingKey();
    String getBuildingUid();
    BuildingData getBuildingData();
    Building getBuilding();
    void changeBuildingData(BuildingData buildingData);

    void upgradeComplete(String unitId);

    void moveTo(Position newPosition);

    void collect(long time);
}
