package swcnoops.server.model;

import java.util.List;
import java.util.Map;

public class Mission {
    public boolean activated;
    public String campaignUid;
    public boolean collected;
    public boolean completed;
    public Map<String, Integer> counters;
    public int earnedStars;
    public int grindMissionRetries;
    public boolean locked;
    public Map<String,Integer> lootRemaining;
    public MissionStatus status;
    public String uid;
    public int goals;
    public String lastBattleId;
    public List<String> satisfiedGoals;
}
