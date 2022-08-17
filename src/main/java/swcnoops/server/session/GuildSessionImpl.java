package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.guild.GuildHelper;
import swcnoops.server.commands.guild.TroopDonationResult;
import swcnoops.server.datasource.GuildSettings;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.datasource.War;
import swcnoops.server.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class GuildSessionImpl implements GuildSession {
    final private GuildSettings guildSettings;

    // TODO - can probably remove this as it does nothing accept a quick lookup
    final private Map<String, PlayerSession> guildPlayerSessions = new ConcurrentHashMap<>();
    final private TroopDonationResult failedTroopDonationResult = new TroopDonationResult(null, new HashMap<>());
    private Queue<SquadNotification> squadNotifications = new ConcurrentLinkedQueue<>();
    private AtomicLong squadNotificationOrder = new AtomicLong();
    private Lock notificationLock = new ReentrantLock();

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
            Member member = GuildHelper.createMember(playerSession);
            this.getGuildSettings().login(member);
            this.guildPlayerSessions.put(playerSession.getPlayerId(), playerSession);
        }
    }

    @Override
    public void join(PlayerSession playerSession) {
        addMember(playerSession);
        SquadNotification joinNotification = createNotification(this.getGuildId(), this.getGuildName(), playerSession, SquadMsgType.join);
        this.addNotification(joinNotification);
        ServiceFactory.instance().getPlayerDatasource().joinSquad(this, playerSession, joinNotification);
    }

    @Override
    public void joinRequest(PlayerSession playerSession, String message) {
        SquadNotification joinRequestNotification =
                createNotification(this.getGuildId(), this.getGuildName(), playerSession, message, SquadMsgType.joinRequest);
        this.addNotification(joinRequestNotification);
        ServiceFactory.instance().getPlayerDatasource().joinRequest(this, playerSession, joinRequestNotification);
    }

    @Override
    public void joinRequestAccepted(String acceptorId, PlayerSession memberSession) {
        SquadNotification joinRequestAcceptedNotification =
                createNotification(this.getGuildId(), this.getGuildName(), memberSession, SquadMsgType.joinRequestAccepted);
        AcceptorSquadMemberApplyData squadMemberApplyData = new AcceptorSquadMemberApplyData();
        squadMemberApplyData.acceptor = acceptorId;
        joinRequestAcceptedNotification.setData(squadMemberApplyData);
        this.squadNotifications.removeIf(a -> a.getPlayerId().equals(memberSession.getPlayerId()) && a.getType() == SquadMsgType.joinRequest);
        this.addNotification(joinRequestAcceptedNotification);
        addMember(memberSession);
        memberSession.addSquadNotification(joinRequestAcceptedNotification);
        ServiceFactory.instance().getPlayerDatasource().joinSquad(this, memberSession, joinRequestAcceptedNotification);
    }

    @Override
    public void warMatched(String warId) {
        SquadNotification warPreparedNotification =
                createNotification(this.getGuildId(), this.getGuildName(), null, SquadMsgType.warPrepared);

        this.addNotification(warPreparedNotification);
        ServiceFactory.instance().getPlayerDatasource().saveNotification(this.getGuildId(), warPreparedNotification);
    }

    @Override
    public void joinRequestRejected(String rejectorId, PlayerSession memberSession) {
        SquadNotification joinRequestRejectedNotification =
                createNotification(this.getGuildId(), this.getGuildName(), memberSession, SquadMsgType.joinRequestRejected);
        RejectorSquadMemberApplyData squadMemberApplyData = new RejectorSquadMemberApplyData();
        squadMemberApplyData.rejector = rejectorId;
        joinRequestRejectedNotification.setData(squadMemberApplyData);
        this.squadNotifications.removeIf(a -> a.getPlayerId().equals(memberSession.getPlayerId()) && a.getType() == SquadMsgType.joinRequest);
        this.addNotification(joinRequestRejectedNotification);
        // TODO - the new member probably needs a special notification
        //memberSession.addSquadNotification(joinRequestRejectedNotification);
        ServiceFactory.instance().getPlayerDatasource().joinRejected(this, memberSession, joinRequestRejectedNotification);
    }

    @Override
    public void leave(PlayerSession playerSession, SquadMsgType leaveType) {
        removeMember(playerSession);
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

    private void removeMember(PlayerSession playerSession) {
        playerSession.setGuildSession(null);
        this.guildSettings.removeMember(playerSession.getPlayerId());
        this.guildPlayerSessions.remove(playerSession.getPlayerId());
        this.squadNotifications.removeIf(a -> a.getPlayerId().equals(playerSession.getPlayerId()));
    }

    private void addMember(PlayerSession playerSession) {
        playerSession.setGuildSession(this);
        if (!guildPlayerSessions.containsKey(playerSession.getPlayerId())) {
            Member member = GuildHelper.createMember(playerSession);
            this.guildSettings.addMember(member);
            this.guildPlayerSessions.put(playerSession.getPlayerId(), playerSession);
        }
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
    public TroopDonationResult troopDonation(Map<String, Integer> troopsDonated, String requestId, PlayerSession playerSession,
                                             String recipientPlayerId, long time) {

        // determine recipient again for self donation to work
        recipientPlayerId = this.guildSettings.troopDonationRecipient(playerSession, recipientPlayerId);
        PlayerSession recipientPlayerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(recipientPlayerId);

        if (recipientPlayerSession == null) {
            return failedTroopDonationResult;
        }

        if (!recipientPlayerSession.processDonatedTroops(troopsDonated, playerSession.getPlayerId()))
            return failedTroopDonationResult;

        playerSession.removeDeployedTroops(troopsDonated, time);

        SquadNotification squadNotification = new SquadNotification(this.getGuildId(), this.getGuildName(),
                ServiceFactory.createRandomUUID(), null, playerSession.getPlayerSettings().getName(),
                playerSession.getPlayerId(), SquadMsgType.troopDonation);

        TroopDonationData troopDonationData = new TroopDonationData();
        troopDonationData.troopsDonated = troopsDonated;
        troopDonationData.amount = troopsDonated.size();
        troopDonationData.requestId = requestId;
        troopDonationData.recipientId = recipientPlayerId;
        squadNotification.setData(troopDonationData);

        this.addNotification(squadNotification);
        ServiceFactory.instance().getPlayerDatasource().savePlayerSessions(this, playerSession,
                recipientPlayerSession, squadNotification);

        return new TroopDonationResult(squadNotification, troopsDonated);
    }

    @Override
    public SquadNotification warMatchmakingStart(PlayerSession playerSession, List<String> participantIds, boolean isSameFactionWarAllowed, long time) {
        SquadNotification squadNotification = GuildSessionImpl.createNotification(this.getGuildId(), this.getGuildName(),
                playerSession, SquadMsgType.warMatchMakingBegin);

        this.addNotification(squadNotification);
        this.getGuildSettings().warMatchmakingStart(time, participantIds);
        ServiceFactory.instance().getPlayerDatasource().saveWarMatchMake(playerSession.getFaction(), this.getGuildId(), participantIds,
                squadNotification, time);
        return squadNotification;
    }

    @Override
    public SquadNotification warMatchmakingCancel(PlayerSession playerSession, long time) {
        SquadNotification squadNotification = GuildSessionImpl.createNotification(this.getGuildId(), this.getGuildName(),
                playerSession, SquadMsgType.warMatchMakingCancel);

        this.addNotification(squadNotification);
        this.getGuildSettings().setWarSignUpTime(null);
        ServiceFactory.instance().getPlayerDatasource().saveWarMatchCancel(this.getGuildId(), squadNotification);
        return squadNotification;
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
    public void addNotification(SquadNotification squadNotification) {
        this.notificationLock.lock();
        try {
            squadNotification.setDate(ServiceFactory.getSystemTimeSecondsFromEpoch());
            squadNotification.setOrderNo(squadNotificationOrder.addAndGet(1));
            this.squadNotifications.add(squadNotification);
        } finally {
            this.notificationLock.unlock();
        }
    }

    public List<SquadNotification> getNotifications(long since) {
        List<SquadNotification> notifications =
                this.squadNotifications.stream().filter(n -> n.getDate() >= since).collect(Collectors.toList());
        return notifications;
    }

    final static public SquadNotification createNotification(String guildId, String guildName, PlayerSession playerSession,
                                                             SquadMsgType squadMsgType)
    {
        return createNotification(guildId, guildName, playerSession, null, squadMsgType);
    }

    final static public SquadNotification createNotification(String guildId, String guildName, PlayerSession playerSession,
                                                             String message, SquadMsgType squadMsgType)
    {
        SquadNotification squadNotification = null;
        PlayerSettings playerSettings = null;
        if (playerSession != null)
            playerSettings = playerSession.getPlayerSettings();

        if (squadMsgType != null) {
            switch (squadMsgType) {
                case leave:
                case join:
                case promotion:
                case demotion:
                case ejected:
                case joinRequest:
                case joinRequestAccepted:
                case joinRequestRejected:
                case warMatchMakingBegin:
                case warMatchMakingCancel:
                case warPrepared:
                    squadNotification =
                            new SquadNotification(guildId, guildName,
                                    ServiceFactory.createRandomUUID(), message,
                                    playerSettings != null ? playerSettings.getName() : null,
                                    playerSettings != null ? playerSettings.getPlayerId() : null, squadMsgType);
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

    @Override
    public War getCurrentWar() {
        War war = null;
        if (this.getGuildSettings().getWarId() != null) {
            war = ServiceFactory.instance().getPlayerDatasource().getWar(this.getGuildSettings().getWarId());
        }

        return war;
    }

    @Override
    public List<SquadMemberWarData> getWarParticipants() {
        return ServiceFactory.instance().getPlayerDatasource()
                .getWarParticipants(this.getGuildId(), this.getGuildSettings().getWarId());
    }
}
