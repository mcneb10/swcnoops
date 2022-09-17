package swcnoops.server.model;

import org.mongojack.Id;

import java.util.List;
import java.util.Map;

public class SquadMemberWarData {
    @Id
    public String _id;
    public String id;               // this is the playerId
    public String guildId;
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
    public String defenseBattleId;
    public String attackBattleId;
    public long attackExpirationDate;

    public List<CreatureTrapData> creatureTraps;
    public Map<String, Integer> champions;

    // TODO
    //public List<SquadWarRewardData> rewards;
}
