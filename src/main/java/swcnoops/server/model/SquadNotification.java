package swcnoops.server.model;

import org.mongojack.Id;

public class SquadNotification {
    @Id
    private String id;
    private long date;
    private String message;
    private String name;
    private String playerId;
    private SquadMsgType type;
    private SquadNotificationData data;
    private String guildId;
    private String guildName;

    public SquadNotification() {
    }

    public SquadNotification(String guildId, String guildName, String id, String message, String name, String playerId, SquadMsgType type)
    {
        this(guildId, guildName, 0, id, message, name, playerId, type, null);
    }

    public SquadNotification(String guildId, String guildName, long date, String id, String message,
                             String name, String playerId, SquadMsgType type, SquadNotificationData data)
    {
        this.guildId = guildId;
        this.guildName = guildName;
        this.date = date;
        this.id = id;
        this.message = message;
        this.name = name;
        this.playerId = playerId;
        this.type = type;
        this.data = data;
    }

    public void setDate(long date) {
        this.date = date;
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

    public void setData(SquadNotificationData data) {
        this.data = data;
    }

    public String getGuildId() {
        return guildId;
    }

    public String getGuildName() {
        return this.guildName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
