package swcnoops.server.session.map;

import swcnoops.server.game.BuildingData;
import swcnoops.server.model.Building;
import swcnoops.server.model.Position;
import swcnoops.server.session.PlayerSession;

public interface MapItem {
    String getBuildingKey();
    String getBuildingUid();
    BuildingData getBuildingData();
    Building getBuilding();
    void changeBuildingData(BuildingData buildingData);

    void upgradeComplete(PlayerSession playerSession, String unitId, String tag, long endTime);

    void moveTo(Position newPosition);

    void collect(long time);

    void buildComplete(PlayerSession playerSession, String unitId, String tag, long endTime);
}
