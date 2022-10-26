package swcnoops.server.datasource;

import org.mongojack.Id;
import swcnoops.server.model.FactionType;

public class TournamentStat {
    @Id
    public String playerId;
    public String uid;
    public int value;
    public int attacksWon;
    public int defensesWon;
    public Integer rank;
    public float percentile;
    public String name;
    public FactionType faction;
    public String planet;
    public String guildId;
    public Integer hqLevel;
    public String guildName;
    public String icon;
}
