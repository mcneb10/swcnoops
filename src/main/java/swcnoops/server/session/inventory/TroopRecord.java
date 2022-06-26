package swcnoops.server.session.inventory;

public class TroopRecord {
    private int level;
    private long effectiveTime;

    public TroopRecord() {
    }

    public TroopRecord(int level, long effectiveTime) {
        this.level = level;
        this.effectiveTime = effectiveTime;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(long effectiveTime) {
        this.effectiveTime = effectiveTime;
    }
}
