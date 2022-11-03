package swcnoops.server.game;

import java.util.List;

public class RaidData implements JoeData {
    private String uid;
    private String buildingHoloAsset;
    private String buildingHoloBundle;
    private int duration;
    private int endHour;
    private int endMinute;
    private String planetUid;
    private List<String> raidMissionsEmpire;
    private List<String> raidMissionsRebel;
    private boolean squadEnabled;
    private int startHour;
    private int startMinute;

    private int startOrder;
    private int endOrder;

    @Override
    public String getUid() {
        return uid;
    }

    public String getBuildingHoloAsset() {
        return buildingHoloAsset;
    }

    public String getBuildingHoloBundle() {
        return buildingHoloBundle;
    }

    public int getDuration() {
        return duration;
    }

    public int getEndHour() {
        return endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public String getPlanetUid() {
        return planetUid;
    }

    public List<String> getRaidMissionsEmpire() {
        return raidMissionsEmpire;
    }

    public List<String> getRaidMissionsRebel() {
        return raidMissionsRebel;
    }

    public boolean isSquadEnabled() {
        return squadEnabled;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void initOrderValue() {
        this.startOrder = (this.startHour * 100) + this.startMinute;
        this.endOrder = (this.endHour * 100) + this.endMinute;
    }

    public int getStartOrder() {
        return startOrder;
    }

    public int getEndOrder() {
        return endOrder;
    }
}
