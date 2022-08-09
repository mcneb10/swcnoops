package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.GuildSettings;
import swcnoops.server.model.SquadMsgType;
import swcnoops.server.model.SquadNotification;
import swcnoops.server.model.TroopDonationData;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class GuildSessionImpl implements GuildSession {
    final private GuildSettings guildSettings;
    final private Map<String,PlayerSession> guildPlayerSessions = new ConcurrentHashMap<>();

    public GuildSessionImpl(GuildSettings guildSettings) {
        this.guildSettings = guildSettings;
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
        this.guildSettings.addMember(playerSession.getPlayerId(), playerSession.getPlayerSettings().getName());
        this.guildPlayerSessions.put(playerSession.getPlayerId(), playerSession);
    }

    // TODO - needs proper handler to notify other players, or if the player is already in the squad
    @Override
    public void join(PlayerSession playerSession) {
        login(playerSession);
        SquadNotification joinNotification = createNotification(playerSession, SquadMsgType.join);
        this.addNotification(joinNotification);
        playerSession.savePlayerSession();
    }

    @Override
    public void leave(PlayerSession playerSession) {
        playerSession.setGuildSession(null);
        this.guildSettings.removeMember(playerSession.getPlayerId());
        this.guildPlayerSessions.remove(playerSession.getPlayerId());

        SquadNotification joinNotification = createNotification(playerSession, SquadMsgType.leave);
        this.addNotification(joinNotification);
        playerSession.savePlayerSession();
    }

    @Override
    public SquadNotification troopsRequest(PlayerSession playerSession, String message, long time) {
        SquadNotification squadNotification = this.guildSettings.createTroopRequest(playerSession, message);
        return squadNotification;
    }

    @Override
    public SquadNotification troopDonation(Map<String, Integer> troopsDonated, String requestId, PlayerSession playerSession,
                                           String recipientPlayerId, long time)
    {
        SquadNotification squadNotification = new SquadNotification(ServiceFactory.createRandomUUID(),
                null, playerSession.getPlayerSettings().getName(), playerSession.getPlayerId(), SquadMsgType.troopDonation);

        // determine recipient for self donation to work
        recipientPlayerId = this.guildSettings.troopDonationRecipient(playerSession, recipientPlayerId);

        // remove from the donor
        playerSession.removeDeployedTroops(troopsDonated, time);

        // move to recipient
        PlayerSession recipientPlayerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(recipientPlayerId);
        recipientPlayerSession.processDonatedTroops(troopsDonated, playerSession.getPlayerId());
        ServiceFactory.instance().getPlayerDatasource().savePlayerSessions(playerSession, recipientPlayerSession);

        TroopDonationData troopDonationData = new TroopDonationData();
        troopDonationData.troopsDonated = troopsDonated;
        troopDonationData.amount = troopsDonated.size();
        troopDonationData.requestId = requestId;
        troopDonationData.recipientId = recipientPlayerId;

        squadNotification.setData(troopDonationData);
        this.addNotification(squadNotification);

        // TODO - save notification
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

    private Queue<SquadNotification> squadNotifications = new ConcurrentLinkedQueue<>();

    @Override
    synchronized public void addNotification(SquadNotification squadNotification) {
        squadNotification.setDate(ServiceFactory.getSystemTimeSecondsFromEpoch());
        this.squadNotifications.add(squadNotification);
    }

    public List<SquadNotification> getNotificationsSince(long since) {
        List<SquadNotification> notifications =
                this.squadNotifications.stream().filter(n -> n.getDate() >= since).collect(Collectors.toList());
        return notifications;
    }

    final static public SquadNotification createNotification(PlayerSession playerSession, SquadMsgType squadMsgType) {
        SquadNotification squadNotification = null;
        if (squadMsgType != null) {
            switch (squadMsgType) {
                case leave:
                case join:
                    squadNotification =
                            new SquadNotification(ServiceFactory.createRandomUUID(), null,
                                    playerSession.getPlayerSettings().getName(),
                                    playerSession.getPlayerId(), squadMsgType);
                    break;
                default:
                    throw new RuntimeException("SquadMsgType not support yet " + squadMsgType);
            }
        }

        return squadNotification;
    }
}
