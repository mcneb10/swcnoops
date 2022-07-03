package swcnoops.server.session.creature;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.Creature;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.GameDataManager;
import swcnoops.server.game.TrapData;
import swcnoops.server.game.TrapEventType;
import swcnoops.server.model.Building;
import swcnoops.server.model.PlayerMap;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.PlayerMapItems;
import swcnoops.server.session.map.MoveableMapItem;

public class CreatureManagerFactory {
    public CreatureManager createForPlayer(PlayerSession playerSession) {
        CreatureManagerImpl creatureManager = this.createForMap(playerSession);
        return creatureManager;
    }

    private CreatureManagerImpl createForMap(PlayerSession playerSession) {
        PlayerSettings playerSettings = playerSession.getPlayerSettings();
        CreatureDataMap creatureDataMap = findCreatureTrap(playerSession.getPlayerMapItems());
        Creature playerCreature = playerSettings.getCreature();
        if (playerCreature == null) {
            playerCreature = new Creature();

            if (creatureDataMap != null && creatureDataMap.building != null) {
                playerCreature.setCreatureStatus(CreatureStatus.Alive);
                // TODO - set this to what the player has
                playerCreature.setCreatureUid("troopEmpireRageRancorCreature10");
                playerCreature.setSpecialAttack(creatureDataMap.trapData.getEventData());
            } else {
                playerCreature.setCreatureStatus(CreatureStatus.Invalid);
            }
        }
        return new CreatureManagerImpl(creatureDataMap, playerCreature);
    }

    static public CreatureDataMap findCreatureTrap(PlayerMapItems map) {
        CreatureDataMap creatureDataMap = null;
        if (map != null) {
            GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
            for (MoveableMapItem moveableMapItem : map.getMapItems()) {
                BuildingData buildingData = moveableMapItem.getBuildingData();
                if (buildingData != null) {
                    if (buildingData.getTrapId() != null) {
                        TrapData trapData = gameDataManager.getTrapDataByUid(buildingData.getTrapId());
                        if (trapData.getEventType() == TrapEventType.CreatureSpecialAttack) {
                            creatureDataMap = new CreatureDataMap(moveableMapItem.getBuilding(), buildingData, trapData);
                            break;
                        }
                    }
                }
            }
        }

        if (creatureDataMap == null)
            creatureDataMap = new CreatureDataMap(null, null, null);

        return creatureDataMap;
    }

    static public CreatureDataMap findCreatureTrap(PlayerMap map) {
        CreatureDataMap creatureDataMap = null;
        if (map != null) {
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
        }

        if (creatureDataMap == null)
            creatureDataMap = new CreatureDataMap(null, null, null);

        return creatureDataMap;
    }
}
