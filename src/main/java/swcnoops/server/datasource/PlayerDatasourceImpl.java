package swcnoops.server.datasource;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.guild.response.SquadResult;
import swcnoops.server.model.*;
import swcnoops.server.session.PlayerSession;
import swcnoops.server.session.creature.CreatureManager;
import swcnoops.server.session.inventory.Troops;
import swcnoops.server.session.training.BuildUnits;
import swcnoops.server.session.training.DeployableQueue;
import swcnoops.server.session.training.TrainingManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerDatasourceImpl implements PlayerDataSource {
    public PlayerDatasourceImpl() {
    }

    @Override
    public void initOnStartup() {
        checkAndPrepareDB();
    }

    public void checkAndPrepareDB() {
        try {
            try (Connection connection = getConnection()) {
                try (Statement stmt = connection.createStatement()) {
                    String sql = getCreatePlayerDBSql();
                    stmt.executeUpdate(sql);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed player DB check", ex);
        }
    }

    private String getCreatePlayerDBSql() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(ServiceFactory.instance().getConfig().playerCreatePlayerDBSqlResource);

        String content = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        return content;
    }

    private Connection getConnection() throws SQLException {
        String url = ServiceFactory.instance().getConfig().playerSqliteDB;
        Connection conn = DriverManager.getConnection(url);
        return conn;
    }

    @Override
    public Player loadPlayer(String playerId) {
        final String sql = "SELECT id, secret " +
                             "FROM Player p WHERE p.id = ?";

        Player player = null;
        try {
            try (Connection con = getConnection()) {
                try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                    pstmt.setString(1, playerId);
                    ResultSet rs = pstmt.executeQuery();

                    while (rs.next()) {
                        player = new Player(rs.getString("id"));
                        player.setSecret(rs.getString("secret"));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load player from DB id=" + playerId, ex);
        }

        return player;
    }

    @Override
    public void savePlayerName(String playerId, String playerName) {
        final String sql = "update PlayerSettings " +
                              "set name = ? " +
                            "WHERE id = ?";

        try {
            try (Connection con = getConnection()) {
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, playerName);
                    stmt.setString(2, playerId);
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save player name id=" + playerId, ex);
        }
    }

    @Override
    public PlayerSettings loadPlayerSettings(String playerId) {
        final String sql = "SELECT id, name, faction, baseMap, upgrades, deployables, contracts, creature, troops, donatedTroops, " +
                "inventoryStorage, currentQuest, campaigns, preferences, guildId, unlockedPlanets " +
                "FROM PlayerSettings p WHERE p.id = ?";

        PlayerSettings playerSettings = null;
        try {
            try (Connection con = getConnection()) {
                try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                    pstmt.setString(1, playerId);
                    ResultSet rs = pstmt.executeQuery();

                    while (rs.next()) {
                        playerSettings = new PlayerSettings(rs.getString("id"));
                        playerSettings.setName(rs.getString("name"));

                        String faction = rs.getString("faction");
                        if (faction != null && !faction.isEmpty())
                            playerSettings.setFaction(FactionType.valueOf(faction));

                        String baseMap = rs.getString("baseMap");
                        if (baseMap != null) {
                            PlayerMap playerMap = ServiceFactory.instance().getJsonParser()
                                    .fromJsonString(baseMap, PlayerMap.class);
                            playerSettings.setBaseMap(playerMap);
                        }

                        String upgradesJson = rs.getString("upgrades");
                        Upgrades upgrades;
                        if (upgradesJson != null)
                            upgrades = ServiceFactory.instance().getJsonParser()
                                    .fromJsonString(upgradesJson, Upgrades.class);
                        else
                            upgrades = new Upgrades();
                        playerSettings.setUpgrades(upgrades);

                        String deployablesJson = rs.getString("deployables");
                        Deployables deployables;
                        if (deployablesJson != null)
                            deployables = ServiceFactory.instance().getJsonParser()
                                .fromJsonString(deployablesJson, Deployables.class);
                        else
                            deployables = new Deployables();
                        playerSettings.setDeployableTroops(deployables);

                        String contractsJson = rs.getString("contracts");
                        BuildUnits buildUnits;
                        if (contractsJson != null)
                            buildUnits = ServiceFactory.instance().getJsonParser()
                                    .fromJsonString(contractsJson, BuildUnits.class);
                        else
                            buildUnits = new BuildUnits();
                        playerSettings.setBuildContracts(buildUnits);

                        String creatureJson = rs.getString("creature");
                        Creature creature = null;
                        if (creatureJson != null) {
                            creature = ServiceFactory.instance().getJsonParser()
                                    .fromJsonString(creatureJson, Creature.class);
                        }
                        playerSettings.setCreature(creature);

                        String troopsJson = rs.getString("troops");
                        Troops troops;
                        if (troopsJson != null)
                            troops = ServiceFactory.instance().getJsonParser()
                                    .fromJsonString(troopsJson, Troops.class);
                        else
                            troops = new Troops();
                        troops.initialiseMaps();
                        playerSettings.setTroops(troops);

                        String donatedTroopsJson = rs.getString("donatedTroops");
                        DonatedTroops donatedTroops;
                        if (donatedTroopsJson != null)
                            donatedTroops = ServiceFactory.instance().getJsonParser()
                                    .fromJsonString(donatedTroopsJson, DonatedTroops.class);
                        else
                            donatedTroops = new DonatedTroops();
                        playerSettings.setDonatedTroops(donatedTroops);

                        String inventoryStorageJson = rs.getString("inventoryStorage");
                        InventoryStorage inventoryStorage = null;
                        if (inventoryStorageJson != null) {
                            inventoryStorage = ServiceFactory.instance().getJsonParser()
                                    .fromJsonString(inventoryStorageJson, InventoryStorage.class);
                        }
                        playerSettings.setInventoryStorage(inventoryStorage);

                        playerSettings.setCurrentQuest(rs.getString("currentQuest"));

                        String campaignsJson = rs.getString("campaigns");
                        PlayerCampaignMission playerCampaignMission = null;
                        if (campaignsJson != null) {
                            playerCampaignMission = ServiceFactory.instance().getJsonParser()
                                    .fromJsonString(campaignsJson, PlayerCampaignMission.class);
                        }
                        playerSettings.setPlayerCampaignMissions(playerCampaignMission);

                        String preferencesJson = rs.getString("preferences");
                        PreferencesMap preferences = null;
                        if (preferencesJson != null) {
                            preferences = ServiceFactory.instance().getJsonParser()
                                    .fromJsonString(preferencesJson, PreferencesMap.class);
                        }
                        playerSettings.setSharedPreferences(preferences);

                        playerSettings.setGuildId(rs.getString("guildId"));

                        String unlockedPlanetsJson = rs.getString("unlockedPlanets");
                        UnlockedPlanets unlockedPlanets = null;
                        if (unlockedPlanetsJson != null) {
                            unlockedPlanets = ServiceFactory.instance().getJsonParser()
                                    .fromJsonString(unlockedPlanetsJson, UnlockedPlanets.class);
                        }
                        playerSettings.setUnlockedPlanets(unlockedPlanets);
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load player settings from DB id=" + playerId, ex);
        }

        return playerSettings;
    }

    @Override
    public void savePlayerSession(PlayerSession playerSession) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            savePlayerSession(playerSession, connection);
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save player settings id=" + playerSession.getPlayerId(), ex);
        }
    }

    private void savePlayerSession(PlayerSession playerSession, Connection connection) {
        BuildUnits allContracts = mapContractsToPlayerSettings(playerSession);
        String contractsJson = ServiceFactory.instance().getJsonParser().toJson(allContracts);

        Deployables deployables = mapDeployablesToPlayerSettings(playerSession);
        String deployablesJson = ServiceFactory.instance().getJsonParser().toJson(deployables);

        Creature creature = mapCreatureToPlayerSession(playerSession);
        String creatureJson = ServiceFactory.instance().getJsonParser().toJson(creature);

        Troops troops = playerSession.getPlayerSettings().getTroops();
        String troopsJson = ServiceFactory.instance().getJsonParser().toJson(troops);

        DonatedTroops donatedTroops = mapDonatedTroopsToPlayerSession(playerSession);
        String donatedTroopsJson = ServiceFactory.instance().getJsonParser().toJson(donatedTroops);

        PlayerMap playerMap = playerSession.getPlayerMapItems().getBaseMap();
        String playerMapJson = ServiceFactory.instance().getJsonParser().toJson(playerMap);

        InventoryStorage inventoryStorage = playerSession.getPlayerSettings().getInventoryStorage();
        String inventoryStorageJson = ServiceFactory.instance().getJsonParser().toJson(inventoryStorage);

        PlayerCampaignMission playerCampaignMission = playerSession.getPlayerSettings().getPlayerCampaignMission();
        String campaignsJson = ServiceFactory.instance().getJsonParser().toJson(playerCampaignMission);

        PreferencesMap preferences = playerSession.getPlayerSettings().getSharedPreferences();
        String preferencesJson = ServiceFactory.instance().getJsonParser().toJson(preferences);

        UnlockedPlanets unlockedPlanets = playerSession.getPlayerSettings().getUnlockedPlanets();
        String unlockedPlanetsJson = ServiceFactory.instance().getJsonParser().toJson(unlockedPlanets);

        savePlayerSettings(playerSession.getPlayerId(), deployablesJson, contractsJson,
                creatureJson, troopsJson, donatedTroopsJson, playerMapJson, inventoryStorageJson,
                playerSession.getPlayerSettings().getFaction(),
                playerSession.getPlayerSettings().getCurrentQuest(),
                campaignsJson,
                preferencesJson,
                playerSession.getPlayerSettings().getGuildId(),
                unlockedPlanetsJson,
                connection);
    }

    private BuildUnits mapContractsToPlayerSettings(PlayerSession playerSession) {
        BuildUnits allContracts = new BuildUnits();
        allContracts.addAll(playerSession.getTrainingManager().getDeployableTroops().getUnitsInQueue());
        allContracts.addAll(playerSession.getTrainingManager().getDeployableChampion().getUnitsInQueue());
        allContracts.addAll(playerSession.getTrainingManager().getDeployableHero().getUnitsInQueue());
        allContracts.addAll(playerSession.getTrainingManager().getDeployableSpecialAttack().getUnitsInQueue());
        allContracts.addAll(playerSession.getDroidManager().getUnitsInQueue());
        return allContracts;
    }

    private Deployables mapDeployablesToPlayerSettings(PlayerSession playerSession) {
        Deployables deployables = playerSession.getPlayerSettings().getDeployableTroops();
        TrainingManager trainingManager = playerSession.getTrainingManager();
        mapToPlayerSetting(trainingManager.getDeployableTroops(), deployables.troop);
        mapToPlayerSetting(trainingManager.getDeployableChampion(), deployables.champion);
        mapToPlayerSetting(trainingManager.getDeployableHero(), deployables.hero);
        mapToPlayerSetting(trainingManager.getDeployableSpecialAttack(), deployables.specialAttack);
        return deployables;
    }

    private Creature mapCreatureToPlayerSession(PlayerSession playerSession) {
        // replace the players settings with new data before saving
        PlayerSettings playerSettings = playerSession.getPlayerSettings();
        CreatureManager creatureManager = playerSession.getCreatureManager();
        playerSettings.setCreature(creatureManager.getCreature());
        return playerSettings.getCreature();
    }

    private DonatedTroops mapDonatedTroopsToPlayerSession(PlayerSession playerSession) {
        // replace the players troops with new data before saving
        DonatedTroops donatedTroops = playerSession.getDonatedTroops();
        PlayerSettings playerSettings = playerSession.getPlayerSettings();
        playerSettings.setDonatedTroops(donatedTroops);
        return donatedTroops;
    }

    private void mapToPlayerSetting(DeployableQueue deployableQueue, Map<String, Integer> storage) {
        storage.clear();
        storage.putAll(deployableQueue.getDeployableUnits());
    }

    private void savePlayerSettings(String playerId, String deployables, String contracts, String creature,
                                    String troops, String donatedTroops, String playerMapJson, String inventoryStorageJson,
                                    FactionType faction, String currentQuest, String campaignsJson, String preferencesJson,
                                    String guildId, String unlockedPlanets,
                                    Connection connection)
    {
        final String sql = "update PlayerSettings " +
                "set deployables = ?, contracts = ?, creature = ?, troops = ?, donatedTroops = ?, baseMap = ?, " +
                "inventoryStorage = ?, faction = ?, currentQuest = ?, campaigns = ?, preferences = ?, guildId = ?," +
                "unlockedPlanets = ? " +
                "WHERE id = ?";
        try {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, deployables);
                stmt.setString(2, contracts);
                stmt.setString(3, creature);
                stmt.setString(4, troops);
                stmt.setString(5, donatedTroops);
                stmt.setString(6, playerMapJson);
                stmt.setString(7, inventoryStorageJson);
                stmt.setString(8, faction != null ? faction.name() : null);
                stmt.setString(9, currentQuest);
                stmt.setString(10, campaignsJson);
                stmt.setString(11, preferencesJson);
                stmt.setString(12, guildId);
                stmt.setString(13, unlockedPlanets);
                stmt.setString(14, playerId);
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save player settings id=" + playerId, ex);
        }
    }

    @Override
    public void savePlayerSessions(PlayerSession playerSession, PlayerSession recipientPlayerSession) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            savePlayerSession(playerSession, connection);
            savePlayerSession(recipientPlayerSession, connection);
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save player settings id=" + playerSession.getPlayerId() +
                    " and id=" + recipientPlayerSession.getPlayerId(), ex);
        }
    }

    @Override
    public void newPlayer(String playerId, String secret) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            createNewPlayer(playerId, secret, connection);
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create a new player", ex);
        }
    }

    private void createNewPlayer(String playerId, String secret, Connection connection) {
        final String playerSql = "insert into Player (id, secret) values " +
                "(?, ?)";

        final String settingsSql = "insert into PlayerSettings (id, upgrades) values " +
                "(?, '{}')";

        try {
            try (PreparedStatement stmt = connection.prepareStatement(playerSql)) {
                stmt.setString(1, playerId);
                stmt.setString(2, secret);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = connection.prepareStatement(settingsSql)) {
                stmt.setString(1, playerId);
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save player settings id=" + playerId, ex);
        }
    }

    @Override
    public void newGuild(String playerId, SquadResult squadResult) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            createNewGuild(playerId, squadResult, connection);
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create a new player", ex);
        }
    }

    private void createNewGuild(String playerId, SquadResult squadResult, Connection connection) {
        final String squadSql = "insert into Squads (id, faction, name) values " +
                "(?, ?, ?)";

        final String playerSettingsSql = "update PlayerSettings " +
                "set guildId = ? " +
                "WHERE id = ?";

        try {
            try (PreparedStatement stmt = connection.prepareStatement(squadSql)) {
                stmt.setString(1, squadResult.id);
                stmt.setString(2, squadResult.faction.toString());
                stmt.setString(3, squadResult.name);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = connection.prepareStatement(playerSettingsSql)) {
                stmt.setString(1, squadResult.id);
                stmt.setString(2, playerId);
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save player settings id=" + playerId, ex);
        }
    }

    @Override
    public GuildSettings loadGuildSettings(String guildId) {
        final String sql = "SELECT id, name, faction, perks, members, warId, description, icon " +
                "FROM Squads s WHERE s.id = ?";

        final String squadPlayers = "SELECT id, name " +
                "FROM PlayerSettings s WHERE s.guildId = ?";

        GuildSettingsImpl guildSettings = null;
        try {
            try (Connection con = getConnection()) {
                try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                    pstmt.setString(1, guildId);
                    ResultSet rs = pstmt.executeQuery();

                    while (rs.next()) {
                        guildSettings = new GuildSettingsImpl(rs.getString("id"));
                        guildSettings.setName(rs.getString("name"));

                        String faction = rs.getString("faction");
                        if (faction != null && !faction.isEmpty())
                            guildSettings.setFaction(FactionType.valueOf(faction));

                        guildSettings.setDescription(rs.getString("description"));
                        guildSettings.setIcon(rs.getString("icon"));
                    }
                }

                try (PreparedStatement pstmt = con.prepareStatement(squadPlayers)) {
                    pstmt.setString(1, guildId);
                    ResultSet rs = pstmt.executeQuery();

                    while (rs.next()) {
                        String playerId = rs.getString("id");
                        String playerName = rs.getString("name");

                        guildSettings.addMember(playerId, playerName);
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load Guild settings from DB id=" + guildId, ex);
        }

        return guildSettings;
    }

    @Override
    public void editGuild(String guildId, String description, String icon, Integer minScoreAtEnrollment,
                          boolean openEnrollment)
    {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            editGuild(guildId, description, icon, minScoreAtEnrollment, openEnrollment, connection);
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create a new player", ex);
        }
    }

    private void editGuild(String guildId, String description, String icon, Integer minScoreAtEnrollment,
                           boolean openEnrollment, Connection connection)
    {
        final String squadSql = "update Squads " +
                "set description = ?, " +
                "icon = ? " +
                "where id = ?";

        try {
            try (PreparedStatement stmt = connection.prepareStatement(squadSql)) {
                stmt.setString(1, description);
                stmt.setString(2, icon);
                stmt.setString(3, guildId);
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to edit squad id=" + guildId, ex);
        }
    }

    @Override
    public List<Squad> getGuildList(FactionType faction) {
        try (Connection connection = getConnection()) {
            return getGuildList(faction, connection);
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create a new player", ex);
        }
    }

    private List<Squad> getGuildList(FactionType faction, Connection connection) {
        final String sql = "SELECT id, name, faction, description, icon " +
                "FROM Squads s WHERE s.faction = ?";

        List<Squad> squads = new ArrayList<>();
        try {
            try (Connection con = getConnection()) {
                try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                    pstmt.setString(1, faction.name());
                    ResultSet rs = pstmt.executeQuery();

                    while (rs.next()) {
                        Squad squad = new Squad();
                        squad._id = rs.getString("id");
                        squad.name = rs.getString("name");
                        squad.faction = FactionType.valueOf(rs.getString("faction"));
                        squad.icon = rs.getString("icon");
                        squad.openEnrollment = true;
                        squad.minScore = 0;
                        squad.rank = 0;
                        squad.level = 0;
                        squad.activeMemberCount = 1;
                        squad.members = 10;
                        squad.score = 1;
                        squads.add(squad);
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load Guild list from DB", ex);
        }

        return squads;
    }
}
