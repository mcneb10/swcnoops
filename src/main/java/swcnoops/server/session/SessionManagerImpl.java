package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.Player;
import swcnoops.server.datasource.PlayerDataSource;
import swcnoops.server.datasource.PlayerSettings;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SessionManagerImpl implements SessionManager {
    final private Map<String, PlayerSession> players = new ConcurrentHashMap<>();
    final private Map<String, GuildSession> guilds = new ConcurrentHashMap<>();
    final private Lock guildLock = new ReentrantLock();

    @Override
    public PlayerSession getPlayerSession(String playerId) {
        PlayerSession playerSession = getOrLoadPlayerSession(playerId);
        return playerSession;
    }

    private PlayerSession getOrLoadPlayerSession(String playerId) {
        PlayerSession playerSession;
        if (!this.players.containsKey(playerId)) {
            playerSession = loadPlayer(playerId);
            if (playerSession != null) {
                players.put(playerSession.getPlayerId(), playerSession);
            }
        }

        playerSession = this.players.get(playerId);

        return playerSession;
    }

    private PlayerSession loadPlayer(String playerId) {
        PlayerDataSource playerDataSource = ServiceFactory.instance().getPlayerDatasource();
        Player player = playerDataSource.loadPlayer(playerId);

        if (player == null) {
            // TODO - for now we create unknown players and default them some data
            //player = new Player(ServiceFactory.createRandomUUID());
            throw new RuntimeException("Unknown user id " + playerId);
        }

        PlayerSettings playerSettings = playerDataSource.loadPlayerSettings(playerId);
        player.setPlayerSettings(playerSettings);
        PlayerSession playerSession = new PlayerSessionImpl(player, playerSettings);
        return playerSession;
    }

    //TODO - make it load and minimise blocking
    @Override
    public GuildSession getGuildSession(String guildId) {
        GuildSession guildSession = this.guilds.get(guildId);
        if (guildSession == null) {
            try {
                guildLock.lock();
                guildSession = this.guilds.get(guildId);
                if (guildSession == null) {
                    guildSession = createGuildSession(guildId);
                    this.guilds.put(guildSession.getGuildId(), guildSession);
                }
            } finally {
                this.guildLock.unlock();
            }
        }

        return guildSession;
    }

    private GuildSession createGuildSession(String guildId) {
        GuildSession guildSession = new GuildSessionImpl(guildId);
        return guildSession;
    }
}
