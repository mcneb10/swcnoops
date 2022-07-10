package swcnoops.server.game;

import swcnoops.server.model.FactionType;

public class CampaignData {
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

    public String getUid() {
        return uid;
    }

    public int getUnlockOrder() {
        return unlockOrder;
    }
}
