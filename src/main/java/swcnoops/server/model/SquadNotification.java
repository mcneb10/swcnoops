package swcnoops.server.model;

public class SquadNotification {
    final private long date;
    final private String id;
    final private String message;
    final private String name;
    final private String playerId;
    final private SquadMsgType type;
    final private SquadNotificationData data;

    public SquadNotification(long date, String id, String message, String name, String playerId, SquadMsgType type,
                             SquadNotificationData data)
    {
        this.date = date;
        this.id = id;
        this.message = message;
        this.name = name;
        this.playerId = playerId;
        this.type = type;
        this.data = data;
    }

    public long getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    public String getPlayerId() {
        return playerId;
    }

    public SquadMsgType getType() {
        return type;
    }

    public SquadNotificationData getData() {
        return data;
    }
}
