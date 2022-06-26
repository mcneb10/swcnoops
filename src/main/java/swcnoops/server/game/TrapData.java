package swcnoops.server.game;

public class TrapData implements GameData {
    final String uid;
    private Long rearmTime;
    private TrapEventType eventType;
    private String eventData;

    public TrapData(String uid) {
        this.uid = uid;
    }

    @Override
    public String getUid() {
        return uid;
    }

    public Long getRearmTime() {
        return rearmTime;
    }

    public void setRearmTime(Long rearmTime) {
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
}
