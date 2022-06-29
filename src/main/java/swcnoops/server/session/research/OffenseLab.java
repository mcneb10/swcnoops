package swcnoops.server.session.research;

import swcnoops.server.session.buildings.MapItem;
import swcnoops.server.session.commands.BuildingCommands;

public interface OffenseLab extends BuildingCommands, MapItem {
    void upgradeStart(String buildingId, String troopUid, long time);
    boolean processCompletedUpgrades(long time);
}
