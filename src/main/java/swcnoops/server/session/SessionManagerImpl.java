package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.*;
import swcnoops.server.model.PlayerModel;
import swcnoops.server.model.PreferencesMap;
import swcnoops.server.model.UnlockedPlanets;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SessionManagerImpl implements SessionManager {
    final private Map<String, PlayerSession> players = new ConcurrentHashMap<>();
    final private Map<String, GuildSession> guilds = new ConcurrentHashMap<>();
    final private Map<String, WarSession> wars = new ConcurrentHashMap<>();
    final private Lock guildLock = new ReentrantLock();

    @Override
    public PlayerSession getPlayerSession(String playerId) {
        return getPlayerSession(playerId, null);
    }

    @Override
    public PlayerSession getPlayerSession(String playerId, PlayerModel defaultPlayerModel) {
        PlayerSession playerSession = getOrLoadPlayerSession(playerId, defaultPlayerModel);
        return playerSession;
    }

    @Override
    public void removePlayerSession(String playerId) {
        this.players.remove(playerId);
    }

    private PlayerSession getOrLoadPlayerSession(String playerId, PlayerModel defaultPlayerModel) {
        PlayerSession playerSession;
        if (!this.players.containsKey(playerId)) {
            playerSession = loadPlayer(playerId, defaultPlayerModel);
            if (playerSession != null) {
                players.put(playerSession.getPlayerId(), playerSession);
            }
        }

        playerSession = this.players.get(playerId);

        return playerSession;
    }

    private PlayerSession loadPlayer(String playerId, PlayerModel defaultPlayerModel) {
        PlayerDataSource playerDataSource = ServiceFactory.instance().getPlayerDatasource();
        Player player = playerDataSource.loadPlayer(playerId);

        if (player == null) {
            // TODO - for now we create unknown players and default them some data
            //player = new Player(ServiceFactory.createRandomUUID());
            throw new RuntimeException("Unknown user id " + playerId);
        }

        PlayerSettings playerSettings = playerDataSource.loadPlayerSettings(playerId);

        if (playerSettings == null)
            throw new RuntimeException("Failed to load playerSettings for id " + playerId);

        if (playerSettings.getBaseMap() == null)
            playerSettings.setBaseMap(defaultPlayerModel.map);
        if (playerSettings.getInventoryStorage() == null)
            playerSettings.setInventoryStorage(defaultPlayerModel.inventory.storage);
        if (playerSettings.getFaction() == null)
            playerSettings.setFaction(defaultPlayerModel.faction);
        if (playerSettings.getCurrentQuest() == null)
            playerSettings.setCurrentQuest(defaultPlayerModel.currentQuest);
        if (playerSettings.getPlayerCampaignMission() == null) {
            PlayerCampaignMission playerCampaignMission =
                    new PlayerCampaignMission(defaultPlayerModel.campaigns, defaultPlayerModel.missions);
            playerSettings.setPlayerCampaignMissions(playerCampaignMission);
        }

        if (playerSettings.getSharedPreferences() == null)
            playerSettings.setSharedPreferences(new PreferencesMap());

        if (playerSettings.getUnlockedPlanets() == null)
            playerSettings.setUnlockedPlanets(new UnlockedPlanets());

        player.setPlayerSettings(playerSettings);
        PlayerSession playerSession = new PlayerSessionImpl(player, playerSettings);
        return playerSession;
    }

    //TODO - make it load and minimise blocking
    @Override
    public GuildSession getGuildSession(PlayerSession playerSession, String guildId) {
        GuildSession guildSession = this.guilds.get(guildId);
        if (guildSession == null) {
            try {
                guildLock.lock();
                guildSession = this.guilds.get(guildId);
                if (guildSession == null) {
                    guildSession = createGuildSession(playerSession, guildId);
                    this.guilds.put(guildSession.getGuildId(), guildSession);
                }
            } finally {
                this.guildLock.unlock();
            }
        }

        return guildSession;
    }

    @Override
    public GuildSession getGuildSession(String guildId) {
        return this.getGuildSession(null, guildId);
    }

    @Override
    public WarSession getWarSession(String warId) {
        WarSession warSession = this.wars.get(warId);

        if (warSession == null) {
            synchronized (this.wars) {
                warSession = this.wars.get(warId);
                if (warSession == null) {
                    warSession = new WarSessionImpl(warId);
                    this.wars.put(warId, warSession);
                }
            }
        }
        return warSession;
    }

    private GuildSession createGuildSession(PlayerSession playerSession, String guildId) {
        GuildSettings guildSettings;
        if (playerSession != null && playerSession.getPlayerId().equals(guildId))
            guildSettings = new SelfDonatingSquad(playerSession);
        else
            guildSettings = loadGuildSettings(guildId);

        return new GuildSessionImpl(guildSettings);
    }

    private GuildSettings loadGuildSettings(String guildId) {
        return ServiceFactory.instance().getPlayerDatasource().loadGuildSettings(guildId);
    }
}
