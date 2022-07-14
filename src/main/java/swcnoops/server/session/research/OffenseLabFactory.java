package swcnoops.server.session.research;

import swcnoops.server.game.*;
import swcnoops.server.session.PlayerMapItems;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.map.MapItem;

public class OffenseLabFactory {
    public OffenseLab createForPlayer(PlayerSession playerSession) {
        OffenseLab offenseLab = this.createForMap(playerSession);
        return offenseLab;
    }

    private OffenseLab createForMap(PlayerSession playerSession) {
        OffenseLab offenseLab = null;
        PlayerMapItems playerMapItems = playerSession.getPlayerMapItems();
        for (MapItem moveableMapItem : playerMapItems.getMapItems()) {
            BuildingData buildingData = moveableMapItem.getBuildingData();
            if (buildingData != null && buildingData.getType() == BuildingType.troop_research) {
                offenseLab = new OffenseLabImpl(playerSession, moveableMapItem.getBuilding(), buildingData);
            }
        }
        return offenseLab;
    }
}
