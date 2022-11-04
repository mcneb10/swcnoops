package swcnoops.server.game;

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
}
