package swcnoops.server.session.creature;

import swcnoops.server.game.BuildingData;
import swcnoops.server.game.TrapData;
import swcnoops.server.model.Building;

public class CreatureDataMap {
    final public Building building;
    final public BuildingData buildingData;
    final public TrapData trapData;

    protected CreatureDataMap(Building building, BuildingData buildingData, TrapData trapData) {
        this.building = building;
        this.buildingData = buildingData;
        this.trapData = trapData;
    }
}
