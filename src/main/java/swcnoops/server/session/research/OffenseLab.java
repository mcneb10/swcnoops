package swcnoops.server.session.research;

import swcnoops.server.session.commands.BuildingCommands;

public interface OffenseLab extends BuildingCommands {
    void upgradeStart(String buildingId, String troopUid, long time);
    void processCompletedUpgrades(long time);
}
