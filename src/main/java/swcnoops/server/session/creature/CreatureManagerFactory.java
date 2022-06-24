package swcnoops.server.session.creature;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.CreatureSettings;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.GameDataManager;
import swcnoops.server.game.TrapData;
import swcnoops.server.game.TrapEventType;
import swcnoops.server.model.Building;
import swcnoops.server.model.PlayerMap;

public class CreatureManagerFactory {
    public CreatureManager createForPlayer(PlayerSettings playerSettings) {
        CreatureManagerImpl creatureManager = this.createForMap(playerSettings);
        return creatureManager;
    }

    private CreatureManagerImpl createForMap(PlayerSettings playerSettings) {
        CreatureDataMap creatureDataMap = findCreatureTrap(playerSettings.getBaseMap());
        CreatureSettings playerCreatureSettings = playerSettings.getCreatureSettings();
        if (playerCreatureSettings == null) {
            playerCreatureSettings = new CreatureSettings();

            if (creatureDataMap != null && creatureDataMap.building != null) {
                playerCreatureSettings.setCreatureStatus(CreatureStatus.Alive);
                // TODO - set this to what the player has
                playerCreatureSettings.setCreatureUid("troopEmpireRageRancorCreature10");
                playerCreatureSettings.setSpecialAttack(creatureDataMap.trapData.getEventData());
            } else {
                playerCreatureSettings.setCreatureStatus(CreatureStatus.Invalid);
            }
        }
        return new CreatureManagerImpl(creatureDataMap, playerCreatureSettings);
    }

    static public CreatureDataMap findCreatureTrap(PlayerMap map) {
        CreatureDataMap creatureDataMap = null;
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        for (Building building : map.buildings) {
            BuildingData buildingData = gameDataManager.getBuildingDataByUid(building.uid);
            if (buildingData != null) {
                if (buildingData.getTrapId() != null) {
                    TrapData trapData = gameDataManager.getTrapDataByUid(buildingData.getTrapId());
                    if (trapData.getEventType() == TrapEventType.CreatureSpecialAttack) {
                        creatureDataMap = new CreatureDataMap(building, buildingData, trapData);
                        break;
                    }
                }
            }
        }

        return creatureDataMap;
    }
}
