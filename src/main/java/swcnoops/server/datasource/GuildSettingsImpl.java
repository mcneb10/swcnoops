package swcnoops.server.datasource;

import swcnoops.server.commands.guild.GuildHelper;
import swcnoops.server.model.*;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;
import java.util.List;

public class GuildSettingsImpl implements GuildSettings {
    private String id;
    private String name;
    private String description;
    private FactionType faction;
    private boolean openEnrollment;
    private Integer minScoreAtEnrollment;
    private String icon;

    private Long warSignUpTime;
    private String warId;

    private List<Member> members;

    public GuildSettingsImpl(String id) {
        this.id = id;
    }

    @Override
    public List<Member> getMembers() {
        return this.members;
    }

    @Override
    public String getGuildId() {
        return id;
    }

    @Override
    public String getGuildName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public FactionType getFaction() {
        return faction;
    }

    // TODO
    @Override
    public long getCreated() {
        return 0;
    }

    @Override
    public Perks getPerks() {
        return GuildHelper.emptyPerks;
    }

    @Override
    public String getIcon() {
        return this.icon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setFaction(FactionType faction) {
        this.faction = faction;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public void setMinScoreAtEnrollment(Integer minScoreAtEnrollment) {
        this.minScoreAtEnrollment = minScoreAtEnrollment;
    }

    @Override
    public void setOpenEnrollment(boolean openEnrollment) {
        this.openEnrollment = openEnrollment;
    }

    @Override
    public boolean canSave() {
        return true;
    }

    @Override
    public SquadNotification createTroopRequest(PlayerSession playerSession, String message) {
        String playerId = playerSession.getPlayerId();
        String playerName = playerSession.getPlayerSettings().getName();

        GuildSession guildSession = playerSession.getGuildSession();
        SquadNotification squadNotification = null;

        if (guildSession != null) {
            squadNotification = new SquadNotification(playerSession.getGuildSession().getGuildId(),
                    playerSession.getGuildSession().getGuildName(),
                    message, playerName, playerId, SquadMsgType.troopRequest);
        }

        return squadNotification;
    }

    @Override
    public String troopDonationRecipient(PlayerSession playerSession, String recipientPlayerId) {
        return recipientPlayerId;
    }

    @Override
    public boolean getOpenEnrollment() {
        return openEnrollment;
    }

    public Integer getMinScoreAtEnrollment() {
        return minScoreAtEnrollment;
    }

    @Override
    public Long getWarSignUpTime() {
        return warSignUpTime;
    }

    @Override
    public void setWarSignUpTime(Long warSignUpTime) {
        this.warSignUpTime = warSignUpTime;
    }

    @Override
    public void setWarMatchmakingSignUpTime(Long time) {
        this.setWarSignUpTime(time);
    }

    @Override
    public void setWarId(String warId) {
        this.warId = warId;
    }

    @Override
    public String getWarId() {
        return warId;
    }

    public void setMembers(List<Member> squadMembers) {
        this.members = squadMembers;
    }
}
