package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.DBCacheObjectImpl;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.model.InventoryStorage;

public class InventoryManagerImpl extends DBCacheObjectImpl<InventoryStorage> implements InventoryManager {
    private final PlayerSession playerSession;
    public InventoryManagerImpl(PlayerSession playerSession, InventoryStorage inventoryStorage) {
        super(inventoryStorage);
        this.playerSession = playerSession;
    }

    @Override
    protected InventoryStorage loadDBObject() {
        PlayerSettings playerSettings = ServiceFactory.instance().getPlayerDatasource()
                .loadPlayerSettings(playerSession.getPlayerId(), false, "playerSettings.inventoryStorage");

        return playerSettings.getInventoryStorage();
    }
}
