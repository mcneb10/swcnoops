package swcnoops.server.session.inventory;

import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.game.BuildingType;
import swcnoops.server.session.PlayerSessionImpl;
import swcnoops.server.session.PlayerMapItems;
import swcnoops.server.session.map.MapItem;

public class TroopInventoryFactory {
    public TroopInventory createForPlayer(PlayerSessionImpl playerSession) {
        TroopInventory troopInventory = new TroopInventoryImpl(playerSession);
        initialiseFromPlayersMap(troopInventory, playerSession.getPlayerMapItems());
        initialiseFromPlayerSettings(troopInventory, playerSession.getPlayerSettings());
        return troopInventory;
    }

    private void initialiseFromPlayersMap(TroopInventory troopInventory, PlayerMapItems map) {
        if (map != null) {
            for (MapItem mapItem : map.getMapItems()) {
                if (mapItem.getBuildingData().getType() == BuildingType.champion_platform) {
                    troopInventory.addTroopUid(mapItem.getBuildingData().getLinkedUnit());
                }
            }
        }
    }

    private void initialiseFromPlayerSettings(TroopInventory troopInventory, PlayerSettings playerSettings) {
        final Troops troops = playerSettings.getTroops();
        troopInventory.initialise(troops);
    }
}
