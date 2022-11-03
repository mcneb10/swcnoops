package swcnoops.server.game;

import swcnoops.server.model.FactionType;

public class CampaignData implements JoeData {
    private String endDate;
    private String introStory;
    private String startDate;
    private FactionType faction;
    private String reward;
    private boolean timed;
    private String uid;
    private int unlockOrder;

    public FactionType getFaction() {
        return faction;
    }

    public String getReward() {
        return reward;
    }

    public boolean isTimed() {
        return timed;
    }

    @Override
    public String getUid() {
        return uid;
    }

    public int getUnlockOrder() {
        return unlockOrder;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getIntroStory() {
        return introStory;
    }

    public String getStartDate() {
        return startDate;
    }
}
