package swcnoops.server.datasource;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.player.PlayerIdentitySwitch;
import swcnoops.server.model.*;
import swcnoops.server.session.GuildSession;
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
        final String primarySql = "SELECT id, secret " +
                "FROM Player p WHERE p.id = ?";

        final String secondarySql = "SELECT secondaryAccount, secret " +
                "FROM Player p WHERE p.secondaryAccount = ?";

        String sql = primarySql;
        if (playerId.endsWith("_1")) {
            sql = secondarySql;
        }

        Player player = null;
        try {
            try (Connection con = getConnection()) {
                try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                    pstmt.setString(1, playerId);
                    ResultSet rs = pstmt.executeQuery();

                    while (rs.next()) {
                        player = new Player(playerId);
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
    public void savePlayerSession(PlayerSession playerSession, SquadNotification squadNotification) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            savePlayerSession(playerSession, connection);
            if (squadNotification != null)
                saveNotification(squadNotification.getGuildId(), squadNotification, connection);
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save player settings id=" + playerSession.getPlayerId(), ex);
        }
    }

    @Override
    public void joinSquad(GuildSession guildSession, PlayerSession playerSession, SquadNotification squadNotification) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            savePlayerSession(playerSession, connection);

            if (guildSession.canEdit()) {
                insertSquadMember(guildSession.getGuildId(), playerSession.getPlayerId(), false, false, 0, connection);
                deleteJoinRequestNotifications(squadNotification.getGuildId(), squadNotification.getPlayerId(), connection);
                saveNotification(squadNotification.getGuildId(), squadNotification, connection);
            }
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save player settings id=" + playerSession.getPlayerId(), ex);
        }
    }

    @Override
    public void joinRequest(GuildSession guildSession, PlayerSession playerSession, SquadNotification squadNotification) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            savePlayerSession(playerSession, connection);

            if (guildSession.canEdit()) {
                deleteJoinRequestNotifications(squadNotification.getGuildId(), squadNotification.getPlayerId(), connection);
                saveNotification(squadNotification.getGuildId(), squadNotification, connection);
            }
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save player settings id=" + playerSession.getPlayerId(), ex);
        }
    }

    @Override
    public void joinRejected(GuildSession guildSession, PlayerSession playerSession, SquadNotification squadNotification) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);

            if (guildSession.canEdit()) {
                deleteJoinRequestNotifications(squadNotification.getGuildId(), squadNotification.getPlayerId(), connection);
                saveNotification(squadNotification.getGuildId(), squadNotification, connection);
            }
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save player settings id=" + playerSession.getPlayerId(), ex);
        }
    }

    private void deleteJoinRequestNotifications(String guildId, String playerId, Connection connection) {
        final String squadMemberSql = "delete from SquadNotifications where guildId = ? and playerId = ? and squadMessageType = ?";

        try {
            try (PreparedStatement stmt = connection.prepareStatement(squadMemberSql)) {
                stmt.setString(1, guildId);
                stmt.setString(2, playerId);
                stmt.setString(3, SquadMsgType.joinRequest.toString());
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete SquadNotifications for playerId=" +
                    playerId + " in guidId = " + guildId, ex);
        }
    }

    @Override
    public void leaveSquad(GuildSession guildSession, PlayerSession playerSession, SquadNotification squadNotification) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            savePlayerSession(playerSession, connection);
            if (guildSession.canEdit()) {
                deleteSquadMember(playerSession.getPlayerId(), connection);
                deleteNotifications(squadNotification.getGuildId(), playerSession.getPlayerId(), connection);
                saveNotification(squadNotification.getGuildId(), squadNotification, connection);
            }
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save player settings id=" + playerSession.getPlayerId(), ex);
        }
    }

    private void deleteNotifications(String guildId, String playerId, Connection connection) {
        final String squadMemberSql = "delete from SquadNotifications where guildId = ? and playerId = ?";

        try {
            try (PreparedStatement stmt = connection.prepareStatement(squadMemberSql)) {
                stmt.setString(1, guildId);
                stmt.setString(2, playerId);
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete SquadNotifications for playerId=" +
                    playerId + " in guidId = " + guildId, ex);
        }
    }

    @Override
    public void changeSquadRole(GuildSession guildSession, PlayerSession playerSession, SquadNotification squadNotification,
                                SquadRole squadRole)
    {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            if (guildSession.canEdit()) {
                Member member = guildSession.getGuildSettings().getMember(playerSession.getPlayerId());
                member.setIsOfficer(squadRole == SquadRole.Officer);
                updateSquadMember(guildSession.getGuildId(), member, connection);
                saveNotification(squadNotification.getGuildId(), squadNotification, connection);
            }
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save player settings id=" + playerSession.getPlayerId(), ex);
        }
    }

    private void updateSquadMember(String guildId, Member member, Connection connection) {
        final String squadSql = "update SquadMembers " +
                "set isOfficer = ?, isOwner = ? " +
                "where guildId = ? and playerId = ?";

        try {
            try (PreparedStatement stmt = connection.prepareStatement(squadSql)) {
                stmt.setBoolean(1, member.isOfficer);
                stmt.setBoolean(2, member.isOwner);
                stmt.setString(3, guildId);
                stmt.setString(4, member.playerId);
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to promote playerId =" + member.playerId, ex);
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
                                    Connection connection) {
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
    public void savePlayerSessions(GuildSession guildSession, PlayerSession playerSession, PlayerSession recipientPlayerSession,
                                   SquadNotification squadNotification) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            savePlayerSession(playerSession, connection);
            savePlayerSession(recipientPlayerSession, connection);
            saveNotification(guildSession.getGuildId(), squadNotification, connection);
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

        final String updateSql = "update Player set secondaryAccount = ? where id = ?";

        final String settingsSql = "insert into PlayerSettings (id, name, upgrades) values " +
                "(?, 'new', '{}')";

        try {
            if (playerId.endsWith("_1")) {
                String primaryAccount = PlayerIdentitySwitch.getPrimaryAccount(playerId);
                try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
                    stmt.setString(1, playerId);
                    stmt.setString(2, primaryAccount);
                    stmt.executeUpdate();
                }
            } else {
                try (PreparedStatement stmt = connection.prepareStatement(playerSql)) {
                    stmt.setString(1, playerId);
                    stmt.setString(2, secret);
                    stmt.executeUpdate();
                }
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
    public void newGuild(String playerId, GuildSettings guildSettings) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            createNewGuild(playerId, guildSettings, connection);
            insertSquadMember(guildSettings.getGuildId(), playerId, false, true, 0, connection);
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create a new player", ex);
        }
    }

    private void createNewGuild(String playerId, GuildSettings guildSettings, Connection connection) {
        final String squadSql = "insert into Squads (id, faction, name, icon, description, openEnrollment, minScoreAtEnrollment) values " +
                "(?, ?, ?, ?, ?, ?, ?)";

        final String playerSettingsSql = "update PlayerSettings " +
                "set guildId = ? " +
                "WHERE id = ?";

        try {
            try (PreparedStatement stmt = connection.prepareStatement(squadSql)) {
                stmt.setString(1, guildSettings.getGuildId());
                stmt.setString(2, guildSettings.getFaction().toString());
                stmt.setString(3, guildSettings.getGuildName());
                stmt.setString(4, guildSettings.getIcon());
                stmt.setString(5, guildSettings.getDescription());
                stmt.setBoolean(6, guildSettings.getOpenEnrollment());
                stmt.setInt(7, guildSettings.getMinScoreAtEnrollment());
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = connection.prepareStatement(playerSettingsSql)) {
                stmt.setString(1, guildSettings.getGuildId());
                stmt.setString(2, playerId);
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save player settings id=" + playerId, ex);
        }
    }

    private void insertSquadMember(String guildId, String playerId, boolean isOfficer, boolean isOwner, long joinDate,
                                   Connection connection) {
        final String squadMemberSql = "insert into SquadMembers (guildId, playerId, isOfficer, isOwner, joinDate) values " +
                "(?, ?, ?, ?, ?)";

        try {
            try (PreparedStatement stmt = connection.prepareStatement(squadMemberSql)) {
                stmt.setString(1, guildId);
                stmt.setString(2, playerId);
                stmt.setInt(3, isOfficer ? 1 : 0);
                stmt.setInt(4, isOwner ? 1 : 0);
                stmt.setLong(5, joinDate);
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save player settings id=" + playerId, ex);
        }
    }

    private void deleteSquadMember(String playerId, Connection connection) {
        final String squadMemberSql = "delete from SquadMembers where playerId = ?";

        try {
            try (PreparedStatement stmt = connection.prepareStatement(squadMemberSql)) {
                stmt.setString(1, playerId);
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete SquadMembers for playerId=" + playerId, ex);
        }
    }

    @Override
    public GuildSettings loadGuildSettings(String guildId) {
        final String sql = "SELECT id, name, faction, perks, members, warId, description, icon, openEnrollment, minScoreAtEnrollment, " +
                "warSignUpTime " +
                "FROM Squads s WHERE s.id = ?";

        final String squadPlayers = "SELECT m.playerId, s.name, m.isOfficer, m.isOwner, m.joinDate, m.troopsDonated, m.troopsReceived, " +
                "m.warParty " +
                "FROM SquadMembers m, PlayerSettings s WHERE m.guildId = ? " +
                "and m.playerId = s.Id";

        final String notificationsSql = "SELECT id, orderNo, date, playerId, name, squadMessageType, message, squadNotification " +
                "FROM SquadNotifications s WHERE s.guildId = ?";

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

                        guildSettings.setOpenEnrollment(rs.getBoolean("openEnrollment"));
                        guildSettings.setMinScoreAtEnrollment(rs.getInt("minScoreAtEnrollment"));
                        guildSettings.setWarSignUpTime(rs.getLong("warSignUpTime"));
                    }
                }

                try (PreparedStatement pstmt = con.prepareStatement(squadPlayers)) {
                    pstmt.setString(1, guildId);
                    ResultSet rs = pstmt.executeQuery();

                    while (rs.next()) {
                        String playerId = rs.getString("playerId");
                        String playerName = rs.getString("name");
                        boolean isOfficer = rs.getBoolean("isOfficer");
                        boolean isOwner = rs.getBoolean("isOwner");
                        long joinDate = rs.getLong("joinDate");
                        long troopsDonated = rs.getLong("troopsDonated");
                        long troopsReceived = rs.getLong("troopsReceived");
                        boolean warParty = rs.getBoolean("warParty");
                        guildSettings.addMember(playerId, playerName, isOwner, isOfficer, joinDate,
                                troopsDonated, troopsReceived, warParty);
                    }
                }

                try (PreparedStatement pstmt = con.prepareStatement(notificationsSql)) {
                    pstmt.setString(1, guildId);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        String id = rs.getString("id");
                        long orderNo = rs.getLong("orderNo");
                        long date = rs.getLong("date");
                        String playerId = rs.getString("playerId");
                        String name = rs.getString("name");
                        SquadMsgType squadMessageType = SquadMsgType.valueOf(rs.getString("squadMessageType"));
                        String message = rs.getString("message");
                        String squadNotificationJson = rs.getString("squadNotification");
                        SquadNotificationData data = null;
                        if (squadNotificationJson != null) {
                            data = mapSquadNotificationData(squadMessageType, squadNotificationJson);
                        }
                        SquadNotification squadNotification =
                                new SquadNotification(guildId, guildSettings.getGuildName(), date, orderNo, id, message, name, playerId, squadMessageType, data);
                        guildSettings.addSquadNotification(squadNotification);
                    }
                }

                if (guildSettings != null)
                    guildSettings.afterLoad();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load Guild settings from DB id=" + guildId, ex);
        }

        return guildSettings;
    }

    private SquadNotificationData mapSquadNotificationData(SquadMsgType squadMessageType, String squadNotificationJson)
            throws Exception {
        SquadNotificationData squadNotificationData = null;
        if (squadMessageType != null) {
            switch (squadMessageType) {
                case troopRequest:
                    squadNotificationData = ServiceFactory.instance().getJsonParser()
                            .fromJsonString(squadNotificationJson, TroopRequestData.class);
                    break;
                case troopDonation:
                    squadNotificationData = ServiceFactory.instance().getJsonParser()
                            .fromJsonString(squadNotificationJson, TroopDonationData.class);
                    break;
            }
        }
        return squadNotificationData;
    }

    @Override
    public void editGuild(String guildId, String description, String icon, Integer minScoreAtEnrollment,
                          boolean openEnrollment) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            editGuild(guildId, description, icon, minScoreAtEnrollment, openEnrollment, connection);
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create a new player", ex);
        }
    }

    private void editGuild(String guildId, String description, String icon, Integer minScoreAtEnrollment,
                           boolean openEnrollment, Connection connection) {
        final String squadSql = "update Squads " +
                "set description = ?, " +
                "icon = ?, " +
                "minScoreAtEnrollment = ?, " +
                "openEnrollment = ? " +
                "where id = ?";

        try {
            try (PreparedStatement stmt = connection.prepareStatement(squadSql)) {
                stmt.setString(1, description);
                stmt.setString(2, icon);
                stmt.setInt(3, minScoreAtEnrollment);
                stmt.setBoolean(4, openEnrollment);
                stmt.setString(5, guildId);
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
        final String sql = "SELECT id, name, faction, description, icon, openEnrollment, minScoreAtEnrollment, " +
                " (select count(1) from SquadMembers m where m.guildId = s.id) as numMembers " +
                "FROM Squads s WHERE s.faction = ?";

        List<Squad> squads = new ArrayList<>();
        try {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, faction.name());
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    Squad squad = new Squad();
                    squad._id = rs.getString("id");
                    squad.name = rs.getString("name");
                    squad.faction = FactionType.valueOf(rs.getString("faction"));
                    squad.icon = rs.getString("icon");
                    squad.openEnrollment = rs.getBoolean("openEnrollment");
                    squad.minScore = rs.getInt("minScoreAtEnrollment");
                    squad.rank = 0;
                    squad.level = 0;
                    squad.activeMemberCount = rs.getInt("minScoreAtEnrollment");
                    squad.members = rs.getInt("numMembers");
                    squad.score = 1;
                    squads.add(squad);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load Guild list from DB", ex);
        }

        return squads;
    }

    @Override
    public PlayerSecret getPlayerSecret(String primaryId) {
        final String sql = "SELECT id, secret, secondaryAccount " +
                "FROM Player s WHERE s.id = ?";

        PlayerSecret playerSecret = null;
        try {
            try (Connection con = getConnection()) {
                try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                    pstmt.setString(1, primaryId);
                    ResultSet rs = pstmt.executeQuery();

                    while (rs.next()) {
                        playerSecret = new PlayerSecret(rs.getString("id"),
                                rs.getString("secret"),
                                rs.getString("secondaryAccount"));
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to Player secret id=" + primaryId, ex);
        }

        return playerSecret;
    }

    @Override
    public void saveNotification(String guildId, SquadNotification squadNotification) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            saveNotification(guildId, squadNotification.getId(),
                    squadNotification.getOrderNo(),
                    squadNotification.getDate(),
                    squadNotification.getPlayerId(),
                    squadNotification.getName(),
                    squadNotification.getType(),
                    squadNotification.getMessage(),
                    squadNotification.getData(),
                    connection);
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create a new player", ex);
        }
    }

    @Override
    public void saveGuildChange(GuildSettings guildSettings, PlayerSession playerSession, SquadNotification squadNotification) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            savePlayerSession(playerSession, connection);
            if (squadNotification != null)
                saveNotification(squadNotification.getGuildId(), squadNotification, connection);
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create a new player", ex);
        }
    }

    private void saveNotification(String guildId, SquadNotification squadNotification, Connection connection) {
        saveNotification(guildId, squadNotification.getId(),
                squadNotification.getOrderNo(),
                squadNotification.getDate(),
                squadNotification.getPlayerId(),
                squadNotification.getName(),
                squadNotification.getType(),
                squadNotification.getMessage(),
                squadNotification.getData(),
                connection);
    }

    private void saveNotification(String guildId, String id, long orderNo, long date, String playerId, String name, SquadMsgType type,
                                  String message, SquadNotificationData data, Connection connection) {
        final String squadSql = "insert into squadNotifications (guildId, id, orderNo, date, playerId, name, squadMessageType, message, squadNotification) " +
                "values (?,?,?,?,?,?,?,?,?)";

        try {
            try (PreparedStatement stmt = connection.prepareStatement(squadSql)) {
                stmt.setString(1, guildId);
                stmt.setString(2, id);
                stmt.setLong(3, orderNo);
                stmt.setLong(4, date);
                stmt.setString(5, playerId);
                stmt.setString(6, name);
                stmt.setString(7, type.toString());
                stmt.setString(8, message);
                stmt.setString(9, data != null ? ServiceFactory.instance().getJsonParser()
                        .toJson(data) : null);
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save squad notification =" + id, ex);
        }
    }

    @Override
    public List<Squad> searchGuildByName(String searchTerm) {
        try (Connection connection = getConnection()) {
            return searchGuildByName(searchTerm, connection);
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create a new player", ex);
        }
    }

    private List<Squad> searchGuildByName(String searchTerm, Connection connection) {
        final String sql = "SELECT id, name, faction, description, icon, openEnrollment, minScoreAtEnrollment, " +
                " (select count(1) from SquadMembers m where m.guildId = s.id) as numMembers " +
                "FROM Squads s WHERE s.name like ?";

        List<Squad> squads = new ArrayList<>();
        try {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, "%" + searchTerm + "%");
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    Squad squad = new Squad();
                    squad._id = rs.getString("id");
                    squad.name = rs.getString("name");
                    squad.faction = FactionType.valueOf(rs.getString("faction"));
                    squad.icon = rs.getString("icon");
                    squad.openEnrollment = rs.getBoolean("openEnrollment");
                    squad.minScore = rs.getInt("minScoreAtEnrollment");
                    squad.rank = 0;
                    squad.level = 0;
                    squad.activeMemberCount = rs.getInt("minScoreAtEnrollment");
                    squad.members = rs.getInt("numMembers");
                    squad.score = 1;
                    squads.add(squad);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to search Guild list from DB", ex);
        }

        return squads;
    }

    @Override
    public void saveWarMatchMake(String guildId, List<String> participantIds, SquadNotification squadNotification, Long time) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            saveWarMatchSignUp(guildId, participantIds, time, connection);
            saveNotification(guildId, squadNotification, connection);
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create a new player", ex);
        }
    }

    private void saveWarMatchSignUp(String guildId, List<String> participantIds, Long time, Connection connection) {
        final String squadSql = "update Squads " +
                "set warSignUpTime = ? " +
                "where id = ?";

        final String squadMembersRestSql = "update SquadMembers " +
                "set warParty = 0 " +
                "where guildId = ?";

        final String squadMembersSql = "update SquadMembers " +
                "set warParty = ? " +
                "where guildId = ? and playerId = ?";

        try {
            try (PreparedStatement stmt = connection.prepareStatement(squadSql)) {
                if (time != null)
                    stmt.setLong(1, time);
                else
                    stmt.setNull(1, Types.INTEGER);
                stmt.setString(2, guildId);
                stmt.executeUpdate();
            }

            if (participantIds != null) {
                try (PreparedStatement stmt = connection.prepareStatement(squadMembersRestSql)) {
                    stmt.setString(1, guildId);
                    stmt.executeUpdate();
                }

                for (int i = 0; i < participantIds.size(); i++) {
                    try (PreparedStatement stmt = connection.prepareStatement(squadMembersSql)) {
                        stmt.setBoolean(1, true);
                        stmt.setString(2, guildId);
                        stmt.setString(3, participantIds.get(i));
                        stmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to match make for squad id=" + guildId, ex);
        }
    }

    @Override
    public void saveWarMatchCancel(String guildId, SquadNotification squadNotification) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            saveWarMatchSignUp(guildId, null, null, connection);
            saveNotification(guildId, squadNotification, connection);
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create a new player", ex);
        }
    }
}
