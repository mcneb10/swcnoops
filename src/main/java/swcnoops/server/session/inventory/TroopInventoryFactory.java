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
        Troops troops = playerSettings.getTroops();
        if (troops == null)
            troops = new Troops();

        troopInventory.setTroops(troops);
        troops.getTroops().forEach((a,b) -> troopInventory.addTroopByUnitIdAndLevel(a,b.getLevel()));
    }
}
