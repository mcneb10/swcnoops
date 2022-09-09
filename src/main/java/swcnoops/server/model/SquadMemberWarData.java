package swcnoops.server.model;

import java.util.List;
import java.util.Map;

public class SquadMemberWarData {
    public String id;
    public String name;
    public int victoryPoints;
    public PlayerMap warMap;
    public DonatedTroops donatedTroops;
    public int turns;
    public int attacksWon;
    public int defensesWon;
    public int score;
    public int level;
    public String warId;
    public long defenseExpirationDate;
    public List<CreatureTrapData> creatureTraps;
    public Map<String, Integer> champions;

    // TODO
    //public List<SquadWarRewardData> rewards;
}
