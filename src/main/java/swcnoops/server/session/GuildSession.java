package swcnoops.server.session;

import swcnoops.server.commands.guild.TroopDonationResult;
import swcnoops.server.commands.player.PlayerBattleComplete;
import swcnoops.server.datasource.GuildSettings;
import swcnoops.server.datasource.War;
import swcnoops.server.model.*;

import java.util.List;
import java.util.Map;

public interface GuildSession {
    String getGuildId();

    void login(PlayerSession playerSession);

    void join(PlayerSession playerSession);

    SquadNotification troopsRequest(PlayerSession playerSession, TroopRequestData troopRequestData, String message, long time);

    String getGuildName();

    TroopDonationResult troopDonation(Map<String, Integer> troopsDonated, String requestId, PlayerSession playerSession, String recipientId, boolean forWar, long time);

    SquadNotification warMatchmakingStart(PlayerSession playerSession, List<String> participantIds, boolean isSameFactionWarAllowed, long time);

    void leave(PlayerSession playerSession, SquadMsgType leaveType);

    GuildSettings getGuildSettings();

    void editGuild(String description, String icon, Integer minScoreAtEnrollment, boolean openEnrollment);

    boolean canEdit();

    void createNewGuild(PlayerSession playerSession);

    List<SquadNotification> getNotifications(long since);

    void saveNotification(SquadNotification squadNotification);

    void saveGuildChange(PlayerSession playerSession, SquadNotification leaveNotification);

    void changeSquadRole(PlayerSession memberSession, SquadRole squadRole, SquadMsgType squadMsgType);

    void joinRequest(PlayerSession playerSession, String message);

    void joinRequestAccepted(String acceptorId, PlayerSession memberSession);

    void joinRequestRejected(String playerId, PlayerSession memberSession);

    SquadNotification warMatchmakingCancel(PlayerSession playerSession, long time);

    void warMatched(String warId);

    War getCurrentWar();

    List<SquadMemberWarData> getWarParticipants(PlayerSession playerSession);

    void warAttackComplete(PlayerBattleComplete playerBattleComplete, PlayerSession playerSession);

    String warAttackStart(PlayerSession playerSession, String opponentId, long time);

    void warStarted(long time);

    void setNotificationDirty(long date);
}
