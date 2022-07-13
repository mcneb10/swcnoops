package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.BuildingType;
import swcnoops.server.model.Building;
import swcnoops.server.model.PlayerMap;
import swcnoops.server.model.Position;
import swcnoops.server.session.map.MoveableBuilding;
import swcnoops.server.session.map.MoveableMapItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is a wrapper around a map to give easy access and manipulation to handle upgrades
 */
public class PlayerMapItems {
    /**
     * Lookup by key
     */
    final private Map<String, MoveableMapItem> mapItemsByKey = new HashMap<>();
    final private Map<BuildingType, MoveableMapItem> mapItemsByType = new HashMap<>();
    /**
     * This is the real map that is from the DB
     */
    final private PlayerMap map;
    public PlayerMapItems(PlayerMap playerMap) {
        this.map = playerMap;
    }

    public void add(String buildingKey, MoveableMapItem mapItem) {
        this.mapItemsByKey.put(buildingKey, mapItem);

        switch (mapItem.getBuildingData().getType()) {
            case HQ:
            case squad:
                this.mapItemsByType.put(mapItem.getBuildingData().getType(), mapItem);
                break;
        }
    }

    public void remove(MoveableMapItem mapItem) {
        this.mapItemsByKey.remove(mapItem.getBuildingKey());
        this.map.buildings.remove(mapItem.getBuilding());
    }

    public List<MoveableMapItem> getMapItems() {
        return new ArrayList<>(mapItemsByKey.values());
    }

    public List<MoveableMapItem> getMapItemsByBuildingUid(String buildingUid) {
        return this.getMapItems().stream().filter(a -> buildingUid.equals(a.getBuildingUid())).collect(Collectors.toList());
    }

    public MoveableMapItem getMapItemByType(BuildingType buildingType) {
        return this.mapItemsByType.get(buildingType);
    }

    public MoveableMapItem getMapItemByKey(String key) {
        return this.mapItemsByKey.get(key);
    }

    public MoveableMapItem createMovableMapItem(String buildingUid, Position position) {
        BuildingData buildingData = ServiceFactory.instance().getGameDataManager().getBuildingDataByUid(buildingUid);
        Building building = new Building();
        building.uid = buildingUid;
        building.key = "bld_" + this.map.next;
        this.map.next++;
        building.x = position.x;
        building.z = position.z;
        building.currentStorage = buildingData.getStorage();
        MoveableMapItem moveableMapItem = new MoveableBuilding(building, buildingData);
        return moveableMapItem;
    }

    public void constructNewBuilding(MoveableMapItem moveableMapItem) {
        add(moveableMapItem.getBuildingKey(), moveableMapItem);
        this.map.buildings.add(moveableMapItem.getBuilding());
    }

    public PlayerMap getBaseMap() {
        return this.map;
    }
}
