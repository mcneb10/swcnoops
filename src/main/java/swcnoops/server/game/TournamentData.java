package swcnoops.server.game;

import swcnoops.server.ServiceFactory;

public class TournamentData implements JoeData {
    public String uid;
    public String startDate;
    public String endDate;
    public String bonusEndTime;
    public String backgroundTextureName;
    public String pvpTextureName;
    public String foregroundEmpireTextureName;
    public String foregroundRebelTextureName;
    public int startingRating;
    public String rewardGroupId;
    public String planetId;
    public String titleString;
    public String foregroundLeftTextureName;
    public String foregroundRightTextureName;
    public String rewardBanner;

    private long startTime;
    private long endTime;

    @Override
    public String getUid() {
        return this.uid;
    }

    public void parseJoeDates() {
        this.startTime = ServiceFactory.convertJoeDate(this.startDate);
        this.endTime = ServiceFactory.convertJoeDate(this.endDate);
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean hasExpired(long now) {
        return this.getEndTime() < now;
    }

    public boolean isActive(long now) {
        if (this.getStartTime() != 0 && this.getEndTime() != 0) {
            if (this.getStartTime() <= now && this.getEndTime() >= now) {
                return true;
            }
        }

        return false;
    }
}
