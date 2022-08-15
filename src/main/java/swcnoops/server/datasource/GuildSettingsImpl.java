package swcnoops.server.datasource;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.guild.GuildHelper;
import swcnoops.server.model.*;
import swcnoops.server.session.GuildSession;
import swcnoops.server.session.PlayerSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuildSettingsImpl implements GuildSettings {
    private String id;
    private String name;
    private String description;
    private FactionType faction;
    private Map<String, Member> memberMap = new ConcurrentHashMap<>();
    private boolean openEnrollment;
    private Integer minScoreAtEnrollment;
    private String icon;
    private String leaderId;

    public GuildSettingsImpl(String id) {
        this.id = id;
    }

    public void afterLoad() {
        this.notifications.sort((a,b) -> Long.compare(a.getOrderNo(), b.getOrderNo()));
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
    public List<Member> getMembers() {
        return new ArrayList<>(this.memberMap.values());
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
    public void addMember(String playerId, String playerName, boolean isOwner, boolean isOfficer, long joinDate,
                          long troopsDonated, long troopsReceived)
    {
        if (!this.memberMap.containsKey(playerId)) {
            Member member = GuildHelper.createMember(playerId, playerName, isOwner,
                    isOfficer, joinDate, troopsDonated, troopsReceived);
            this.memberMap.put(playerId, member);
        }
    }

    @Override
    synchronized public void removeMember(String playerId) {
        if (this.memberMap.containsKey(playerId)) {
            this.memberMap.remove(playerId);
        }
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
                    ServiceFactory.createRandomUUID(),
                    message, playerName, playerId, SquadMsgType.troopRequest);
        }

        return squadNotification;
    }

    @Override
    public String troopDonationRecipient(PlayerSession playerSession, String recipientPlayerId) {
        return recipientPlayerId;
    }

    private List<SquadNotification> notifications = new ArrayList<>();

    @Override
    public void addSquadNotification(SquadNotification squadNotification) {
        this.notifications.add(squadNotification);
    }

    @Override
    public Collection<? extends SquadNotification> getSquadNotifications() {
        return this.notifications;
    }

    @Override
    public Member getMember(String playerId) {
        return this.memberMap.get(playerId);
    }
}
