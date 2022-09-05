package swcnoops.server.datasource;

import swcnoops.server.model.*;
import swcnoops.server.session.PlayerSession;

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

    SquadNotification createTroopRequest(PlayerSession playerSession, String message);

    String troopDonationRecipient(PlayerSession playerSession, String recipientPlayerId);

    boolean getOpenEnrollment();

    Integer getMinScoreAtEnrollment();

    Long getWarSignUpTime();

    void setWarSignUpTime(Long warSignUpTime);

    void warMatchmakingStart(long time, List<String> participantIds);

    String getWarId();

    void setWarId(String warId);

    void setDirty();

    List<WarHistory> getWarHistory();
}
