package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.GuildSettings;
import swcnoops.server.model.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class GuildSessionImpl implements GuildSession {
    final private GuildSettings guildSettings;
    final private Map<String, PlayerSession> guildPlayerSessions = new ConcurrentHashMap<>();

    private Queue<SquadNotification> squadNotifications = new ConcurrentLinkedQueue<>();
    private AtomicLong squadNotificationOrder = new AtomicLong();

    public GuildSessionImpl(GuildSettings guildSettings) {
        this.guildSettings = guildSettings;
        if (this.guildSettings.getSquadNotifications() != null) {
            this.squadNotifications.addAll(guildSettings.getSquadNotifications());
        }

        Optional<SquadNotification> maxNotification =
                this.squadNotifications.stream().max((a, b) -> Long.compare(a.getOrderNo(), b.getOrderNo()));

        if (maxNotification.isPresent())
            this.squadNotificationOrder.set(maxNotification.get().getOrderNo());
    }

    @Override
    public GuildSettings getGuildSettings() {
        return this.guildSettings;
    }

    @Override
    public String getGuildId() {
        return this.guildSettings.getGuildId();
    }

    @Override
    public String getGuildName() {
        return this.guildSettings.getGuildName();
    }

    @Override
    public void login(PlayerSession playerSession) {
        if (!this.guildPlayerSessions.containsKey(playerSession.getPlayerId())) {
            playerSession.setGuildSession(this);
            this.guildPlayerSessions.put(playerSession.getPlayerId(), playerSession);
        }
    }

    @Override
    public void join(PlayerSession playerSession) {
        playerSession.setGuildSession(this);
        this.guildSettings.addMember(playerSession.getPlayerId(), playerSession.getPlayerSettings().getName(),
                false, false, 0, 0, 0);
        this.guildPlayerSessions.put(playerSession.getPlayerId(), playerSession);
        SquadNotification joinNotification = createNotification(this.getGuildId(), this.getGuildName(), playerSession, SquadMsgType.join);
        this.addNotification(joinNotification);
        ServiceFactory.instance().getPlayerDatasource().joinSquad(this, playerSession, joinNotification);
    }

    @Override
    public void leave(PlayerSession playerSession, SquadMsgType leaveType) {
        playerSession.setGuildSession(null);
        this.guildSettings.removeMember(playerSession.getPlayerId());
        this.guildPlayerSessions.remove(playerSession.getPlayerId());
        this.squadNotifications.removeIf(a -> a.getPlayerId().equals(playerSession.getPlayerId()));
        SquadNotification leaveNotification = createNotification(this.getGuildId(), this.getGuildName(), playerSession, leaveType);
        this.addNotification(leaveNotification);

        // the ejected player gets their own one as they are no longer in the squad so will not see the squad message
        if (leaveType == SquadMsgType.ejected) {
            SquadNotification ejectedNotification = new EjectedSquadNotification(this.getGuildId(), this.getGuildName(),
                            ServiceFactory.createRandomUUID(), null,
                            playerSession.getPlayerSettings().getName(),
                            playerSession.getPlayerId(), leaveType);
            ejectedNotification.setDate(leaveNotification.getDate());
            playerSession.addSquadNotification(ejectedNotification);
        }

        ServiceFactory.instance().getPlayerDatasource().leaveSquad(this, playerSession, leaveNotification);
    }

    @Override
    public void changeSquadRole(PlayerSession memberSession, SquadRole squadRole, SquadMsgType squadMsgType) {
        SqmMemberData sqmMemberData = new SqmMemberData();
        sqmMemberData.memberId = memberSession.getPlayerId();
        sqmMemberData.toRank = squadRole;
        SquadNotification roleChangeNotification =
                createNotification(this.getGuildId(), this.getGuildName(), memberSession, squadMsgType);
        roleChangeNotification.setData(sqmMemberData);
        this.addNotification(roleChangeNotification);
        ServiceFactory.instance().getPlayerDatasource().changeSquadRole(this, memberSession,
                roleChangeNotification, squadRole);
    }

    @Override
    public SquadNotification troopsRequest(PlayerSession playerSession, TroopRequestData troopRequestData, String message, long time) {
        SquadNotification squadNotification = this.guildSettings.createTroopRequest(playerSession, message);
        squadNotification.setData(troopRequestData);

        this.addNotification(squadNotification);
        this.saveNotification(squadNotification);
        return squadNotification;
    }

    @Override
    public SquadNotification troopDonation(Map<String, Integer> troopsDonated, String requestId, PlayerSession playerSession,
                                           String recipientPlayerId, long time) {
        SquadNotification squadNotification = new SquadNotification(this.getGuildId(), this.getGuildName(),
                ServiceFactory.createRandomUUID(), null, playerSession.getPlayerSettings().getName(),
                playerSession.getPlayerId(), SquadMsgType.troopDonation);

        // determine recipient for self donation to work
        recipientPlayerId = this.guildSettings.troopDonationRecipient(playerSession, recipientPlayerId);

        // remove units from the donor
        playerSession.removeDeployedTroops(troopsDonated, time);

        // move to recipient
        PlayerSession recipientPlayerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(recipientPlayerId);
        recipientPlayerSession.processDonatedTroops(troopsDonated, playerSession.getPlayerId());

        TroopDonationData troopDonationData = new TroopDonationData();
        troopDonationData.troopsDonated = troopsDonated;
        troopDonationData.amount = troopsDonated.size();
        troopDonationData.requestId = requestId;
        troopDonationData.recipientId = recipientPlayerId;
        squadNotification.setData(troopDonationData);

        this.addNotification(squadNotification);
        ServiceFactory.instance().getPlayerDatasource().savePlayerSessions(this, playerSession,
                recipientPlayerSession, squadNotification);

        return squadNotification;
    }

    @Override
    public void warMatchmakingStart(List<String> participantIds, boolean isSameFactionWarAllowed) {
        // TODO
        // add a guild notification to say match making has started
    }

    @Override
    public void editGuild(String description, String icon, Integer minScoreAtEnrollment, boolean openEnrollment) {
        this.guildSettings.setDescription(description);
        this.guildSettings.setIcon(icon);
        this.guildSettings.setMinScoreAtEnrollment(minScoreAtEnrollment);
        this.guildSettings.setOpenEnrollment(openEnrollment);

        ServiceFactory.instance().getPlayerDatasource().editGuild(this.getGuildId(),
                description, icon, minScoreAtEnrollment, openEnrollment);
    }

    @Override
    public void createNewGuild(PlayerSession playerSession) {
        ServiceFactory.instance().getPlayerDatasource().newGuild(playerSession.getPlayerId(),
                this.getGuildSettings());
    }

    @Override
    public boolean canEdit() {
        return this.guildSettings.canSave();
    }

    @Override
    synchronized public void addNotification(SquadNotification squadNotification) {
        squadNotification.setDate(ServiceFactory.getSystemTimeSecondsFromEpoch());
        squadNotification.setOrderNo(squadNotificationOrder.addAndGet(1));
        this.squadNotifications.add(squadNotification);
    }

    public List<SquadNotification> getNotifications(long since) {
        List<SquadNotification> notifications =
                this.squadNotifications.stream().filter(n -> n.getDate() >= since).collect(Collectors.toList());
        return notifications;
    }

    final static public SquadNotification createNotification(String guildId, String guildName, PlayerSession playerSession,
                                                             SquadMsgType squadMsgType)
    {
        SquadNotification squadNotification = null;
        if (squadMsgType != null) {
            switch (squadMsgType) {
                case leave:
                case join:
                case promotion:
                case demotion:
                case ejected:
                    squadNotification =
                            new SquadNotification(guildId, guildName,
                                    ServiceFactory.createRandomUUID(), null,
                                    playerSession.getPlayerSettings().getName(),
                                    playerSession.getPlayerId(), squadMsgType);
                    break;
                default:
                    throw new RuntimeException("SquadMsgType not support yet " + squadMsgType);
            }
        }

        return squadNotification;
    }

    @Override
    public void saveNotification(SquadNotification squadNotification) {
        ServiceFactory.instance().getPlayerDatasource().saveNotification(this.getGuildId(), squadNotification);
    }

    @Override
    public void saveGuildChange(PlayerSession playerSession, SquadNotification leaveNotification) {
        ServiceFactory.instance().getPlayerDatasource().saveGuildChange(this.getGuildSettings(),
                playerSession, leaveNotification);
    }
}
