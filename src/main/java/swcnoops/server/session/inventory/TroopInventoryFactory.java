package swcnoops.server.session.inventory;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.BuildingType;
import swcnoops.server.model.Building;
import swcnoops.server.model.PlayerMap;
import swcnoops.server.session.PlayerSessionImpl;

public class TroopInventoryFactory {
    public TroopInventory createForPlayer(PlayerSessionImpl playerSession) {
        TroopInventory troopInventory = new TroopInventoryImpl(playerSession);
        initialiseFromPlayersMap(troopInventory, playerSession.getBaseMap());
        initialiseFromPlayerSettings(troopInventory, playerSession.getPlayerSettings());
        return troopInventory;
    }

    private void initialiseFromPlayersMap(TroopInventory troopInventory, PlayerMap map) {
        for (Building building: map.buildings) {
            BuildingData buildingData = ServiceFactory.instance().getGameDataManager().getBuildingDataByUid(building.uid);
            if (buildingData != null) {
                if (buildingData.getType() == BuildingType.champion_platform) {
                    troopInventory.addTroopUid(buildingData.getLinkedUnit());
                }
            }
        }
    }

    private void initialiseFromPlayerSettings(TroopInventory troopInventory, PlayerSettings playerSettings) {
        final Troops troops = playerSettings.getTroops() != null ? playerSettings.getTroops() : new Troops();
        troopInventory.setTroops(troops);
        // troop records are not persisted, need to initialise that map and the inventory
        troops.getTroops().forEach((a,b) -> troops.getTroopRecords().put(a,b));
        troops.getSpecialAttacks().forEach((a,b) -> troops.getTroopRecords().put(a,b));
        troops.getTroopRecords().forEach((a,b) -> troopInventory.addTroopByUnitIdAndLevel(a,b.getLevel()));
    }
}
