package swcnoops.server.session.research;

import swcnoops.server.ServiceFactory;
import swcnoops.server.game.*;
import swcnoops.server.model.Building;
import swcnoops.server.model.PlayerMap;
import swcnoops.server.session.PlayerSession;

public class OffenseLabFactory {
    public OffenseLab createForPlayer(PlayerSession playerSession) {
        OffenseLab offenseLab = this.createForMap(playerSession);
        return offenseLab;
    }

    private OffenseLab createForMap(PlayerSession playerSession) {
        OffenseLab offenseLab = null;
        PlayerMap map = playerSession.getBaseMap();
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        for (Building building : map.buildings) {
            BuildingData buildingData = gameDataManager.getBuildingDataByUid(building.uid);
            if (buildingData != null && buildingData.getType() == BuildingType.troop_research) {
                offenseLab = new OffenseLabImpl(playerSession, building, buildingData);
            }
        }
        return offenseLab;
    }
}
