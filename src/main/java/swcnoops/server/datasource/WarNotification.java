package swcnoops.server.datasource;

public class WarNotification {
    private long guildANotificationDate;
    private long guildBNotificationDate;

    public long getGuildANotificationDate() {
        return guildANotificationDate;
    }

    public void setGuildANotificationDate(long guildANotificationDate) {
        this.guildANotificationDate = guildANotificationDate;
    }

    public long getGuildBNotificationDate() {
        return guildBNotificationDate;
    }

    public void setGuildBNotificationDate(long guildBNotificationDate) {
        this.guildBNotificationDate = guildBNotificationDate;
    }
}
