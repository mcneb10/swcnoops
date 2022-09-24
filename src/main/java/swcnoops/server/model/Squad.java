package swcnoops.server.model;

import org.mongojack.Id;

public class Squad {
    @Id
    public String _id;
    public FactionType faction;
    public String name;
    public String icon;
    public int minScore;
    public int score;
    public int rank;
    public boolean openEnrollment;
    public int members;
    public int activeMemberCount;
    public int level;
    public Long warSignUpTime;
    public String warId;
    public String description;
}
