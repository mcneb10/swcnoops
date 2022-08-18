package swcnoops.server.commands.guild.response;

import swcnoops.server.model.*;

import java.util.List;

public class SquadResult extends GuildResult {
    public FactionType faction;
    public int activeMemberCount;
    public long created;
    public String currentWarId;
    public String description;
    public Integer highestRankAchieved;
    public String icon;
    public String id;
    public boolean isSameFactionWarAllowed;
    public long lastPerkNotif;
    public int level;
    public int memberCount;
    public List<Member> members;
    public MembershipRestrictions membershipRestrictions;
    public String name;
    public Perks perks;
    public int rank;
    public int score;
    public int squadWarReadyCount;
    public int totalRepInvested;
    public List<WarHistory> warHistory;
    public Object warRating;
    public Long warSignUpTime;
    public int minScoreAtEnrollment;
    public boolean openEnrollment;
}
