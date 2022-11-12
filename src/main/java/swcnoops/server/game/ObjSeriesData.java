package swcnoops.server.game;

import swcnoops.server.ServiceFactory;

public class ObjSeriesData implements JoeData {
    private String endDate;
    private int graceHours;
    private String headerString;
    private String objBucket;
    private String objBucket2;
    private String objBucket3;
    private int objCount;
    private String objectiveExpiringString;
    private String objectiveString;
    private int periodHours;
    private String planetUid;
    private String startDate;
    private String uid;
    private long startTime;
    private long endTime;

    public String getEndDate() {
        return endDate;
    }

    public int getGraceHours() {
        return graceHours;
    }

    public String getHeaderString() {
        return headerString;
    }

    public String getObjBucket() {
        return objBucket;
    }

    public String getObjBucket2() {
        return objBucket2;
    }

    public String getObjBucket3() {
        return objBucket3;
    }

    public int getObjCount() {
        return objCount;
    }

    public String getObjectiveExpiringString() {
        return objectiveExpiringString;
    }

    public String getObjectiveString() {
        return objectiveString;
    }

    public int getPeriodHours() {
        return periodHours;
    }

    public String getPlanetUid() {
        return planetUid;
    }

    public String getStartDate() {
        return startDate;
    }

    @Override
    public String getUid() {
        return uid;
    }

    public void parseJoeDates() {
        this.startTime = ServiceFactory.convertJoeDate(this.startDate);
        this.endTime = ServiceFactory.convertJoeDate(this.endDate);
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
