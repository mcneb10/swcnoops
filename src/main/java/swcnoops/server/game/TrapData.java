package swcnoops.server.game;

public class TrapData implements GameData {
    final String uid;
    private long rearmTime;
    private TrapEventType eventType;
    private String eventData;
    private int rearmMaterialsCost;

    public TrapData(String uid) {
        this.uid = uid;
    }

    @Override
    public String getUid() {
        return uid;
    }

    public long getRearmTime() {
        return rearmTime;
    }

    public void setRearmTime(long rearmTime) {
        this.rearmTime = rearmTime;
    }

    public TrapEventType getEventType() {
        return eventType;
    }

    public void setEventType(TrapEventType eventType) {
        this.eventType = eventType;
    }


    public String getEventData() {
        return eventData;
    }

    public void setEventData(String eventData) {
        this.eventData = eventData;
    }

    public int getRearmMaterialsCost() {
        return rearmMaterialsCost;
    }

    public void setRearmMaterialsCost(int rearmMaterialsCost) {
        this.rearmMaterialsCost = rearmMaterialsCost;
    }
}
