package swcnoops.server.session.buildings;

import swcnoops.server.game.BuildingData;

public interface MapItem {
    String getBuildingId();
    BuildingData getBuildingData();
}
