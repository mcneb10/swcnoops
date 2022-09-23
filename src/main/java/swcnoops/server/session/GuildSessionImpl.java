package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.guild.TroopDonationResult;
import swcnoops.server.datasource.GuildSettings;
import swcnoops.server.datasource.War;
import swcnoops.server.model.*;
import swcnoops.server.requests.ResponseHelper;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static swcnoops.server.session.NotificationFactory.createNotification;

public class GuildSessionImpl implements GuildSession {
    final private GuildSettings guildSettings;
    final private TroopDonationResult failedTroopDonationResult = new TroopDonationResult(null, new HashMap<>());
    private Collection<SquadNotification> squadNotifications = new ConcurrentLinkedQueue<>();
    private Set<String> messageIds = new HashSet<>();
    volatile private long latestNotificationDate;
    volatile private long latestDirtyNotificationDate;
    private Lock notificationLock = new ReentrantLock();

    public GuildSessionImpl(GuildSettings guildSettings) {
        this.guildSettings = guildSettings;
        this.latestDirtyNotificationDate = 1;
        this.getNotifications(0);
        this.latestNotificationDate = getMaxNotificationDate(this.squadNotifications);
        this.latestDirtyNotificationDate = this.latestNotificationDate;
    }

    private long getMaxNotificationDate(Collection<SquadNotification> squadNotification) {
        Optional<SquadNotification> maxNotification = squadNotification.stream()
                .max((a,b) -> Long.compare(a.getDate(), b.getDate()));
        if (maxNotification.isPresent())
            return maxNotification.get().getDate();

        return 0;
    }

    public List<SquadNotification> getNotifications(long since) {
        if (this.latestDirtyNotificationDate > this.latestNotificationDate) {
            this.notificationLock.lock();
            try {
                if (this.latestDirtyNotificationDate > this.latestNotificationDate) {
                    Collection<SquadNotification> latestNotifications = ServiceFactory.instance().getPlayerDatasource()
                            .getSquadNotificationsSince(this.getGuildId(), this.getGuildName(), this.latestNotificationDate);
                    if (!latestNotifications.isEmpty()) {
                        this.latestNotificationDate = getMaxNotificationDate(latestNotifications);
                        latestNotifications.forEach(a -> {
                            if (this.messageIds.contains(a.getId())) {
                                System.out.println("message already in");
                            } else {
                                this.messageIds.add(a.getId());
                                this.squadNotifications.add(a);
                            }
                        });
                    }
                }
            } finally {
                this.notificationLock.unlock();
            }
        }

        List<SquadNotification> notifications =
                this.squadNotifications.stream().filter(n -> n.getDate() >= since).collect(Collectors.toList());
        return notifications;
    }

    @Override
    public void setNotificationDirty(long date) {
        this.notificationLock.lock();
        if (this.latestDirtyNotificationDate < date) {
            this.latestDirtyNotificationDate = date;
        }
        this.notificationLock.unlock();
    }

    @Override
    public void processGuildGet(long time) {
        War currentWar = this.getCurrentWar();
        if (currentWar != null) {
            WarSession warSession = ServiceFactory.instance().getSessionManager().getWarSession(currentWar.getWarId());
            if (warSession != null) {
                warSession.processGuildGet(time);
            }
        }
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
        playerSession.setGuildSession(this);
    }

    @Override
    public void join(PlayerSession playerSession) {
        SquadNotification joinNotification =
                createNotification(this.getGuildId(), this.getGuildName(), playerSession, SquadMsgType.join);
        ServiceFactory.instance().getPlayerDatasource().joinSquad(this, playerSession, joinNotification);
        addMember(playerSession);
        this.setNotificationDirty(joinNotification.getDate());
    }

    @Override
    public void joinRequest(PlayerSession playerSession, String message) {
        SquadNotification joinRequestNotification =
                createNotification(this.getGuildId(), this.getGuildName(), playerSession, message, SquadMsgType.joinRequest);
        ServiceFactory.instance().getPlayerDatasource().joinRequest(this, playerSession, joinRequestNotification);
        this.setNotificationDirty(joinRequestNotification.getDate());
    }

    @Override
    public int battleShare(PlayerSession playerSession, String battleId, String message) {
        int returnCode = ResponseHelper.REPLAY_DATA_NOT_FOUND;

        BattleReplay battleReplay = ServiceFactory.instance().getPlayerDatasource().getBattleReplay(battleId);

        if (battleReplay != null && battleReplay.battleLog != null) {
            SquadNotification shareBattleNotification =
                    createNotification(this.getGuildId(), this.getGuildName(), playerSession, message, SquadMsgType.shareBattle);

            ShareBattleNotificationData shareBattleNotificationData =
                    ShareBattleHelper.createBattleNotification(playerSession, battleReplay);

            shareBattleNotification.setData(shareBattleNotificationData);
            ServiceFactory.instance().getPlayerDatasource().battleShare(this, playerSession, shareBattleNotification);
            this.setNotificationDirty(shareBattleNotification.getDate());

            returnCode = ResponseHelper.RECEIPT_STATUS_COMPLETE;
        }

        return returnCode;

    }

