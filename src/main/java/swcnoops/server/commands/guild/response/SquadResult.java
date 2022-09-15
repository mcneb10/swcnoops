package swcnoops.server.commands.guild.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import swcnoops.server.model.*;
import swcnoops.server.requests.ResponseHelper;

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

    @JsonIgnore
    private int status;

    public SquadResult() {
        this.status = ResponseHelper.RECEIPT_STATUS_COMPLETE;
    }

    public SquadResult(int errorCode) {
        this.status = errorCode;
    }

    @Override
    public Integer getStatus() {
        return this.status;
    }
}
