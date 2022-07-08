package swcnoops.server.game;

import swcnoops.server.model.FactionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CampaignSet {
    private FactionType factionType;
    private Map<String, CampaignMissionSet> campaignMap;
    private List<CampaignMissionSet> campaignMissionSets;

    public CampaignSet(FactionType factionType) {
        this.factionType = factionType;
        this.campaignMap = new HashMap<>();
        this.campaignMissionSets = new ArrayList<>();
    }

    public FactionType getFactionType() {
        return factionType;
    }

    private void addCampaignMissionSet(CampaignMissionSet campaignMissionSet) {
        this.campaignMissionSets.add(campaignMissionSet);
        this.campaignMap.put(campaignMissionSet.getUid(), campaignMissionSet);
        this.campaignMissionSets.sort((a,b) -> Integer.compare(a.getOrder(), b.getOrder()));
    }

    public CampaignMissionSet getCampaignMissionSet(String uid) {
        return this.campaignMap.get(uid);
    }

    public CampaignMissionSet getCampaignMissionSet(int order) {
        return this.campaignMissionSets.get(order - 1);
    }

    public void addCampaignData(CampaignData campaignData) {
        CampaignMissionSet campaignMissionSet = this.getCampaignMissionSet(campaignData.getUid());
        if (campaignMissionSet == null) {
            campaignMissionSet = new CampaignMissionSet(campaignData);
            addCampaignMissionSet(campaignMissionSet);
        }
    }
}
