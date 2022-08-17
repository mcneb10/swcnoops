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

    private Long warSignUpTime;
    private String warId;

    public GuildSettingsImpl(String id) {
        this.id = id;
    }

    public void afterLoad() {
        this.notifications.sort((a, b) -> Long.compare(a.getOrderNo(), b.getOrderNo()));

        if (ServiceFactory.instance().getConfig().createBotPlayersInGroup) {
            if (this.memberMap.size() < 15) {
                for (int i = 0; i < 15; i++) {
                    Member member = createDummyBot(this.getGuildId(), i);
                    this.memberMap.put(member.playerId, member);
                }
            }
        }
    }

    private Member createDummyBot(String guildId, int botName) {
        Member member = new Member();
        member.isOfficer = false;
        member.isOwner = false;
        member.playerId = guildId + "-BOT" + botName;
        member.planet = "planet1";
        member.joinDate = ServiceFactory.getSystemTimeSecondsFromEpoch();
        member.setLevel(10);
        member.name = "BOT-" + botName;
        return member;
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
                          long troopsDonated, long troopsReceived, boolean warParty, int hqLevel) {
        if (!this.memberMap.containsKey(playerId)) {
            Member member = GuildHelper.createMember(playerId, playerName, isOwner,
                    isOfficer, joinDate, troopsDonated, troopsReceived, warParty, hqLevel);
            this.memberMap.put(playerId, member);
        }
    }

    @Override
    public void addMember(Member member) {
        if (member.hasPlayerSession())
            this.login(member);
        else if (!this.memberMap.containsKey(member.playerId)) {
            this.memberMap.put(member.playerId, member);
        }
    }

    @Override
    public void login(Member member) {
        Member oldDetails = this.memberMap.get(member.playerId);
        if (oldDetails != member && oldDetails != null) {
            member.isOfficer = oldDetails.isOfficer;
            member.isOwner = oldDetails.isOwner;
            member.warParty = oldDetails.warParty;
            member.troopsReceived = oldDetails.troopsReceived;
            member.troopsDonated = oldDetails.troopsDonated;
            member.joinDate = oldDetails.joinDate;
            member.attacksWon = oldDetails.attacksWon;
            member.defensesWon = oldDetails.defensesWon;
            member.planet = oldDetails.planet;
            member.hasPlanetaryCommand = oldDetails.hasPlanetaryCommand;
            member.lastLoginTime = oldDetails.lastLoginTime;
            member.rank = oldDetails.rank;
            member.reputationInvested = oldDetails.reputationInvested;
            member.score = oldDetails.score;
            member.xp = oldDetails.xp;
        }

        this.memberMap.put(member.playerId, member);
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
    public void warMatchmakingStart(long time, List<String> participantIds) {
        this.setWarSignUpTime(time);
        for (String id : participantIds) {
            if (this.memberMap.containsKey(id)) {
                this.memberMap.get(id).warParty = 1;
            }
        }
    }

    public void setWarId(String warId) {
        this.warId = warId;
    }

    @Override
    public String getWarId() {
        return warId;
    }
}
