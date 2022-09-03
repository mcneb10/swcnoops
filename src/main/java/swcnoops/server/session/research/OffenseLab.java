package swcnoops.server.session.research;

import swcnoops.server.session.CurrencyDelta;
import swcnoops.server.session.map.MapItem;
import swcnoops.server.session.commands.BuildingCommands;

public interface OffenseLab extends BuildingCommands, MapItem {
    CurrencyDelta upgradeStart(String buildingId, String troopUid, int credits, int materials, int contraband, long time);
    boolean processCompletedUpgrades(long time);

    boolean isResearchingTroop();
}
