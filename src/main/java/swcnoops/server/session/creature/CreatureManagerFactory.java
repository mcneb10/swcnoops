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
import swcnoops.server.session.map.MapItem;

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
            for (MapItem mapItem : map.getMapItems()) {
                BuildingData buildingData = mapItem.getBuildingData();
                if (buildingData != null) {
                    if (buildingData.getTrapId() != null) {
                        TrapData trapData = gameDataManager.getTrapDataByUid(buildingData.getTrapId());
                        if (trapData.getEventType() == TrapEventType.CreatureSpecialAttack) {
                            creatureDataMap = new CreatureDataMap(mapItem.getBuilding(), buildingData, trapData);
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
                if (isCreatureTrap(building.uid)) {
                    BuildingData buildingData = gameDataManager.getBuildingDataByUid(building.uid);
                    TrapData trapData = gameDataManager.getTrapDataByUid(buildingData.getTrapId());
                    creatureDataMap = new CreatureDataMap(building, buildingData, trapData);
                    break;
                }
            }
        }

        if (creatureDataMap == null)
            creatureDataMap = new CreatureDataMap(null, null, null);

        return creatureDataMap;
    }

    static public boolean isCreatureTrap(String uid) {
        boolean isCreatureTrap = false;
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        BuildingData buildingData = ServiceFactory.instance().getGameDataManager().getBuildingDataByUid(uid);
        if (buildingData != null) {
            if (buildingData.getTrapId() != null) {
                TrapData trapData = gameDataManager.getTrapDataByUid(buildingData.getTrapId());
                if (trapData.getEventType() == TrapEventType.CreatureSpecialAttack) {
                    isCreatureTrap = true;
                }
            }
        }

        return isCreatureTrap;
    }
}
