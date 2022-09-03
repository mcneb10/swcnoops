package swcnoops.server.session.creature;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.Creature;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.game.*;
import swcnoops.server.model.Building;
import swcnoops.server.model.FactionType;
import swcnoops.server.model.PlayerMap;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.PlayerMapItems;
import swcnoops.server.session.map.MapItem;

import java.util.List;
import java.util.Random;

public class CreatureManagerFactory {
    static private Random random = new Random();
    public static String getRandomCreatureUnitId(FactionType faction) {
        List<TroopData> creatureTroopDatum = ServiceFactory.instance().getGameDataManager().getCreaturesByFaction().get(faction);
        return creatureTroopDatum.get(random.nextInt(creatureTroopDatum.size())).getUnitId();
    }

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
                String unitId = getDefaultCreatureUnitId(playerSession.getFaction());
                playerCreature.setCreatureUnitId(unitId);
            } else {
                playerCreature.setCreatureStatus(CreatureStatus.Invalid);
            }
        }
        return new CreatureManagerImpl(playerSession, creatureDataMap, playerCreature);
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

    static public String getDefaultCreatureUnitId(FactionType faction) {
        String unitId = faction.getNameForLookup() + "RancorCreature";
        return unitId;
    }
}
