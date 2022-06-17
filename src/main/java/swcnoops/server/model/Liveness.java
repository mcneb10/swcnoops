package swcnoops.server.model;

public class Liveness {
    public long installDate;
    public long commandCount;
    public long sessionCountChecked;
    public long sessionCountToday;
    public long lastLoginTime;
    public long lastTestImpressionBiLogTime;
    public long consecutiveLiveDays;
    public long keepAliveTime;
}
