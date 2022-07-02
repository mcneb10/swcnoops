package swcnoops.server.session.map;

import swcnoops.server.game.BuildingData;

public interface MapItem {
    String getBuildingKey();
    String getBuildingUid();
    BuildingData getBuildingData();
}
