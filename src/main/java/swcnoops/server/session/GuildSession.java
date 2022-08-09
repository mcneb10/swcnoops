package swcnoops.server.session;

import swcnoops.server.datasource.GuildSettings;
import swcnoops.server.model.SquadMsgType;
import swcnoops.server.model.SquadNotification;

import java.util.List;
import java.util.Map;

public interface GuildSession {
    String getGuildId();

    void login(PlayerSession playerSession);

    void join(PlayerSession playerSession);

    void troopsRequest(String playerId, String message, long time);

    String getGuildName();

    void processDonations(Map<String, Integer> troopsDonated, String requestId, PlayerSession playerSession, String recipientId, long time);

    void warMatchmakingStart(List<String> participantIds, boolean isSameFactionWarAllowed);

    void leave(PlayerSession playerSession);

    GuildSettings getGuildSettings();

    void editGuild(String description, String icon, Integer minScoreAtEnrollment, boolean openEnrollment);

    boolean canEdit();

    void createNewGuild(PlayerSession playerSession);

    SquadNotification createNotification(PlayerSession playerSession, SquadMsgType squadMsgType);

    void addNotification(SquadNotification squadNotification);

    List<SquadNotification> getNotificationsSince(long since);
}
