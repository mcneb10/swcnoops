package swcnoops.server.datasource;

import swcnoops.server.model.FactionType;
import swcnoops.server.model.Member;
import swcnoops.server.model.Perks;

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

    boolean canEdit();

    void addMember(String playerId, String playerName);

    void removeMember(String playerId);
}
