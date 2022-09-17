package swcnoops.server.datasource;

import org.mongojack.Id;

public class SquadTotalScore {
    @Id
    public String guildId;
    public int totalScore;
}
