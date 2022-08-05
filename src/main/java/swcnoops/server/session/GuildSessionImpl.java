package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.GuildSettings;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        this.guildPlayerSessions.put(playerSession.getPlayerId(), playerSession);
    }

    // TODO - needs proper handler to notify other players, or if the player is already in the squad
    @Override
    public void join(PlayerSession playerSession) {
        login(playerSession);
        playerSession.savePlayerSession();
    }

    @Override
    public void leave(PlayerSession playerSession) {
        playerSession.setGuildSession(null);
        this.guildPlayerSessions.remove(playerSession.getPlayerId());
        playerSession.savePlayerSession();
    }

    @Override
    public void troopsRequest(String playerId, String message, long time) {
        // TODO
    }

    @Override
    public void processDonations(Map<String, Integer> troopsDonated, String requestId, PlayerSession playerSession,
                                 String recipientPlayerId, long time)
    {
        // remove from the donor
        playerSession.removeDeployedTroops(troopsDonated, time);
        // move to recipient
        PlayerSession recipientPlayerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(recipientPlayerId);
        recipientPlayerSession.processDonatedTroops(troopsDonated, playerSession.getPlayerId());
        ServiceFactory.instance().getPlayerDatasource().savePlayerSessions(playerSession, recipientPlayerSession);
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
    public boolean canEdit() {
        return this.guildSettings.canEdit();
    }
}
