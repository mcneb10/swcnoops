package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.Player;
import swcnoops.server.datasource.PlayerCampaignMission;
import swcnoops.server.datasource.PlayerDataSource;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.model.PlayerModel;
import swcnoops.server.model.PreferencesMap;
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
        return getPlayerSession(playerId, null);
    }

    @Override
    public PlayerSession getPlayerSession(String playerId, PlayerModel defaultPlayerModel) {
        PlayerSession playerSession = getOrLoadPlayerSession(playerId, defaultPlayerModel);
        return playerSession;
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
        if (playerSettings.getSharedPreferences() == null) {
            playerSettings.setSharedPreferences(new PreferencesMap());
        }

        player.setPlayerSettings(playerSettings);
        PlayerSession playerSession = new PlayerSessionImpl(player, playerSettings);
        return playerSession;
    }

    //TODO - make it load and minimise blocking
    //should not be taking the name from being passed in, need to fix
    @Override
    public GuildSession getGuildSession(String playerId, String guildId) {
        GuildSession guildSession = this.guilds.get(guildId);
        if (guildSession == null) {
            try {
                guildLock.lock();
                guildSession = this.guilds.get(guildId);
                if (guildSession == null) {
                    guildSession = createGuildSession(playerId, guildId);
                    this.guilds.put(guildSession.getGuildId(), guildSession);
                }
            } finally {
                this.guildLock.unlock();
            }
        }

        return guildSession;
    }

    private GuildSession createGuildSession(String playerId, String guildId) {
        GuildSession guildSession = null;

        if (playerId.equals(guildId))
            guildSession = new GuildSessionImpl(guildId, "SelfDonating");

        return guildSession;
    }
}
