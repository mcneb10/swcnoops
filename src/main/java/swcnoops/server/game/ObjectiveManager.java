package swcnoops.server.game;

import swcnoops.server.model.FactionType;
import swcnoops.server.model.ObjectiveGroup;
import swcnoops.server.model.UnlockedPlanets;

import java.util.Collection;
import java.util.Map;

public interface ObjectiveManager {
    void setup(Collection<ObjSeriesData> values, Map<String, ObjTableData> map);

    Map<String, ObjectiveGroup> getObjectiveGroups(Map<String, ObjectiveGroup> existingObjectives,
                                                   UnlockedPlanets unlockedPlanets, Map<String, Long> receivedDonations, FactionType faction,
                                                   int hqLevel, float offset);

    ObjectiveGroup getObjectiveGroup(String planetId, Long receivedDonation, FactionType faction, int hqLevel, float offset);

    Map<String, ObjectiveGroup> getObjectiveGroups(UnlockedPlanets unlockedPlanets, Map<String, Long> receivedDonations,
                                                   FactionType faction, int hqLevel, float offset);
}