    @Override
    public void joinRequestAccepted(String acceptorId, PlayerSession memberSession) {
        SquadNotification joinRequestAcceptedNotification =
                createNotification(this.getGuildId(), this.getGuildName(), memberSession, SquadMsgType.joinRequestAccepted);
        AcceptorSquadMemberApplyData squadMemberApplyData = new AcceptorSquadMemberApplyData();
        squadMemberApplyData.acceptor = acceptorId;
        joinRequestAcceptedNotification.setData(squadMemberApplyData);
        this.squadNotifications.removeIf(a -> a.getPlayerId().equals(memberSession.getPlayerId()) && a.getType() == SquadMsgType.joinRequest);
        addMember(memberSession);
        memberSession.addSquadNotification(joinRequestAcceptedNotification);
        ServiceFactory.instance().getPlayerDatasource().joinSquad(this, memberSession, joinRequestAcceptedNotification);
        this.setNotificationDirty(joinRequestAcceptedNotification.getDate());
    }

    @Override
    public void joinRequestRejected(String rejectorId, PlayerSession memberSession) {
        SquadNotification joinRequestRejectedNotification =
                createNotification(this.getGuildId(), this.getGuildName(), memberSession, SquadMsgType.joinRequestRejected);
        RejectorSquadMemberApplyData squadMemberApplyData = new RejectorSquadMemberApplyData();
        squadMemberApplyData.rejector = rejectorId;
        joinRequestRejectedNotification.setData(squadMemberApplyData);
        this.squadNotifications.removeIf(a -> a.getPlayerId() != null && a.getPlayerId().equals(memberSession.getPlayerId())
                && a.getType() == SquadMsgType.joinRequest);
        // TODO - the new member probably needs a special notification
        //memberSession.addSquadNotification(joinRequestRejectedNotification);
        ServiceFactory.instance().getPlayerDatasource().joinRejected(this, memberSession, joinRequestRejectedNotification);
        this.setNotificationDirty(joinRequestRejectedNotification.getDate());
    }

    @Override
    public void leave(PlayerSession playerSession, SquadMsgType leaveType) {
        SquadNotification leaveNotification = createNotification(this.getGuildId(), this.getGuildName(), playerSession, leaveType);
        ServiceFactory.instance().getPlayerDatasource().leaveSquad(this, playerSession, leaveNotification);
        removeMember(playerSession);
        this.setNotificationDirty(leaveNotification.getDate());

        // the ejected player gets their own one as they are no longer in the squad so will not see the squad message
        if (leaveType == SquadMsgType.ejected) {
            SquadNotification ejectedNotification = new EjectedSquadNotification(this.getGuildId(), this.getGuildName(),
                            null,
                            playerSession.getPlayerSettings().getName(),
                            playerSession.getPlayerId(), leaveType);
            ejectedNotification.setDate(leaveNotification.getDate());
            playerSession.addSquadNotification(ejectedNotification);
        }
    }

    private void removeMember(PlayerSession playerSession) {
        playerSession.setGuildSession(null);
        this.squadNotifications.removeIf(a -> a.getPlayerId() != null
                && a.getPlayerId().equals(playerSession.getPlayerId()));
        this.guildSettings.setDirty();
    }

    private void addMember(PlayerSession playerSession) {
        playerSession.setGuildSession(this);
        this.guildSettings.setDirty();
    }

    @Override
    public void changeSquadRole(PlayerSession invokerSession, PlayerSession memberSession, SquadRole squadRole, SquadMsgType squadMsgType) {
        SqmMemberData sqmMemberData = new SqmMemberData();
        sqmMemberData.memberId = memberSession.getPlayerId();
        sqmMemberData.toRank = squadRole;
        SquadNotification roleChangeNotification =
                createNotification(this.getGuildId(), this.getGuildName(), memberSession, squadMsgType);
        roleChangeNotification.setData(sqmMemberData);
        ServiceFactory.instance().getPlayerDatasource().changeSquadRole(this, invokerSession, memberSession,
                roleChangeNotification, squadRole);
        this.guildSettings.setDirty();
        this.setNotificationDirty(roleChangeNotification.getDate());
    }

    @Override
    public SquadNotification troopsRequest(PlayerSession playerSession, TroopRequestData troopRequestData, String message, long time) {
        SquadNotification squadNotification = this.guildSettings.createTroopRequest(playerSession, message);
        squadNotification.setData(troopRequestData);
        this.saveNotification(squadNotification);
        this.setNotificationDirty(squadNotification.getDate());
        return squadNotification;
    }

