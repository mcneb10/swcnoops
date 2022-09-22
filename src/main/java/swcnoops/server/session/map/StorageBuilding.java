package swcnoops.server.session.map;

import swcnoops.server.game.BuildingData;
import swcnoops.server.game.CurrencyHelper;
import swcnoops.server.model.Building;
import swcnoops.server.model.CurrencyType;
import swcnoops.server.model.InventoryStorage;
import swcnoops.server.session.PlayerSession;

public class StorageBuilding extends MapItemImpl {
    public StorageBuilding(Building building, BuildingData buildingData) {
        super(building, buildingData);
    }

    @Override
    public void buildComplete(PlayerSession playerSession, String unitId, String tag, long endTime) {
        super.buildComplete(playerSession, unitId, tag, endTime);
        updateTotalCapacity(playerSession, this.buildingData.getCurrency());
    }

    private void updateTotalCapacity(PlayerSession playerSession, CurrencyType currency) {
        int totalCapacity = CurrencyHelper.getTotalCapacity(playerSession, currency);
        InventoryStorage inventoryStorage = playerSession.getInventoryManager().getObjectForWriting();
        switch (currency) {
            case credits:
                inventoryStorage.credits.capacity = totalCapacity;
                break;
            case materials:
                inventoryStorage.materials.capacity = totalCapacity;
                break;
            case contraband:
                inventoryStorage.contraband.capacity = totalCapacity;
                break;
        }
    }

    @Override
    public void upgradeComplete(PlayerSession playerSession, String unitId, String tag, long endTime) {
        super.upgradeComplete(playerSession, unitId, tag, endTime);
        updateTotalCapacity(playerSession, this.buildingData.getCurrency());
    }
}