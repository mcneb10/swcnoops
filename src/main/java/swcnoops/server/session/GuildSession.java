package swcnoops.server.session;

import swcnoops.server.datasource.GuildSettings;
import swcnoops.server.model.SquadNotification;

import java.util.List;
import java.util.Map;

public interface GuildSession {
    String getGuildId();

    void login(PlayerSession playerSession);

    void join(PlayerSession playerSession);

    SquadNotification troopsRequest(PlayerSession playerSession, String message, long time);

    String getGuildName();

    SquadNotification troopDonation(Map<String, Integer> troopsDonated, String requestId, PlayerSession playerSession, String recipientId, long time);

    void warMatchmakingStart(List<String> participantIds, boolean isSameFactionWarAllowed);

    void leave(PlayerSession playerSession);

    GuildSettings getGuildSettings();

    void editGuild(String description, String icon, Integer minScoreAtEnrollment, boolean openEnrollment);

    boolean canEdit();

    void createNewGuild(PlayerSession playerSession);

    void addNotification(SquadNotification squadNotification);

    List<SquadNotification> getNotificationsSince(long since);
}
