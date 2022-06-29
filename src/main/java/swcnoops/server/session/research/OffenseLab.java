package swcnoops.server.session.research;

import swcnoops.server.session.map.MapItem;
import swcnoops.server.session.commands.BuildingCommands;

public interface OffenseLab extends BuildingCommands, MapItem {
    void upgradeStart(String buildingId, String troopUid, long time);
    boolean processCompletedUpgrades(long time);
}
