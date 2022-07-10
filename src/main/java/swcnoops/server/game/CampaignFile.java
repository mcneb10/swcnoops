package swcnoops.server.game;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CampaignFile {
    public CampaignContent content;

    static public class CampaignContent {
        public CampaignObjects objects;
    }

    static public class CampaignObjects {
        @JsonProperty("CampaignData")
        public List<CampaignData> campaignData;
        @JsonProperty("CampaignMissionData")
        public List<CampaignMissionData> campaignMissionData;
    }
}
