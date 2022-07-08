package swcnoops.server.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CampaignMissionSet {
    private CampaignData campaignData;
    private Map<String, CampaignMissionData> missionDataMap;
    private List<CampaignMissionData> missions;

    public CampaignMissionSet(CampaignData campaignData) {
        this.campaignData = campaignData;
        this.missionDataMap = new HashMap<>();
        this.missions = new ArrayList<>();
    }

    public String getUid() {
        return campaignData.getUid();
    }
    public int getOrder() {
        return campaignData.getUnlockOrder();
    }

    public void addMission(CampaignMissionData campaignMissionData) {
        this.missions.add(campaignMissionData);
        this.missionDataMap.put(campaignMissionData.getUid(), campaignMissionData);
        this.missions.sort((a,b) -> Integer.compare(a.getUnlockOrder(), b.getUnlockOrder()));
    }

    public CampaignMissionData getMission(String uid) {
        return this.missionDataMap.get(uid);
    }

    public CampaignMissionData getMission(int unlocKOrder) {
        return this.missions.get(unlocKOrder - 1);
    }
}
