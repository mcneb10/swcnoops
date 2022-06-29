package swcnoops.server.model;

public class SquadMessage {
    public SquadMsgType event;
    public String guildId;
    public String guildName;
    public int level;
    public SquadNotification notification;
    public long serverTime;

    public SquadMessage(SquadNotification squadNotification) {
        this.notification = squadNotification;
    }
}
