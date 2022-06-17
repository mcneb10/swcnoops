package swcnoops.server.commands.guild.response;

import swcnoops.server.model.Member;
import swcnoops.server.model.MembershipRestrictions;
import swcnoops.server.model.WarHistory;
import swcnoops.server.requests.AbstractCommandResult;

import java.util.List;

public class GuildGetCommandResult extends AbstractCommandResult {
    public int activeMemberCount;
    public long created;
    public String currentWarId;
    public String description;
    public Integer highestRankAchieved;
    public Object icon;
    public String id;
    public boolean isSameFactionWarAllowed;
    public long lastPerkNotif;
    public int level;
    public int memberCount;
    public List<Member> members;
    public MembershipRestrictions membershipRestrictions;
    public String name;
    public Object perks;
    public int rank;
    public int score;
    public int squadWarReadyCount;
    public int totalRepInvested;
    public List<WarHistory> warHistory;
    public Object warRating;
    public Object warSignUpTime;
}
