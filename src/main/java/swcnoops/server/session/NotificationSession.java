package swcnoops.server.session;

public class NotificationSession {
    private String notificationGuildId;
    private long notificationSince;
    final private PlayerSession playerSession;

    public NotificationSession(PlayerSession playerSession) {
        this.playerSession = playerSession;
    }

    public void playerLogin() {
        notificationGuildId = null;
        notificationSince = 0;
    }

    /**
     * returns a flag to indicate if the guild has changed
     * @param guildId
     * @param since
     * @return
     */
    public boolean setLastNotification(String guildId, long since) {
        String currentGuildId = this.notificationGuildId;
        this.notificationGuildId = guildId;
        this.notificationSince = since;

        if (currentGuildId == null && guildId != null)
            return true;

        return currentGuildId != null ? !currentGuildId.equals(guildId) : false;
    }

    public boolean canSendNotifications() {
        long currentSince = this.notificationSince;

        if (this.playerSession.getNotifications(currentSince).size() > 0)
            return true;

        if (currentSince == 0)
            return false;

        return true;
    }

    public long getNotificationsSince() {
        return this.notificationSince;
    }
}
