package swcnoops.server.game;

import swcnoops.server.model.FactionType;
import swcnoops.server.model.Raid;
import swcnoops.server.model.UnlockedPlanets;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface RaidManager {
    <T extends JoeData> void setup(Collection<RaidData> values, Map<String, RaidMissionPoolData> map);
    Raid calculateRaidTimes(String planetId, float offset, FactionType faction, int hqLevel, Map<String, Long> objectForReading, long time);

    List<Raid> getRaids(UnlockedPlanets unlockedPlanets, Map<String, Long> objectForReading, float timeZoneOffset, FactionType faction, int hqLevel, long time);
}
