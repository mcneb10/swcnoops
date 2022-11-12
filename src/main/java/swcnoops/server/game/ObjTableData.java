package swcnoops.server.game;

import swcnoops.server.model.FactionType;

public class ObjTableData implements JoeData {
    private String crateRewardUid;
    private FactionType faction;
    private int hq10;
    private int hq11;
    private int hq4;
    private int hq5;
    private int hq6;
    private int hq7;
    private int hq8;
    private int hq9;
    private String item;
    private String objBucket;
    private String objIcon;
    private String objString;
    private GoalType type;
    private String uid;
    private int weight;
    private int minHQ;

    public String getCrateRewardUid() {
        return crateRewardUid;
    }

    public FactionType getFaction() {
        return faction;
    }

    public int getHq10() {
        return hq10;
    }

    public int getHq11() {
        return hq11;
    }

    public int getHq4() {
        return hq4;
    }

    public int getHq5() {
        return hq5;
    }

    public int getHq6() {
        return hq6;
    }

    public int getHq7() {
        return hq7;
    }

    public int getHq8() {
        return hq8;
    }

    public int getHq9() {
        return hq9;
    }

    public String getItem() {
        return item;
    }

    public String getObjBucket() {
        return objBucket;
    }

    public String getObjIcon() {
        return objIcon;
    }

    public String getObjString() {
        return objString;
    }

    public GoalType getType() {
        return type;
    }

    @Override
    public String getUid() {
        return uid;
    }

    public int getWeight() {
        return weight;
    }

    public int getMinHQ() {
        return minHQ;
    }

    public void setMinHQ(int minHQ) {
        this.minHQ = minHQ;
    }
}
