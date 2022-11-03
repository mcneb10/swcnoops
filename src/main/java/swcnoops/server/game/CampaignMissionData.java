package swcnoops.server.game;

/**
 * Not everything is being mapped
 */
public class CampaignMissionData implements JoeData {
    private String ambientMusic;
    private String battleMusic;
    private String bi_chap_id;
    private String bi_context;
    private int bi_enemy_tier;
    private String bi_mission_id;
    private String bi_mission_name;
    private String campaignUid;
    private int creditRewards;
    private String goalFailString;
    private String goalString;
    private String uid;
    private int unlockOrder;
    private String materialRewards;
    private String progressString;

    public String getAmbientMusic() {
        return ambientMusic;
    }

    public String getBattleMusic() {
        return battleMusic;
    }

    public String getBi_chap_id() {
        return bi_chap_id;
    }

    public String getBi_context() {
        return bi_context;
    }

    public int getBi_enemy_tier() {
        return bi_enemy_tier;
    }

    public String getBi_mission_id() {
        return bi_mission_id;
    }

    public String getBi_mission_name() {
        return bi_mission_name;
    }

    public String getCampaignUid() {
        return campaignUid;
    }

    public int getCreditRewards() {
        return creditRewards;
    }

    public String getGoalFailString() {
        return goalFailString;
    }

    public String getGoalString() {
        return goalString;
    }

    @Override
    public String getUid() {
        return uid;
    }

    public int getUnlockOrder() {
        return unlockOrder;
    }

    public String getMaterialRewards() {
        return materialRewards;
    }

    public String getProgressString() {
        return progressString;
    }
}
