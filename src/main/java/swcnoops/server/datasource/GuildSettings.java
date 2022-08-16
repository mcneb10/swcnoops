package swcnoops.server.datasource;

import swcnoops.server.model.FactionType;
import swcnoops.server.model.Member;
import swcnoops.server.model.Perks;
import swcnoops.server.model.SquadNotification;
import swcnoops.server.session.PlayerSession;

import java.util.Collection;
import java.util.List;

public interface GuildSettings {
    String getGuildId();

    String getGuildName();

    String getDescription();

    FactionType getFaction();

    long getCreated();

    Perks getPerks();

    List<Member> getMembers();

    String getIcon();

    void setDescription(String description);

    void setIcon(String icon);

    void setMinScoreAtEnrollment(Integer minScoreAtEnrollment);

    void setOpenEnrollment(boolean openEnrollment);

    boolean canSave();

    void addMember(String playerId, String playerName, boolean isOwner, boolean isOfficer, long joinDate, long troopsDonated, long troopsReceived, boolean warParty, int hqLevel);

    void removeMember(String playerId);

    SquadNotification createTroopRequest(PlayerSession playerSession, String message);

    String troopDonationRecipient(PlayerSession playerSession, String recipientPlayerId);

    void addSquadNotification(SquadNotification squadNotification);

    Collection<? extends SquadNotification> getSquadNotifications();

    Member getMember(String playerId);

    boolean getOpenEnrollment();

    Integer getMinScoreAtEnrollment();

    Long getWarSignUpTime();

    void setWarSignUpTime(Long warSignUpTime);

    void warMatchmakingStart(long time, List<String> participantIds);

    void addMember(Member member);

    void login(Member member);
}
