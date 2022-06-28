package swcnoops.server.session;

import swcnoops.server.ServiceFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuildSessionImpl implements GuildSession {
    final private String guildId;
    final private String guildName;
    final private Map<String,PlayerSession> guildPlayerSessions = new ConcurrentHashMap<>();

    public GuildSessionImpl(String guildId, String guildName) {
        this.guildId = guildId;
        this.guildName = guildName;
    }

    @Override
    public String getGuildId() {
        return guildId;
    }

    @Override
    public String getGuildName() {
        return guildName;
    }

    // TODO - needs proper handler to notify other players, or if the player is already in the squad
    @Override
    public void join(PlayerSession playerSession) {
        playerSession.setGuildSession(this);
        this.guildPlayerSessions.put(playerSession.getPlayerId(), playerSession);
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
}
