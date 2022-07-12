package swcnoops.server.datasource;

import swcnoops.server.ServiceFactory;
import swcnoops.server.game.CampaignData;
import swcnoops.server.game.CampaignMissionData;
import swcnoops.server.game.CampaignMissionSet;
import swcnoops.server.game.CampaignSet;
import swcnoops.server.model.Campaign;
import swcnoops.server.model.Mission;
import swcnoops.server.model.MissionStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PlayerCampaignMission {
    public Map<String, Campaign> campaigns;
    public Map<String, Mission> missions;

    public PlayerCampaignMission() {
    }

    public PlayerCampaignMission(Map<String, Campaign> campaigns, Map<String, Mission> missions) {
        this.campaigns = campaigns;
        this.missions = missions;
    }

    public Mission addMission(CampaignMissionData campaignMissionData) {
        Mission mission = null;
        if (campaignMissionData != null) {
            mission = this.missions.get(campaignMissionData.getUid());

            if (mission == null) {
                mission = new Mission();
                mission.uid = campaignMissionData.getUid();
                mission.campaignUid = campaignMissionData.getCampaignUid();
                mission.collected = false;
                mission.completed = false;
                mission.goals = 3;
                mission.earnedStars = 0;
                mission.lastBattleId = null;
                mission.locked = false;
                this.missions.put(mission.uid, mission);
            }

            CampaignMissionSet campaignMissionSet =
                    ServiceFactory.instance().getGameDataManager().getCampaignMissionSet(mission.campaignUid);
            this.addCampaign(campaignMissionSet);
        }
        return mission;
    }

    private void addCampaign(CampaignMissionSet campaignMissionSet) {
        CampaignData campaignData = campaignMissionSet.getCampaignData();
        Campaign campaign = this.campaigns.get(campaignData.getUid());
        if (campaign == null) {
            campaign = new Campaign();
            campaign.uid = campaignData.getUid();
            campaign.completed = false;
            campaign.collected = false;
            campaign.items = new HashMap<>();
            campaign.points = 0;
            this.campaigns.put(campaign.uid, campaign);
        }
    }

    /**
     * I think these are called after each mission that requires a battle
     */
    public void battleComplete(String battleId, int stars) {
        Optional<Mission> missionOptional = this.missions.values().stream().filter(a -> battleId.equals(a.lastBattleId)).findFirst();
        // if this is a completed battle
        if (missionOptional.isPresent()) {
            Mission mission = missionOptional.get();
            mission.earnedStars = stars;
            if (stars > 0) {
                mission.completed = true;
                mission.status = MissionStatus.Completed;

                // add next mission
                addNextMission(mission);
            }
        }
    }

    private void addNextMission(Mission mission) {
        CampaignMissionData campaignMissionData = ServiceFactory.instance().getGameDataManager().getCampaignMissionData(mission.uid);
        if (campaignMissionData != null) {
            CampaignMissionSet campaignMissionSet = ServiceFactory.instance().getGameDataManager().getCampaignMissionSet(mission.campaignUid);

            int nextMissionIndex = campaignMissionData.getUnlockOrder() + 1;
            if (!campaignMissionSet.hasMission(nextMissionIndex)) {
                nextMissionIndex = 1;
                CampaignSet campaignSet = ServiceFactory.instance().getGameDataManager()
                        .getCampaignForFaction(campaignMissionSet.getCampaignData().getFaction());
                campaignMissionSet = campaignSet.getCampaignMissionSet(campaignMissionSet.getOrder() + 1);
            }

            CampaignMissionData nextcampaignMissionData = campaignMissionSet.getMission(nextMissionIndex);
            this.addMission(nextcampaignMissionData);
        }
    }

    public void pveCollect(String missionUid) {
        Optional<Mission> missionOptional = this.missions.values().stream().filter(a -> missionUid.equals(a.uid)).findFirst();
        // if this is a completed battle
        if (missionOptional.isPresent()) {
            Mission mission = missionOptional.get();
            mission.collected = true;
            mission.status = MissionStatus.Claimed;
        }
    }

    /**
     * These look like they are called when a mission is a task and not a battle
     */
    public void missionsClaimMission(String missionUid) {
        Optional<Mission> missionOptional = this.missions.values().stream().filter(a -> missionUid.equals(a.uid)).findFirst();
        if (missionOptional.isPresent()) {
            Mission mission = missionOptional.get();
            mission.completed = true;
            mission.collected = true;
            mission.status = MissionStatus.Claimed;
            mission.earnedStars = 3;

            // add next mission
            addNextMission(mission);
        }
    }
}
