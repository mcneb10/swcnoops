package swcnoops.server.game;

import swcnoops.server.ServiceFactory;
import swcnoops.server.model.Building;
import swcnoops.server.model.CreatureTrapData;
import swcnoops.server.model.PlayerMap;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.creature.CreatureManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapHelper {
    static public Map<String, Integer> mapChampions(PlayerMap playerMap) {
        Map<String, Integer> map = new HashMap<>();

        if (playerMap != null) {
            for (Building building : playerMap.buildings) {
                BuildingData buildingData = ServiceFactory.instance().getGameDataManager()
                        .getBuildingDataByUid(building.uid);
                if (buildingData != null) {
                    BuildingType buildingType = buildingData.getType();
                    if (buildingType == BuildingType.champion_platform ) {
                        map.put(buildingData.getLinkedUnit(), Integer.valueOf(1));
                    }
                }
            }
        }

        return map;
    }

    static public List<CreatureTrapData> mapCreatureTraps(PlayerSession playerSession) {
        List<CreatureTrapData> creatureTrapDatums = new ArrayList<>();

        CreatureManager creatureManager = playerSession.getCreatureManager();

        if (creatureManager != null && creatureManager.getCreatureUnitId() != null) {
            TroopData troopData = playerSession.getTroopInventory().getTroopByUnitId(creatureManager.getCreatureUnitId());

            if (troopData != null) {
                CreatureTrapData creatureTrapData = new CreatureTrapData();
                creatureTrapData.buildingId = creatureManager.getBuildingKey();
                creatureTrapData.specialAttackUid = creatureManager.getSpecialAttackUid();
                creatureTrapData.ready = true;
                creatureTrapData.championUid = troopData.getUid();
                creatureTrapDatums.add(creatureTrapData);
            }
        }

        return creatureTrapDatums;
    }

    static public void enableTraps(PlayerMap playerMap) {
        if (playerMap != null) {
            for (Building building : playerMap.buildings) {
                BuildingData buildingData = ServiceFactory.instance().getGameDataManager()
                        .getBuildingDataByUid(building.uid);
                if (buildingData != null) {
                    BuildingType buildingType = buildingData.getType();
                    if (buildingType == BuildingType.trap || buildingType == BuildingType.champion_platform )
                        building.currentStorage = 1;
                }
            }
        }
    }
}
