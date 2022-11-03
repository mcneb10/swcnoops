package swcnoops.server.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import swcnoops.server.model.FactionType;

import java.util.List;

public class RaidMissionPoolData implements JoeData {
    @JsonProperty("HQ")
    private int hq;
    private List<String> campaignMissions;
    private String crate1;
    private String crate2;
    private String crate3;
    private String descCondition1;
    private String descCondition2;
    private String descCondition3;
    private FactionType faction;
    private String uid;

    @Override
    public String getUid() {
        return uid;
    }

    public int getHq() {
        return hq;
    }

    public List<String> getCampaignMissions() {
        return campaignMissions;
    }

    public String getCrate1() {
        return crate1;
    }

    public String getCrate2() {
        return crate2;
    }

    public String getCrate3() {
        return crate3;
    }

    public String getDescCondition1() {
        return descCondition1;
    }

    public String getDescCondition2() {
        return descCondition2;
    }

    public String getDescCondition3() {
        return descCondition3;
    }

    public FactionType getFaction() {
        return faction;
    }
}