    @Override
    public TroopDonationResult troopDonation(Map<String, Integer> troopsDonated, String requestId, PlayerSession playerSession,
                                             String recipientPlayerId, boolean forWar, long time) {

        // determine recipient again for self donation to work
        recipientPlayerId = this.guildSettings.troopDonationRecipient(playerSession, recipientPlayerId);
        PlayerSession recipientPlayerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(recipientPlayerId);

        if (recipientPlayerSession == null) {
            return failedTroopDonationResult;
        }

        DonatedTroops troopsInSC;
        SquadMemberWarData squadMemberWarData = null;
        if (forWar) {
            squadMemberWarData = recipientPlayerSession.getSquadMemberWarData(time);
            if (squadMemberWarData == null)
                return failedTroopDonationResult;

            if (squadMemberWarData.donatedTroops == null)
                squadMemberWarData.donatedTroops = new DonatedTroops();

            troopsInSC = squadMemberWarData.donatedTroops;
        } else {
            troopsInSC = recipientPlayerSession.getDonatedTroops();
        }

        if (!recipientPlayerSession.processDonatedTroops(troopsDonated, playerSession.getPlayerId(), troopsInSC))
            return failedTroopDonationResult;

        SquadNotification squadNotification = new SquadNotification(this.getGuildId(), this.getGuildName(),
                null, playerSession.getPlayerSettings().getName(),
                playerSession.getPlayerId(), SquadMsgType.troopDonation);

        TroopDonationData troopDonationData = new TroopDonationData();
        troopDonationData.troopsDonated = troopsDonated;
        troopDonationData.amount = troopsDonated.size();
        troopDonationData.requestId = requestId;
        troopDonationData.recipientId = recipientPlayerId;
        squadNotification.setData(troopDonationData);

        // TODO - if the save fails then we should invalidate/undo the recipient receiving the troops
        if (forWar) {
            ServiceFactory.instance().getPlayerDatasource().saveWarTroopDonation(this, playerSession,
                    squadMemberWarData, squadNotification);
        } else {
            ServiceFactory.instance().getPlayerDatasource().saveTroopDonation(this, playerSession,
                    recipientPlayerSession, squadNotification);
        }

        playerSession.removeDeployedTroops(troopsDonated, time);
        this.setNotificationDirty(squadNotification.getDate());
        return new TroopDonationResult(squadNotification, troopsDonated);
    }

    @Override
    public SquadNotification warMatchmakingStart(PlayerSession playerSession, List<String> participantIds, boolean isSameFactionWarAllowed, long time) {
        SquadNotification squadNotification = createNotification(this.getGuildId(), this.getGuildName(),
                playerSession, SquadMsgType.warMatchMakingBegin);

        this.getGuildSettings().setWarMatchmakingSignUpTime(time);
        this.getGuildSettings().setWarId(null);
        boolean started = ServiceFactory.instance().getPlayerDatasource().saveWarSignUp(playerSession.getFaction(), this,
                participantIds, isSameFactionWarAllowed, squadNotification, time);

        if (started) {
            this.setNotificationDirty(squadNotification.getDate());
        } else {
            // TODO - if we have not managed to start the signUp, then we want to roll back signUpTime and WarId
            // by reading out what is in the DB, we need make those two properties a DBCache object
            squadNotification = null;
        }

        return squadNotification;
    }

    @Override
    public SquadNotification warMatchmakingCancel(PlayerSession playerSession, long time) {
        SquadNotification squadNotification = createNotification(this.getGuildId(), this.getGuildName(),
                playerSession, SquadMsgType.warMatchMakingCancel);

        this.getGuildSettings().setWarMatchmakingSignUpTime(null);
        this.getGuildSettings().setWarId(null);
        ServiceFactory.instance().getPlayerDatasource().cancelWarSignUp(this, squadNotification);
        this.setNotificationDirty(squadNotification.getDate());
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
        int cost = ServiceFactory.instance().getGameDataManager().getGameConstants().squad_create_cost;
        CurrencyDelta currencyDelta = new CurrencyDelta(cost, cost, CurrencyType.credits, true);
        playerSession.processInventoryStorage(currencyDelta);
        ServiceFactory.instance().getPlayerDatasource().newGuild(playerSession, this.getGuildSettings());
    }

    @Override
    public boolean canEdit() {
        return this.guildSettings.canSave();
    }

    @Override
    public void saveNotification(SquadNotification squadNotification) {
        ServiceFactory.instance().getPlayerDatasource().saveNotification(this, squadNotification);
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
    public List<SquadMemberWarData> getWarParticipants(PlayerSession playerSession) {
        List<SquadMemberWarData> squadMemberWarDatums = ServiceFactory.instance().getPlayerDatasource()
                .getWarParticipants(this.getGuildId(), this.getGuildSettings().getWarId());

        Optional<SquadMemberWarData> playersWarData =
                squadMemberWarDatums.stream().filter(a -> a.id.equals(playerSession.getPlayerId())).findFirst();

        if (playersWarData.isPresent()) {
            playerSession.levelUpBase(playersWarData.get().warMap);
        }

        return squadMemberWarDatums;
    }
}
