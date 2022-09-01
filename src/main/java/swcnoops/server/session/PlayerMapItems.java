package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.BuildingType;
import swcnoops.server.model.Building;
import swcnoops.server.model.PlayerMap;
import swcnoops.server.model.Position;
import swcnoops.server.session.creature.CreatureManagerFactory;
import swcnoops.server.session.map.*;

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
    final private Map<String, MapItem> mapItemsByKey = new HashMap<>();
    final private Map<BuildingType, MapItem> mapItemsByType = new HashMap<>();
    /**
     * This is the real map that is from the DB
     */
    final private PlayerMap map;
    public PlayerMapItems(PlayerMap playerMap) {
        this.map = playerMap;
    }

    public void add(String buildingKey, MapItem mapItem) {
        this.mapItemsByKey.put(buildingKey, mapItem);

        switch (mapItem.getBuildingData().getType()) {
            case HQ:
            case squad:
            case droid_hut:
            case scout_tower:
                this.mapItemsByType.put(mapItem.getBuildingData().getType(), mapItem);
                break;
        }
    }

    public void remove(MapItem mapItem) {
        this.mapItemsByKey.remove(mapItem.getBuildingKey());
        this.map.buildings.remove(mapItem.getBuilding());
    }

    public List<MapItem> getMapItems() {
        return new ArrayList<>(mapItemsByKey.values());
    }

    public List<MapItem> getMapItemsByBuildingUid(String buildingUid) {
        return this.getMapItems().stream().filter(a -> buildingUid.equals(a.getBuildingUid())).collect(Collectors.toList());
    }

    public MapItem getMapItemByType(BuildingType buildingType) {
        return this.mapItemsByType.get(buildingType);
    }

    public MapItem getMapItemByKey(String key) {
        return this.mapItemsByKey.get(key);
    }

    public MapItem createMapItem(String buildingUid, String tag, Position position) {
        BuildingData buildingData = ServiceFactory.instance().getGameDataManager().getBuildingDataByUid(buildingUid);
        Building building = new Building();
        building.uid = buildingUid;
        building.key = "bld_" + this.map.next;
        this.map.next++;
        building.x = position.x;
        building.z = position.z;
        building.currentStorage = 0;
        MapItem mapItem = createMapItem(building, buildingData);
        return mapItem;
    }

    public void constructNewBuilding(MapItem mapItem) {
        add(mapItem.getBuildingKey(), mapItem);
        this.map.buildings.add(mapItem.getBuilding());
    }

    public PlayerMap getBaseMap() {
        return this.map;
    }

    static public MapItem createMapItem(Building building, BuildingData buildingData) {
        MapItem mapItem;
        switch (buildingData.getType()) {
            case navigation_center:
                mapItem = new NavigationCenter(building, buildingData);
                break;
            case champion_platform:
                mapItem = new ChampionPlatform(building, buildingData);
                break;
            case resource:
                mapItem = new ResourceBuilding(building, buildingData);
                break;
            case HQ:
                mapItem = new HeadQuarter(building, buildingData);
                break;
            case trap:
                if (CreatureManagerFactory.isCreatureTrap(building.uid)) {
                    mapItem = new StrixBeacon(building, buildingData);
                } else {
                    mapItem = new MapItemImpl(building, buildingData);
                }
                break;
            default:
                mapItem = new MapItemImpl(building, buildingData);
                break;
        }
        return mapItem;
    }
}
