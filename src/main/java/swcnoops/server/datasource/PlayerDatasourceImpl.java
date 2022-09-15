package swcnoops.server.datasource;

import com.mongodb.*;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;
import org.mongojack.JacksonMongoCollection;
import swcnoops.server.Config;
import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.guild.GuildHelper;
import swcnoops.server.commands.player.PlayerPvpBattleComplete;
import swcnoops.server.game.PvpMatch;
import swcnoops.server.model.*;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.*;
import swcnoops.server.session.creature.CreatureManager;
import swcnoops.server.session.inventory.Troops;
import swcnoops.server.session.training.BuildUnits;
import swcnoops.server.session.training.DeployableQueue;
import swcnoops.server.session.training.TrainingManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static swcnoops.server.session.NotificationFactory.mapSquadNotificationData;

public class PlayerDatasourceImpl implements PlayerDataSource {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private JacksonMongoCollection<Player> playerCollection;

    public PlayerDatasourceImpl() {
    }

    @Override
    public void initOnStartup() {
        checkAndPrepareDB();
        initMongoClient();
    }

    @Override
    public void shutdown() {
        if (this.mongoClient != null)
            this.mongoClient.close();
    }

    private void initMongoClient() {
        ConnectionString connectionString =
                new ConnectionString(ServiceFactory.instance().getConfig().mongoDBConnection);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build())
                .build();
        this.mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase("dev");
        this.playerCollection = JacksonMongoCollection.builder()
                .build(this.mongoClient, "dev", "player", Player.class, UuidRepresentation.STANDARD);
    }

    public void checkAndPrepareDB() {
        try {
            try (Connection connection = getConnection()) {
                try (Statement stmt = connection.createStatement()) {
                    String sql = getCreatePlayerDBSql();
                    stmt.executeUpdate(sql);
                }

                String tempDbURL = ServiceFactory.instance().getConfig().playerSqliteDB + ".temp";
                String dbLocation = tempDbURL.substring("jdbc:sqlite:".length());
                File file = new File(dbLocation);
                if (file.exists())
                    file.delete();

                try (Connection tempDBConnection = getTempDBConnection(tempDbURL)) {
                    try (Statement stmt = tempDBConnection.createStatement()) {
                        String sql = getCreatePlayerDBSql();
                        stmt.executeUpdate(sql);

                        try (ResultSet rs = tempDBConnection.getMetaData().getTables(null, null, null, null)) {
                            while (rs.next()) {
                                String tableName = rs.getString("TABLE_NAME");

                                // process new columns
                                try(ResultSet columns = tempDBConnection.getMetaData().getColumns(null,null, tableName, null)){
                                    while(columns.next()) {
                                        String columnName = columns.getString("COLUMN_NAME");
                                        String typeName = columns.getString("TYPE_NAME");
                                        try (ResultSet destColumns = connection.getMetaData().getColumns(null, null, tableName, columnName)) {
                                            // if column is missing then we go and create it
                                            if(!destColumns.next()) {
                                                try (Statement addColumnStmt = connection.createStatement()) {
                                                    String columnSql = "alter table " + tableName + " add " + columnName + " " + typeName + ";";
                                                    addColumnStmt.executeUpdate(columnSql);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
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

    private Connection getTempDBConnection(String url) throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        return conn;
    }

    @Override
    public Player loadPlayer(String playerId) {
        Player player = this.playerCollection.find(eq("_id", playerId)).first();
        return player;
    }

    @Override
    public void savePlayerName(PlayerSession playerSession, String playerName) {
        playerSession.getPlayerSettings().setKeepAlive(ServiceFactory.getSystemTimeSecondsFromEpoch());
        Bson simpleUpdate = set("playerSettings.name", playerName);
        Bson simpleUpdateKeepAlive = set("playerSettings.keepAlive", playerSession.getPlayerSettings().getKeepAlive());
        Bson combined = combine(simpleUpdate, simpleUpdateKeepAlive);
        UpdateResult result = this.playerCollection.updateOne(Filters.eq("_id", playerSession.getPlayerId()),
                combined);

        playerSession.getPlayer().getPlayerSettings().setName(playerName);
    }

    @Override
    public PlayerSettings loadPlayerSettings(String playerId) {
        final String sql = "SELECT id, name, faction, baseMap, upgrades, deployables, contracts, creature, troops, donatedTroops, " +
                "inventoryStorage, currentQuest, campaigns, preferences, guildId, unlockedPlanets, scalars " +
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
                        playerSettings.setPlayerCampaignMission(playerCampaignMission);

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

                        String scalarsString = rs.getString("scalars");
                        Scalars scalars = null;
                        if (scalarsString != null) {
                            scalars = ServiceFactory.instance().getJsonParser().fromJsonString(scalarsString, Scalars.class);
                        } else {
                            scalars = new Scalars();
                        }
                        playerSettings.setScalars(scalars);

                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to load player settings from DB id=" + playerId, ex);
        }

        return playerSettings;
    }

    @Override
    public void savePlayerSession(PlayerSession playerSession) {
        try (ClientSession session = this.mongoClient.startSession()) {
            session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            savePlayerSession(playerSession, null, session);
            session.commitTransaction();
        } catch (MongoCommandException e) {
            throw new RuntimeException("Failed to save player session " + playerSession.getPlayerId(), e);
        }
    }

    @Override
    public void savePlayerKeepAlive(PlayerSession playerSession) {
        playerSession.getPlayer().getPlayerSettings().setKeepAlive(ServiceFactory.getSystemTimeSecondsFromEpoch());
        UpdateResult result = this.playerCollection.updateOne(Filters.eq("_id", playerSession.getPlayerId()),
                set("playerSettings.keepAlive", playerSession.getPlayer().getPlayerSettings().getKeepAlive()));
    }

    @Override
    public void recoverWithPlayerSettings(PlayerSession playerSession, PlayerModel playerModel, Map<String, String> sharedPrefs) {
        ServiceFactory.instance().getSessionManager().resetPlayerSettings(playerSession.getPlayerSettings());
        ServiceFactory.instance().getSessionManager().setFromModel(playerSession.getPlayerSettings(), playerModel);

        playerSession.getPlayerSettings().setKeepAlive(ServiceFactory.getSystemTimeSecondsFromEpoch());
        playerSession.getPlayerSettings().getSharedPreferences().putAll(sharedPrefs);
        playerSession.getPlayer().getPlayerSecret().setMissingSecret(false);

        Bson simpleUpdate = set("playerSettings", playerSession.getPlayerSettings());
        Bson recoverUpdate = set("playerSecret.missingSecret", playerSession.getPlayer().getPlayerSecret().getMissingSecret());
        Bson combined = combine(recoverUpdate, simpleUpdate);
        UpdateResult result = this.playerCollection.updateOne(Filters.eq("_id", playerSession.getPlayerId()),
                combined);

        // reload and initialise
        Player player = this.loadPlayer(playerSession.getPlayerId());
        playerSession.initialise(player);
    }

    @Override
    public void joinSquad(GuildSession guildSession, PlayerSession playerSession, SquadNotification squadNotification) {
//        try (Connection connection = getConnection()) {
//            connection.setAutoCommit(false);
//            savePlayerSession(playerSession, connection);
//
//            if (guildSession.canEdit()) {
//                insertSquadMember(guildSession.getGuildId(), playerSession.getPlayerId(), false, false, 0, connection);
//                deleteJoinRequestNotifications(squadNotification.getGuildId(), squadNotification.getPlayerId(), connection);
//                setAndSaveGuildNotification(guildSession, squadNotification, connection);
//            }
//            connection.commit();
//        } catch (SQLException ex) {
//            throw new RuntimeException("Failed to save player settings id=" + playerSession.getPlayerId(), ex);
//        }
    }

    @Override
    public void joinRequest(GuildSession guildSession, PlayerSession playerSession, SquadNotification squadNotification) {
//        try (Connection connection = getConnection()) {
//            connection.setAutoCommit(false);
//            savePlayerSession(playerSession, connection);
//
//            if (guildSession.canEdit()) {
//                deleteJoinRequestNotifications(squadNotification.getGuildId(), squadNotification.getPlayerId(), connection);
//                saveNotification(squadNotification.getGuildId(), squadNotification, connection);
//            }
//            connection.commit();
//        } catch (SQLException ex) {
//            throw new RuntimeException("Failed to save player settings id=" + playerSession.getPlayerId(), ex);
//        }
    }

    @Override
    public void joinRejected(GuildSession guildSession, PlayerSession playerSession, SquadNotification squadNotification) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);

            if (guildSession.canEdit()) {
                deleteJoinRequestNotifications(squadNotification.getGuildId(), squadNotification.getPlayerId(), connection);
                setAndSaveGuildNotification(guildSession, squadNotification, connection);
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
//        try (Connection connection = getConnection()) {
//            connection.setAutoCommit(false);
//            savePlayerSession(playerSession, connection);
//            if (guildSession.canEdit()) {
//                deleteSquadMember(playerSession.getPlayerId(), connection);
//                deleteNotifications(squadNotification.getGuildId(), playerSession.getPlayerId(), connection);
//                setAndSaveGuildNotification(guildSession, squadNotification, connection);
//            }
//            connection.commit();
//        } catch (SQLException ex) {
//            throw new RuntimeException("Failed to save player settings id=" + playerSession.getPlayerId(), ex);
//        }
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
                                SquadRole squadRole) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            if (guildSession.canEdit()) {
                updateSquadMember(guildSession.getGuildId(), playerSession.getPlayerId(),
                        squadRole == SquadRole.Officer, connection);
                setAndSaveGuildNotification(guildSession, squadNotification, connection);
            }
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save player settings id=" + playerSession.getPlayerId(), ex);
        }
    }

    private void updateSquadMember(String guildId, String playerId, boolean isOfficer, Connection connection) {
        final String squadSql = "update SquadMembers " +
                "set isOfficer = ? " +
                "where guildId = ? and playerId = ?";

        try {
            try (PreparedStatement stmt = connection.prepareStatement(squadSql)) {
                stmt.setBoolean(1, isOfficer);
                stmt.setString(2, guildId);
                stmt.setString(3, playerId);
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to promote playerId =" + playerId, ex);
        }
    }

    private void savePlayerSession(PlayerSession playerSession, Connection connection, ClientSession session) {
        // TODO - redo these to do straight through amendments to the settings
        mapDeployablesToPlayerSettings(playerSession);
        playerSession.getPlayerSettings().setBuildContracts(mapContractsToPlayerSettings(playerSession));
        mapCreatureToPlayerSession(playerSession);
        mapDonatedTroopsToPlayerSession(playerSession);

        int hqLevel = playerSession.getHeadQuarter().getBuildingData().getLevel();
        int xp = ServiceFactory.getXpFromBuildings(playerSession.getPlayerMapItems().getBaseMap().buildings);

        playerSession.getPlayerSettings().setHqLevel(hqLevel);
        playerSession.getPlayerSettings().getScalars().xp = xp;

        playerSession.getPlayer().getPlayerSettings().setKeepAlive(ServiceFactory.getSystemTimeSecondsFromEpoch());
        UpdateResult result = this.playerCollection.updateOne(session, Filters.eq("_id", playerSession.getPlayerId()),
                set("playerSettings", playerSession.getPlayer().getPlayerSettings()));
    }

    private void mapDonatedTroopsToPlayerSession(PlayerSession playerSession) {
        // replace the players troops with new data before saving
        DonatedTroops donatedTroops = playerSession.getDonatedTroops();
        PlayerSettings playerSettings = playerSession.getPlayerSettings();
        playerSettings.setDonatedTroops(donatedTroops);
    }

    private void mapCreatureToPlayerSession(PlayerSession playerSession) {
        // replace the players settings with new data before saving
        PlayerSettings playerSettings = playerSession.getPlayerSettings();
        CreatureManager creatureManager = playerSession.getCreatureManager();
        playerSettings.setCreature(creatureManager.getCreature());
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

    private void mapDeployablesToPlayerSettings(PlayerSession playerSession) {
        Deployables deployables = playerSession.getPlayerSettings().getDeployableTroops();
        TrainingManager trainingManager = playerSession.getTrainingManager();
        mapToPlayerSetting(trainingManager.getDeployableTroops(), deployables.troop);
        mapToPlayerSetting(trainingManager.getDeployableChampion(), deployables.champion);
        mapToPlayerSetting(trainingManager.getDeployableHero(), deployables.hero);
        mapToPlayerSetting(trainingManager.getDeployableSpecialAttack(), deployables.specialAttack);
    }

    private void mapToPlayerSetting(DeployableQueue deployableQueue, Map<String, Integer> storage) {
        storage.clear();
        storage.putAll(deployableQueue.getDeployableUnits());
    }

    @Override
    public void savePlayerSessions(GuildSession guildSession, PlayerSession playerSession, PlayerSession recipientPlayerSession,
                                   SquadNotification squadNotification) {
        throw new RuntimeException("Not supported");

//        try (Connection connection = getConnection()) {
//            connection.setAutoCommit(false);
//            savePlayerSession(playerSession, connection);
//            savePlayerSession(recipientPlayerSession, connection);
//            setAndSaveGuildNotification(guildSession, squadNotification, connection);
//            connection.commit();
//        } catch (SQLException ex) {
//            throw new RuntimeException("Failed to save player settings id=" + playerSession.getPlayerId() +
//                    " and id=" + recipientPlayerSession.getPlayerId(), ex);
//        }
    }

    @Override
    public void newPlayer(String playerId, String secret, PlayerModel playerModel, Map<String, String> sharedPrefs, String name) {
        newPlayer(playerId, secret, false, playerModel, sharedPrefs, name);
    }

    @Override
    public void newPlayerWithMissingSecret(String playerId, String secret, PlayerModel playerModel,
                                           Map<String, String> sharedPrefs, String name)
    {
        newPlayer(playerId, secret, true, playerModel, sharedPrefs, name);
    }

    private void newPlayer(String playerId, String secret, boolean missingSecret, PlayerModel playerModel,
                           Map<String, String> sharedPrefs, String name)
    {
        if (playerId.endsWith("_1"))
            throw new RuntimeException("secondary not supported yet");

        Player player = new Player(playerId);
        player.setPlayerSecret(new PlayerSecret(secret, null, missingSecret));
        player.setPlayerSettings(new PlayerSettings());
        ServiceFactory.instance().getSessionManager().setFromModel(player.getPlayerSettings(), playerModel);
        if (sharedPrefs != null)
            player.getPlayerSettings().getSharedPreferences().putAll(sharedPrefs);
        player.getPlayerSettings().setName(name);
        this.playerCollection.insertOne(player);
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
        throw new RuntimeException("not supported");
//        final String squadSql = "insert into Squads (id, faction, name, icon, description, openEnrollment, minScoreAtEnrollment) values " +
//                "(?, ?, ?, ?, ?, ?, ?)";
//
//        final String playerSettingsSql = "update PlayerSettings " +
//                "set guildId = ? " +
//                "WHERE id = ?";
//
//        try {
//            try (PreparedStatement stmt = connection.prepareStatement(squadSql)) {
//                stmt.setString(1, guildSettings.getGuildId());
//                stmt.setString(2, guildSettings.getFaction().toString());
//                stmt.setString(3, guildSettings.getGuildName());
//                stmt.setString(4, guildSettings.getIcon());
//                stmt.setString(5, guildSettings.getDescription());
//                stmt.setBoolean(6, guildSettings.getOpenEnrollment());
//                stmt.setInt(7, guildSettings.getMinScoreAtEnrollment());
//                stmt.executeUpdate();
//            }
//
//            try (PreparedStatement stmt = connection.prepareStatement(playerSettingsSql)) {
//                stmt.setString(1, guildSettings.getGuildId());
//                stmt.setString(2, playerId);
//                stmt.executeUpdate();
//            }
//        } catch (SQLException ex) {
//            throw new RuntimeException("Failed to save player settings id=" + playerId, ex);
//        }
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
                "warSignUpTime, warId " +
                "FROM Squads s WHERE s.id = ?";

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
                        guildSettings.setWarId(rs.getString("warId"));
                    }
                }

                List<Member> members = loadSquadMembers(guildSettings.getGuildId(), con);

                if (guildSettings != null) {
                    GuildMembers guildMembers = new GuildMembers(guildSettings.getGuildId(), members);
                    guildSettings.setGuildMembers(guildMembers);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load Guild settings from DB id=" + guildId, ex);
        }

        return guildSettings;
    }

    @Override
    public List<Member> loadSquadMembers(String guildId) {
        try (Connection connection = getConnection()) {
            return loadSquadMembers(guildId, connection);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load squad members", ex);
        }
    }

    private List<Member> loadSquadMembers(String guildId, Connection connection) throws Exception {
        List<Member> members = new ArrayList<>();

        final String squadPlayers = "SELECT m.playerId, s.name, m.isOfficer, m.isOwner, m.joinDate, m.troopsDonated, m.troopsReceived, " +
                "m.warParty, s.hqLevel " +
                "FROM SquadMembers m, PlayerSettings s WHERE m.guildId = ? " +
                "and m.playerId = s.Id";

        try (PreparedStatement pstmt = connection.prepareStatement(squadPlayers)) {
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
                int hqLevel = rs.getInt("hqLevel");
                Member member = GuildHelper.createMember(playerId, playerName, isOwner,
                        isOfficer, joinDate, troopsDonated, troopsReceived, warParty, hqLevel);
                members.add(member);
            }
        }

        return members;
    }

    @Override
    public List<WarHistory> loadWarHistory(String squadId) {
        try (Connection connection = getConnection()) {
            return loadWarHistory(squadId, connection);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load war history ", ex);
        }
    }

    private List<WarHistory> loadWarHistory(String guildId, Connection connection) throws Exception {
        List<WarHistory> wars = new ArrayList<>();

        final String squadPlayers = "SELECT w.warId, w.squadIdA, w.squadIdB, w.processedEndTime, w.squadAScore, w.squadBScore, " +
                "s.name, s.icon " +
                "FROM War w, Squads s " +
                "WHERE w.processedEndTime > 0 " +
                "AND ((w.squadIdB = s.id and w.squadIdA = ?) or (w.squadIdA = s.id and w.squadIdB = ?))";

        try (PreparedStatement pstmt = connection.prepareStatement(squadPlayers)) {
            pstmt.setString(1, guildId);
            pstmt.setString(2, guildId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                WarHistory warHistory = new WarHistory();
                warHistory.warId = rs.getString("warId");
                warHistory.opponentGuildId = rs.getString("squadIdB");
                warHistory.opponentName = rs.getString("name");
                warHistory.opponentIcon = rs.getString("icon");
                warHistory.opponentScore = rs.getInt("squadBScore");
                warHistory.endDate = rs.getLong("processedEndTime");

                if (!warHistory.opponentGuildId.equals(guildId)) {
                    warHistory.score = rs.getInt("squadAScore");
                } else {
                    warHistory.opponentGuildId = rs.getString("squadIdA");
                    warHistory.opponentScore = rs.getInt("squadAScore");
                    warHistory.score = rs.getInt("squadBScore");
                }

                wars.add(warHistory);
            }
        }

        return wars;
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
        Player player = this.playerCollection.find(eq("_id", primaryId)).projection(include("playerSecret")).first();

        PlayerSecret playerSecret = null;
        if (player != null) {
            playerSecret = player.getPlayerSecret();
        }

        return playerSecret;
    }

    @Override
    public void saveNotification(GuildSession guildSession, SquadNotification squadNotification) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            setAndSaveGuildNotification(guildSession, squadNotification, connection);
//            saveNotification(guildId, squadNotification.getId(),
//                    squadNotification.getOrderNo(),
//                    squadNotification.getDate(),
//                    squadNotification.getPlayerId(),
//                    squadNotification.getName(),
//                    squadNotification.getType(),
//                    squadNotification.getMessage(),
//                    squadNotification.getData(),
//                    connection);
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create a new player", ex);
        }
    }

    @Override
    public void saveGuildChange(GuildSettings guildSettings, PlayerSession playerSession, SquadNotification squadNotification) {
//        try (Connection connection = getConnection()) {
//            connection.setAutoCommit(false);
//            savePlayerSession(playerSession, connection);
//            if (squadNotification != null)
//                saveNotification(squadNotification.getGuildId(), squadNotification, connection);
//            connection.commit();
//        } catch (SQLException ex) {
//            throw new RuntimeException("Failed to create a new player", ex);
//        }
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
    public void saveWarMatchMake(FactionType faction, GuildSession guildSession, List<String> participantIds,
                                 SquadNotification squadNotification, Long time) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            saveMatchMake(faction, guildSession.getGuildId(), participantIds, time, connection);
            saveWarMatchSignUp(guildSession.getGuildId(), participantIds, time, connection);
            setAndSaveGuildNotification(guildSession, squadNotification, connection);
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create a new player", ex);
        }
    }

    private void saveMatchMake(FactionType faction, String guildId, List<String> participantIds, Long time, Connection connection) {
        final String matchMakeSql = "insert into MatchMake (guildId, warSignUpTime, faction, participants) values " +
                "(?,?,?,?)";

        try {
            try (PreparedStatement stmt = connection.prepareStatement(matchMakeSql)) {
                stmt.setString(1, guildId);
                stmt.setLong(2, time);
                stmt.setString(3, faction.toString());
                stmt.setString(4, ServiceFactory.instance().getJsonParser().toJson(participantIds));
                stmt.executeUpdate();
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save match make for squad id=" + guildId, ex);
        }
    }

    private void saveWarMatchSignUp(String guildId, List<String> participantIds, Long time, Connection connection) {
        final String squadSql = "update Squads " +
                "set warSignUpTime = ? " +
                "where id = ?";

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
                clearWarParty(guildId, connection);

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

    private void clearWarParty(String guildId, Connection connection) throws SQLException {
        final String squadMembersNotSignedUpSql = "update SquadMembers " +
                "set warParty = 0 " +
                "where guildId = ?";

        try (PreparedStatement stmt = connection.prepareStatement(squadMembersNotSignedUpSql)) {
            stmt.setString(1, guildId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void saveWarMatchCancel(GuildSession guildSession, SquadNotification squadNotification) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            deleteMatchMake(guildSession.getGuildId(), connection);
            saveWarMatchSignUp(guildSession.getGuildId(), null, null, connection);
            setAndSaveGuildNotification(guildSession, squadNotification, connection);
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create a new player", ex);
        }
    }

    private void deleteMatchMake(String guildId, Connection connection) {
        final String matchMakeSql = "delete from MatchMake where guildId = ?";

        try {
            try (PreparedStatement stmt = connection.prepareStatement(matchMakeSql)) {
                stmt.setString(1, guildId);
                stmt.executeUpdate();
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete match make for squad id=" + guildId, ex);
        }
    }

    @Override
    public String matchMake(String guildId) {
        String warId;
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            warId = matchMake(guildId, connection);
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create a new player", ex);
        }

        return warId;
    }

    private String matchMake(String guildId, Connection connection) {
        final String matchMakeSql = "select m.guildId from MatchMake m where m.guildId != ? ORDER BY RANDOM() LIMIT 1";
        final String deleteMakeSql = "delete from MatchMake where guildId in (?,?)";

        String warId = null;
        try {
            String rivalId = null;
            try (PreparedStatement stmt = connection.prepareStatement(matchMakeSql)) {
                stmt.setString(1, guildId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    rivalId = rs.getString("guildId");
                }
            }

            if (rivalId != null) {
                try (PreparedStatement stmt = connection.prepareStatement(deleteMakeSql)) {
                    stmt.setString(1, guildId);
                    stmt.setString(2, rivalId);
                    stmt.executeUpdate();
                }

                warId = saveWar(guildId, rivalId, connection);
                saveSquadWar(guildId, warId, connection);
                saveSquadWar(rivalId, warId, connection);
                insertWarParticipants(warId, guildId, rivalId, connection);
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Failed to match make for squad id=" + guildId, ex);
        }

        return warId;
    }

    private void insertWarParticipants(String warId, String guildId, String rivalId, Connection connection) {
        final String warParticipantsSql = "insert into WarParticipants (playerId, warId, squadId, warMap, " +
                "donatedTroops, turns, attacksWon, defensesWon, victoryPoints, score) " +
                "select p.id, s.warId, s.id, ifnull(p.warMap, p.baseMap), null, 3, 0, 0, 3, 0 " +
                "from SquadMembers m, Squads s, PlayerSettings p " +
                "where s.id in (?,?) and s.warId = ? and m.guildId = s.id and m.warParty = 1 and p.id = m.playerId";

        try {
            try (PreparedStatement stmt = connection.prepareStatement(warParticipantsSql)) {
                stmt.setString(1, guildId);
                stmt.setString(2, rivalId);
                stmt.setString(3, warId);
                stmt.executeUpdate();
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create WarParticipants for war id=" + warId, ex);
        }
    }

    private void saveSquadWar(String guildId, String warId, Connection connection) {
        final String squadSql = "update Squads " +
                "set warId = ?, warSignUpTime = null " +
                "where id = ?";

        try {
            try (PreparedStatement stmt = connection.prepareStatement(squadSql)) {
                stmt.setString(1, warId);
                stmt.setString(2, guildId);
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save warId for squadId =" + guildId, ex);
        }
    }

    private String saveWar(String guildId, String rivalId, Connection connection) {
        String warId = ServiceFactory.createRandomUUID();

        final String matchMakeSql = "insert into War (warId, squadIdA, squadIdB, " +
                "prepGraceStartTime, prepEndTime, actionGraceStartTime, actionEndTime, cooldownEndTime) values " +
                "(?,?,?,?,?,?,?,?)";

        Long warMatchedTime = ServiceFactory.getSystemTimeSecondsFromEpoch();
        try {
            try (PreparedStatement stmt = connection.prepareStatement(matchMakeSql)) {
                stmt.setString(1, warId);
                stmt.setString(2, guildId);
                stmt.setString(3, rivalId);

                // preparation start time - the end of preparation for base
                // preparation end time (server base prep time)
                // war start time (2 mins before start)
                // war end time (1 hour war)
                // war result prep (2 mins)
                Config config = ServiceFactory.instance().getConfig();
                stmt.setLong(4, warMatchedTime += config.warPlayerPreparationDuration);
                stmt.setLong(5, warMatchedTime += config.warServerPreparationDuration);
                stmt.setLong(6, warMatchedTime += config.warPlayDuration);
                stmt.setLong(7, warMatchedTime += config.warResultDuration);
                stmt.setLong(8, warMatchedTime + config.warCoolDownDuration);
                stmt.executeUpdate();
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create War for squad id=" + guildId, ex);
        }

        return warId;
    }

    @Override
    public War getWar(String warId) {
        War war;
        try (Connection connection = getConnection()) {
            war = loadWar(warId, connection);
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load war id=" + warId, ex);
        }

        return war;
    }

    private War loadWar(String warId, Connection connection) {
        final String matchMakeSql = "select warId, squadIdA, squadIdB, prepGraceStartTime, prepEndTime, " +
                "actionGraceStartTime, actionEndTime, cooldownEndTime, processedEndTime, squadAScore, squadBScore " +
                "from War w where w.warId = ?";

        War war = null;
        try {
            try (PreparedStatement stmt = connection.prepareStatement(matchMakeSql)) {
                stmt.setString(1, warId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String squadIdA = rs.getString("squadIdA");
                    String squadIdB = rs.getString("squadIdB");
                    Long prepGraceStartTime = rs.getLong("prepGraceStartTime");
                    Long prepEndTime = rs.getLong("prepEndTime");
                    Long actionGraceStartTime = rs.getLong("actionGraceStartTime");
                    Long actionEndTime = rs.getLong("actionEndTime");
                    Long cooldownEndTime = rs.getLong("cooldownEndTime");
                    long processedEndTime = rs.getLong("processedEndTime");
                    int squadAScore = rs.getInt("squadAScore");
                    int squadBScore = rs.getInt("squadBScore");
                    war = new War(warId, squadIdA, squadIdB, prepGraceStartTime, prepEndTime,
                            actionGraceStartTime, actionEndTime, cooldownEndTime,
                            processedEndTime, squadAScore, squadBScore);
                }
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load war id=" + warId, ex);
        }

        return war;
    }

    @Override
    public SquadMemberWarData loadPlayerWarData(String warId, String playerId) {
        SquadMemberWarData squadMemberWarData;
        try (Connection connection = getConnection()) {
            squadMemberWarData = loadPlayerWarData(warId, playerId, connection);
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load players war data for id=" + playerId, ex);
        }

        return squadMemberWarData;
    }

    private SquadMemberWarData loadPlayerWarData(String warId, String playerId, Connection connection) {
        final String warParticipantsSql = "select s.warMap, s.donatedTroops, s.victoryPoints, s.turns, s.attacksWon, " +
                "s.defensesWon, s.score, s.defenseExpirationDate, p.hqLevel, p.id, p.name " +
                "from WarParticipants s, PlayerSettings p where s.playerId = ? and s.warId = ? and s.playerId = p.id";

        SquadMemberWarData squadMemberWarData = null;
        try {
            try (PreparedStatement stmt = connection.prepareStatement(warParticipantsSql)) {
                stmt.setString(1, playerId);
                stmt.setString(2, warId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    squadMemberWarData = mapSquadMemberWarData(warId, rs);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load WarParticipants for player id=" + playerId, ex);
        }

        return squadMemberWarData;
    }

    private SquadMemberWarData mapSquadMemberWarData(String warId, ResultSet rs) throws Exception {
        SquadMemberWarData squadMemberWarData = new SquadMemberWarData();
        squadMemberWarData.id = rs.getString("id");
        squadMemberWarData.warId = warId;
        squadMemberWarData.name = rs.getString("name");
        squadMemberWarData.victoryPoints = rs.getInt("victoryPoints");
        squadMemberWarData.turns = rs.getInt("turns");
        squadMemberWarData.attacksWon = rs.getInt("attacksWon");
        squadMemberWarData.defensesWon = rs.getInt("defensesWon");
        squadMemberWarData.score = rs.getInt("score");
        squadMemberWarData.level = rs.getInt("hqLevel");
        squadMemberWarData.defenseExpirationDate = rs.getLong("defenseExpirationDate");

        String warMap = rs.getString("warMap");
        if (warMap != null) {
            squadMemberWarData.warMap = ServiceFactory.instance().getJsonParser()
                    .fromJsonString(warMap, PlayerMap.class);
        }

        String donatedTroops = rs.getString("donatedTroops");
        if (donatedTroops != null) {
            squadMemberWarData.donatedTroops = ServiceFactory.instance().getJsonParser()
                    .fromJsonString(donatedTroops, DonatedTroops.class);
        }

        return squadMemberWarData;
    }

    @Override
    public List<SquadMemberWarData> getWarParticipants(String guildId, String warId) {
        List<SquadMemberWarData> squadMemberWarDatums;
        try (Connection connection = getConnection()) {
            squadMemberWarDatums = getWarParticipants(warId, guildId, connection);
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load war participants for guild id=" + guildId, ex);
        }

        return squadMemberWarDatums;
    }

    private List<SquadMemberWarData> getWarParticipants(String warId, String guildId, Connection connection) {
        List<SquadMemberWarData> participants = new ArrayList<>();

        final String warParticipantsSql = "select p.warMap, p.donatedTroops, p.victoryPoints, p.turns, p.attacksWon, " +
                "p.defensesWon, p.score, p.defenseExpirationDate, ps.hqLevel, ps.id, ps.name " +
                "from WarParticipants p, Squads s, SquadMembers m, PlayerSettings ps where s.id = ? and " +
                "m.guildId = s.id and m.playerId = p.playerId and p.warId = ? and ps.id = p.playerId";

        try {
            try (PreparedStatement stmt = connection.prepareStatement(warParticipantsSql)) {
                stmt.setString(1, guildId);
                stmt.setString(2, warId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    SquadMemberWarData squadMemberWarData = mapSquadMemberWarData(warId, rs);
                    participants.add(squadMemberWarData);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load WarParticipants for squad id=" + guildId, ex);
        }

        return participants;
    }

    @Override
    public void saveWarParticipant(SquadMemberWarData squadMemberWarData) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            saveWarParticipant(squadMemberWarData, connection);
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save war SquadMemberWarData for player id=" + squadMemberWarData.id, ex);
        }
    }

    @Override
    public void saveWarParticipant(GuildSession guildSession, PlayerSession playerSession, SquadMemberWarData squadMemberWarData,
                                   SquadNotification squadNotification) {
//        try (Connection connection = getConnection()) {
//            connection.setAutoCommit(false);
//            savePlayerSession(playerSession, connection);
//            saveWarParticipant(squadMemberWarData, connection);
//            setAndSaveGuildNotification(guildSession, squadNotification, connection);
//            connection.commit();
//        } catch (SQLException ex) {
//            throw new RuntimeException("Failed to save war SquadMemberWarData for player id=" + squadMemberWarData.id, ex);
//        }
    }

    private void saveWarParticipant(SquadMemberWarData squadMemberWarData, Connection connection) {
        final String warParticipantsSql = "update WarParticipants " +
                "set warMap = ?," +
                "donatedTroops = ?," +
                "victoryPoints = ?," +
                "turns = ?," +
                "attacksWon = ?," +
                "defensesWon = ?," +
                "score = ? " +
                "where playerId = ? and warId = ?";

        try {
            try (PreparedStatement stmt = connection.prepareStatement(warParticipantsSql)) {
                String warMapJson = null;
                if (squadMemberWarData.warMap != null)
                    warMapJson = ServiceFactory.instance().getJsonParser().toJson(squadMemberWarData.warMap);

                String donatedTroopsJson = null;
                if (squadMemberWarData.donatedTroops != null)
                    donatedTroopsJson = ServiceFactory.instance().getJsonParser().toJson(squadMemberWarData.donatedTroops);

                stmt.setString(1, warMapJson);
                stmt.setString(2, donatedTroopsJson);
                stmt.setInt(3, squadMemberWarData.victoryPoints);
                stmt.setInt(4, squadMemberWarData.turns);
                stmt.setInt(5, squadMemberWarData.attacksWon);
                stmt.setInt(6, squadMemberWarData.defensesWon);
                stmt.setInt(7, squadMemberWarData.score);
                stmt.setString(8, squadMemberWarData.id);
                stmt.setString(9, squadMemberWarData.warId);
                stmt.executeUpdate();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to save WarParticipants for player id=" + squadMemberWarData.id, ex);
        }
    }

    @Override
    public AttackDetail warAttackStart(WarSession warSession, String playerId, String opponentId,
                                       SquadNotification attackStartNotification, long time) {
        AttackDetail attackDetail;
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            attackDetail = checkAndCreateWarBattleId(warSession.getWarId(), playerId, opponentId, time, connection);
            if (attackDetail.getBattleId() != null) {
                WarNotificationData warNotificationData = (WarNotificationData) attackStartNotification.getData();
                warNotificationData.setAttackExpirationDate(attackDetail.getExpirationDate());
                setAndSaveWarNotification(attackDetail, warSession, attackStartNotification, connection);
            }

            if (attackDetail.getReturnCode() == ResponseHelper.RECEIPT_STATUS_COMPLETE)
                connection.commit();
            else
                connection.rollback();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save war WarBattleId for player id=" + playerId, ex);
        }

        return attackDetail;
    }

    @Override
    public AttackDetail warAttackComplete(WarSession warSession, PlayerSession playerSession,
                                          BattleReplay battleReplay,
                                          SquadNotification attackCompleteNotification,
                                          SquadNotification attackReplayNotification,
                                          DefendingWarParticipant defendingWarParticipant, long time) {
        AttackDetail attackDetail = null;
//        try (Connection connection = getConnection()) {
//            connection.setAutoCommit(false);
//
//            // calculate how many stars earned in the attack
//            int victoryPointsRemaining = defendingWarParticipant.getVictoryPoints();
//            int victoryPointsEarned = (3 - victoryPointsRemaining) - battleReplay.battleLog.stars;
//            if (victoryPointsEarned < 0)
//                victoryPointsEarned = Math.abs(victoryPointsEarned);
//            else
//                victoryPointsEarned = 0;
//
//            attackDetail = saveAndUpdateWarBattle(warSession.getWarId(), playerSession, battleReplay,
//                    victoryPointsEarned, connection);
//
//            if (attackDetail.getReturnCode() == ResponseHelper.RECEIPT_STATUS_COMPLETE) {
//                savePlayerSession(playerSession, connection);
//                WarNotificationData warNotificationData = (WarNotificationData) attackCompleteNotification.getData();
//                warNotificationData.setStars(battleReplay.battleLog.stars);
//                warNotificationData.setVictoryPoints(victoryPointsEarned);
//                setAndSaveWarNotification(attackDetail, warSession, attackCompleteNotification, connection);
//                setAndSaveWarNotification(attackDetail, warSession, attackReplayNotification, connection);
//                saveWarBattle(battleReplay, warSession.getWarId(), time, connection);
//                connection.commit();
//            } else {
//                connection.rollback();
//            }
//        } catch (SQLException ex) {
//            throw new RuntimeException("Failed to save war attack complete for player id=" + playerSession.getPlayerId(), ex);
//        }

        return attackDetail;
    }


    private void saveWarBattle(BattleReplay battleReplay, String warId, long time, Connection connection) {
        final String squadMemberSql = "insert into WarBattles (warId, battleId, attackerId, defenderId, battleResponse, battleCompleteTime) values " +
                "(?, ?, ?, ?, ?, ?)";
        saveBattle(connection, battleReplay.battleLog.battleId, BattleType.PvpAttackSquadWar, battleReplay.battleLog.attacker.playerId, battleReplay.battleLog.defender.playerId);

        try {
            try (PreparedStatement stmt = connection.prepareStatement(squadMemberSql)) {
                stmt.setString(1, warId);
                stmt.setString(2, battleReplay.battleLog.battleId);
                stmt.setString(3, battleReplay.battleLog.attacker.playerId);
                stmt.setString(4, battleReplay.battleLog.defender.playerId);
                String responseJson = ServiceFactory.instance().getJsonParser().toJson(battleReplay);
                stmt.setString(5, responseJson);
                stmt.setLong(6, time);
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save war battle for battleId=" + battleReplay.battleLog.battleId, ex);
        }
    }

    @Override
    public DefendingWarParticipant getDefendingWarParticipantByBattleId(String battleId) {
        DefendingWarParticipant defendingWarParticipant;
        try (Connection connection = getConnection()) {
            defendingWarParticipant = getDefendingWarParticipantByBattleId(battleId, connection);
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to get defending war participant by battleId=" + battleId, ex);
        }

        return defendingWarParticipant;
    }

    @Override
    public WarBattle getWarBattle(String battleId) {
        WarBattle warBattle;
        try (Connection connection = getConnection()) {
            warBattle = getWarBattle(battleId, connection);
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to get war battle by battleId=" + battleId, ex);
        }

        return warBattle;
    }

    private WarBattle getWarBattle(String battleId, Connection connection) {
        final String warBattlesSql = "select warId, battleId, attackerId, defenderId, battleResponse, battleCompleteTime " +
                "from WarBattles w " +
                "where w.battleId = ?";

        WarBattle warBattle = null;

        try {
            try (PreparedStatement stmt = connection.prepareStatement(warBattlesSql)) {
                stmt.setString(1, battleId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String warId = rs.getString("warId");
                        String attackerId = rs.getString("attackerId");
                        String defenderId = rs.getString("defenderId");
                        String battleResponseJson = rs.getString("battleResponse");
                        BattleReplay battleReplay = ServiceFactory.instance().getJsonParser()
                                .fromJsonString(battleResponseJson, BattleReplay.class);

                        long battleCompleteTime = rs.getLong("battleCompleteTime");
                        warBattle = new WarBattle(battleId, attackerId, defenderId, battleReplay, battleCompleteTime);
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to getDefendingWarParticipantByBattleId for player id=" + battleId, ex);
        }

        return warBattle;
    }

    private DefendingWarParticipant getDefendingWarParticipantByBattleId(String battleId, Connection connection) {
        final String warParticipantsDefenseSql = "select playerId, victoryPoints " +
                "from WarParticipants w " +
                "where w.defenseBattleId = ?";

        DefendingWarParticipant defendingWarParticipant;

        try {
            String opponentId = null;
            int victoryPoints = 0;
            try (PreparedStatement stmt = connection.prepareStatement(warParticipantsDefenseSql)) {
                stmt.setString(1, battleId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        opponentId = rs.getString("playerId");
                        victoryPoints = rs.getInt("victoryPoints");
                    }
                }
            }

            if (opponentId == null)
                throw new RuntimeException("unable to find battleId in WarParticipants " + battleId);

            defendingWarParticipant = new DefendingWarParticipant(opponentId, victoryPoints);

        } catch (Exception ex) {
            throw new RuntimeException("Failed to getDefendingWarParticipantByBattleId for player id=" + battleId, ex);
        }

        return defendingWarParticipant;
    }

    private AttackDetail saveAndUpdateWarBattle(String warId, PlayerSession playerSession,
                                                BattleReplay battleReplay,
                                                int victoryPointsEarned, Connection connection) {
        final String warParticipantsDefenseSql = "update WarParticipants " +
                "set defenseBattleId = null, defenseExpirationDate = null, victoryPoints = victoryPoints - ? " +
                "where warId = ? and defenseBattleId = ?";

        final String warParticipantsAttackSql = "update WarParticipants " +
                "set attackBattleId = null, attackExpirationDate = null, score = score + ? " +
                "where warId = ? and attackBattleId = ?";

        AttackDetail attackDetail;
        try {
            boolean updated = false;

            try (PreparedStatement stmt = connection.prepareStatement(warParticipantsDefenseSql)) {
                stmt.setInt(1, victoryPointsEarned);
                stmt.setString(2, warId);
                stmt.setString(3, battleReplay.battleLog.battleId);
                if (stmt.executeUpdate() == 1)
                    updated = true;
            }

            if (updated) {
                updated = false;
                try (PreparedStatement stmt = connection.prepareStatement(warParticipantsAttackSql)) {
                    stmt.setInt(1, victoryPointsEarned);
                    stmt.setString(2, warId);
                    stmt.setString(3, battleReplay.battleLog.battleId);
                    if (stmt.executeUpdate() == 1)
                        updated = true;
                }
            }

            int response = ResponseHelper.RECEIPT_STATUS_COMPLETE;

            // TODO - not sure what to send back yet
            if (!updated)
                response = ResponseHelper.STATUS_CODE_NOT_MODIFIED;

            attackDetail = new AttackDetail(response);

        } catch (Exception ex) {
            throw new RuntimeException("Failed to saveAndUpdateWarBattle for player id=" + playerSession.getPlayerId(), ex);
        }

        return attackDetail;
    }

    private void setAndSaveWarNotification(WarNotification warNotification, WarSession warSession,
                                           SquadNotification squadNotification, Connection connection) {
        synchronized (warSession) {
            GuildSession guildSessionA = warSession.getGuildASession();
            squadNotification.setDate(0);
            setAndSaveGuildNotification(guildSessionA, squadNotification, connection);
            warNotification.setGuildANotificationDate(squadNotification.getDate());
            GuildSession guildSessionB = warSession.getGuildBSession();
            squadNotification.setDate(0);
            setAndSaveGuildNotification(guildSessionB, squadNotification, connection);
            warNotification.setGuildBNotificationDate(squadNotification.getDate());
        }
    }

    private void setAndSaveGuildNotification(GuildSession guildSession, SquadNotification squadNotification, Connection connection) {
        synchronized (guildSession) {
            if (squadNotification.getDate() == 0)
                squadNotification.setDate(ServiceFactory.getSystemTimeSecondsFromEpoch());
            String guildId = guildSession.getGuildId();
            saveNotification(guildId, squadNotification, connection);
        }
    }

    private AttackDetail checkAndCreateWarBattleId(String warId, String playerId, String opponentId, long time, Connection connection) {

        final String warParticipantsDefenderSql = "update WarParticipants " +
                "set defenseBattleId = ?, defenseExpirationDate = ? " +
                "where warId = ? and playerId = ? and victoryPoints > 0 and " +
                "(defenseBattleId is null or ? > defenseExpirationDate + ?) " +
                "returning defenseBattleId, defenseExpirationDate";

        final String warParticipantsAttackerSql = "update WarParticipants " +
                "set turns = turns - 1, attackBattleId = ?, attackExpirationDate = ? " +
                "where warId = ? and playerId = ? and turns > 0";

        final String warDefenseSql = "select victoryPoints, defenseExpirationDate " +
                "from WarParticipants w " +
                "where w.warId = ? and w.playerId = ?";

        AttackDetail attackDetail = null;

        try {
            String defenseBattleId = ServiceFactory.createRandomUUID();
            long defenseExpirationDate = ServiceFactory.getSystemTimeSecondsFromEpoch() +
                    ServiceFactory.instance().getConfig().attackDuration;
            try (PreparedStatement stmt = connection.prepareStatement(warParticipantsDefenderSql)) {
                stmt.setString(1, defenseBattleId);
                stmt.setLong(2, defenseExpirationDate);
                stmt.setString(3, warId);
                stmt.setString(4, opponentId);
                stmt.setLong(5, time);
                stmt.setLong(6, 10);                        // 10 extra seconds before reopening up base
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String battleId = rs.getString("defenseBattleId");
                        defenseExpirationDate = rs.getLong("defenseExpirationDate");
                        attackDetail = new AttackDetail(battleId, defenseExpirationDate);
                    }
                }
            }

            if (attackDetail != null) {
                try (PreparedStatement stmt = connection.prepareStatement(warParticipantsAttackerSql)) {
                    stmt.setString(1, defenseBattleId);
                    stmt.setLong(2, defenseExpirationDate);
                    stmt.setString(3, warId);
                    stmt.setString(4, playerId);
                    int updatedRows = stmt.executeUpdate();
                    if (updatedRows != 1) {
                        attackDetail = new AttackDetail(ResponseHelper.STATUS_CODE_GUILD_WAR_NOT_ENOUGH_TURNS);
                    }
                }
            } else {
                // no battle id generated means it is getting attacked by someone already
                int victoryPoints = -1;

                try (PreparedStatement stmt = connection.prepareStatement(warDefenseSql)) {
                    stmt.setString(1, warId);
                    stmt.setString(2, opponentId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            victoryPoints = rs.getInt("victoryPoints");
                            defenseExpirationDate = rs.getLong("defenseExpirationDate");
                        }
                    }
                }

                // base has been cleared
                if (victoryPoints == 0) {
                    attackDetail = new AttackDetail(ResponseHelper.STATUS_CODE_GUILD_WAR_NOT_ENOUGH_VICTORY_POINTS);
                } else if (time > defenseExpirationDate) {
                    // TODO - give a grace time on when to decide to unlock this base as possible client crash
                    System.out.println("WarBase is still being attacked but expiry time has finished, maybe client crashed?");
                    attackDetail = new AttackDetail(ResponseHelper.STATUS_CODE_GUILD_WAR_BASE_UNDER_ATTACK);
                } else {
                    attackDetail = new AttackDetail(ResponseHelper.STATUS_CODE_GUILD_WAR_BASE_UNDER_ATTACK);
                }
            }

        } catch (Exception ex) {
            throw new RuntimeException("Failed to checkAndCreateWarBattleId for player id=" + playerId, ex);
        }

        return attackDetail;
    }

    @Override
    public void deleteWarForSquads(War war) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            deleteWarForSquads(war, connection);
            connection.commit();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to remove squads from warId=" + war.getWarId(), ex);
        }
    }

    private void deleteWarForSquads(War war, Connection connection) throws Exception {
        final String squadsSql = "update Squads " +
                "set warId = null, warSignUpTime = null " +
                "where warId = ?";

        try (PreparedStatement stmt = connection.prepareStatement(squadsSql)) {
            stmt.setString(1, war.getWarId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void saveWar(War war) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            saveWar(war, connection);
            connection.commit();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to save war from warId=" + war.getWarId(), ex);
        }
    }

    private void saveWar(War war, Connection connection) throws Exception {
        final String squadsSql = "update War " +
                "set prepGraceStartTime = ?, prepEndTime = ?, actionGraceStartTime = ?, actionEndTime = ?, cooldownEndTime = ?, processedEndTime = ? " +
                "where warId = ?";

        try (PreparedStatement stmt = connection.prepareStatement(squadsSql)) {
            stmt.setLong(1, war.getPrepGraceStartTime());
            stmt.setLong(2, war.getPrepEndTime());
            stmt.setLong(3, war.getActionGraceStartTime());
            stmt.setLong(4, war.getActionEndTime());
            stmt.setLong(5, war.getCooldownEndTime());
            stmt.setLong(6, war.getProcessedEndTime());
            stmt.setString(7, war.getWarId());
            stmt.executeUpdate();
        }
    }

    @Override
    public Collection<SquadNotification> getSquadNotificationsSince(String guildId, String guildName, long since) {
        try (Connection connection = getConnection()) {
            return getSquadNotificationsSince(guildId, guildName, since, connection);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load squad notifications since for guildId=" + guildId, ex);
        }
    }

    private Collection<SquadNotification> getSquadNotificationsSince(String guildId, String guildName, long since,
                                                                     Connection connection) throws Exception {
        final String notificationsSql = "SELECT id, orderNo, date, playerId, name, squadMessageType, message, squadNotification " +
                "FROM SquadNotifications s WHERE s.guildId = ? and s.date > ? order by s.date";

        List<SquadNotification> notifications = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(notificationsSql)) {
            pstmt.setString(1, guildId);
            pstmt.setLong(2, since);
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
                        new SquadNotification(guildId, guildName, date, orderNo, id,
                                message, name, playerId, squadMessageType, data);
                notifications.add(squadNotification);
            }
        }

        return notifications;
    }

    @Override
    public WarNotification warPrepared(WarSessionImpl warSession, String warId, SquadNotification warPreparedNotification) {
        WarNotification warNotification = new WarNotification();
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            setAndSaveWarNotification(warNotification, warSession, warPreparedNotification, connection);
            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save war notification for war id=" + warId, ex);
        }

        return warNotification;
    }

    @Override

    public HashMap<String, PvpMatch> getDevBaseMatches(PlayerSession playerSession) {
        String sql = "SELECT id, buildings, buildings, hqlevel, xp FROM DevBases WHERE ( hqlevel >= ? -1 AND hqlevel <= ? +1) or (xp >= ( ? * 0.9) AND xp <= (? * 1.10) ) ORDER BY xp desc";

        HashMap<String, PvpMatch> pvpMatches = new HashMap<>();

        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            int hq = playerSession.getHeadQuarter().getBuildingData().getLevel();
            int xp = playerSession.getPlayerSettings().getScalars().xp;
            preparedStatement.setInt(1, hq);
            preparedStatement.setInt(2, hq);
            preparedStatement.setInt(3, xp);
            preparedStatement.setInt(4, xp);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
//                BattleParticipant defender = new BattleParticipant();
//                defender.attackRating = 500;
//                defender.defenseRating = 500;
//                defender.playerId = rs.getString("id");
//                defender.name = "Dev Base";// TODO add randomised names...
//                defender.attackRatingDelta = 18;
//                defender.defenseRatingDelta = 18;
//                defender.faction = playerSession.getFaction().equals(FactionType.empire) ? FactionType.rebel : FactionType.empire;
//                defender.guildId = rs.getString("id");
//                defender.guildName = "DEV BASE";
//                defender.tournamentRating = 0;
//                defender.tournamentRatingDelta = 0;
                String battleId = ServiceFactory.createRandomUUID();
                PvpMatch pvpMatch = new PvpMatch();
                pvpMatch.setPlayerId(playerSession.getPlayerId());
                pvpMatch.setParticipantId(rs.getString("id"));
                pvpMatch.setDefenderXp(rs.getInt("xp"));
                pvpMatch.setBattleId(battleId);
                pvpMatch.setFactionType(playerSession.getFaction().equals(FactionType.empire) ? FactionType.rebel : FactionType.empire);
                pvpMatch.setDevBase(true);
                pvpMatch.setLevel(rs.getInt("hqlevel"));
                pvpMatches.put(battleId, pvpMatch);
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        return pvpMatches;
    }


    @Override
    public Buildings getDevBaseMap(String id, FactionType faction) {
        Buildings buildings = null;
        String sql = "SELECT buildings FROM DevBases WHERE id = ?";

        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, id);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                String mapString = rs.getString("buildings");
                mapString = mapString.replace(FactionType.neutral.name(), faction.name());
                try {
                    buildings = ServiceFactory.instance().getJsonParser().fromJsonString(mapString, Buildings.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
        return buildings;
    }


    @Override
    public void saveNewPvPBattle(PlayerPvpBattleComplete pvpBattle, PvpMatch match, BattleLog battleLog) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            //            saveBattle(connection, pvpBattle.getBattleId(), match);
            saveBattle(connection, match.getBattleId(), BattleType.Pvp, match.getPlayerId(), match.getParticipantId());
            saveBattleBaseData(connection, pvpBattle, match, battleLog);
            saveReplayData(connection, pvpBattle);
            connection.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveBattle(Connection connection, String battleId, BattleType battleType, String playerId, String participantId) {
        String insertNewMasterBattle = "insert into BattlesMaster (battleId, battleType, playerId, participantId)" +
                "values (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertNewMasterBattle)) {
            preparedStatement.setString(1, battleId);
            preparedStatement.setString(2, battleType.name());
            preparedStatement.setString(3, playerId);
            preparedStatement.setString(4, participantId);
            preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Error saving master battle data", ex);
        }
    }


    private void saveReplayData(Connection connection, PlayerPvpBattleComplete pvpBattle) {
        String insertNewPvPBattleReplay = "insert into PvpBattles_ReplayData (BattleId, combatEncounter, battleActions, attackerDeploymentData,\n" + "                                   defenderDeploymentData, lootCreditsAvailable, lootMaterialsAvailable,\n" + "                                   lootContrabandAvailable, lootBuildingCreditsMap, lootBuildingMaterialsMap,\n" + "                                   lootBuildingContrabandMap, battleType, battleLength, lowFPS, lowFPSTime,\n" + "                                   battleVersion, planetId, manifestVersion, battleAttributes, victoryConditions,\n" + "                                   failureCondition, donatedTroops, donatedTroopsAttacker, champions, disabledBuildings,\n" + "                                   simSeedA, simSeedB, viewTimePreBattle, attackerCreatureTraps, defenderCreatureTraps)\n" + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertNewPvPBattleReplay)) {
            stmt.setString(1, pvpBattle.getBattleId());
            stmt.setString(2, ServiceFactory.instance().getJsonParser().toJson(pvpBattle.getReplayData().combatEncounter));
            stmt.setString(3, ServiceFactory.instance().getJsonParser().toJson(pvpBattle.getReplayData().battleActions));
            stmt.setString(4, ServiceFactory.instance().getJsonParser().toJson(pvpBattle.getReplayData().attackerDeploymentData));
            stmt.setString(5, ServiceFactory.instance().getJsonParser().toJson(pvpBattle.getReplayData().defenderDeploymentData));
            stmt.setLong(6, pvpBattle.getReplayData().lootCreditsAvailable);

            stmt.setLong(7, pvpBattle.getReplayData().lootMaterialsAvailable);
            stmt.setLong(8, pvpBattle.getReplayData().lootContrabandAvailable);
            stmt.setString(9, ServiceFactory.instance().getJsonParser().toJson(pvpBattle.getReplayData().lootBuildingCreditsMap));
            stmt.setString(10, ServiceFactory.instance().getJsonParser().toJson(pvpBattle.getReplayData().lootBuildingMaterialsMap));
            stmt.setString(11, ServiceFactory.instance().getJsonParser().toJson(pvpBattle.getReplayData().lootBuildingContrabandMap));

            stmt.setString(12, pvpBattle.getReplayData().battleType.name());
            stmt.setLong(13, pvpBattle.getReplayData().battleLength);
            stmt.setLong(14, pvpBattle.getReplayData().lowFPS);
            stmt.setLong(15, pvpBattle.getReplayData().lowFPSTime);
            stmt.setString(16, pvpBattle.getReplayData().battleVersion);

            stmt.setString(17, pvpBattle.getReplayData().planetId);
            stmt.setString(18, pvpBattle.getReplayData().manifestVersion);
            stmt.setString(19, ServiceFactory.instance().getJsonParser().toJson(pvpBattle.getReplayData().battleAttributes));
            stmt.setString(20, ServiceFactory.instance().getJsonParser().toJson(pvpBattle.getReplayData().victoryConditions));
            stmt.setString(21, pvpBattle.getReplayData().failureCondition);

            stmt.setString(22, ServiceFactory.instance().getJsonParser().toJson(pvpBattle.getReplayData().donatedTroops));
            stmt.setString(23, ServiceFactory.instance().getJsonParser().toJson(pvpBattle.getReplayData().donatedTroopsAttacker));
            stmt.setString(24, ServiceFactory.instance().getJsonParser().toJson(pvpBattle.getReplayData().champions));
            stmt.setString(25, ServiceFactory.instance().getJsonParser().toJson(pvpBattle.getReplayData().disabledBuildings));
            stmt.setLong(26, pvpBattle.getReplayData().simSeedA);

            stmt.setLong(27, pvpBattle.getReplayData().simSeedB);
            stmt.setDouble(28, pvpBattle.getReplayData().viewTimePreBattle);
            stmt.setString(29, ServiceFactory.instance().getJsonParser().toJson(pvpBattle.getReplayData().attackerCreatureTraps));
            stmt.setString(30, ServiceFactory.instance().getJsonParser().toJson(pvpBattle.getReplayData().defenderCreatureTraps));
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to add new battle Replay", ex);
        }
    }

    private void saveBattleBaseData(Connection connection, PlayerPvpBattleComplete pvpBattle, PvpMatch match, BattleLog battleLog) {
        String insertNewBattle = "insert into PvpBattleData (" +
                "battleId," +
                " cs, " +
                "_credits, " +
                "_materials, " +
                "_contraband, " +
                "_crystals," +
                "seededTroopsDeployed, " +
                "damagedBuildings, " +
                "unarmedTraps, " +
                "baseDamagePercent,  " +
                "stars, " +
                "isUserEnded, " +
                "planetId, " +
                "attackerId, " +
                "participantId, " +
                "battleDate," +
                "BattleLog," +
                "ReplayData) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(insertNewBattle);
            stmt.setString(1, pvpBattle.getBattleId());
            stmt.setLong(2, pvpBattle.getCs());
            stmt.setInt(3, pvpBattle.getCredits());
            stmt.setInt(4, pvpBattle.getMaterials());
            stmt.setInt(5, pvpBattle.getContraband());
            stmt.setInt(6, pvpBattle.getCrystals());
            stmt.setString(7, ServiceFactory.instance().getJsonParser().toJson(pvpBattle.getSeededTroopsDeployed()));
            stmt.setString(8, ServiceFactory.instance().getJsonParser().toJson(pvpBattle.getDamagedBuildings()));
            stmt.setString(9, ServiceFactory.instance().getJsonParser().toJson(pvpBattle.getUnarmedTraps()));
            stmt.setInt(10, pvpBattle.getBaseDamagePercent());
            stmt.setInt(11, pvpBattle.getStars());
            stmt.setBoolean(12, pvpBattle.isUserEnded());
            stmt.setString(13, pvpBattle.getPlanetId());
            stmt.setString(14, pvpBattle.getPlayerId());
            stmt.setString(15, match.getParticipantId());
            stmt.setLong(16, match.getBattleDate());
            stmt.setString(17, ServiceFactory.instance().getJsonParser().toJson(battleLog));
            stmt.setString(18, ServiceFactory.instance().getJsonParser().toJson(pvpBattle.getReplayData()));
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to add new battle data", ex);
        }
    }


    @Override
    public List<BattleLog> getPlayerBattleLogs(String playerId) {
        List<BattleLog> battleLogs = new ArrayList<>();
        String battleLogSql = "SELECT BattleLog " +
                "FROM PvpBattleData " +
                "WHERE attackerId = ? OR participantId = ? LIMIT 30";
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(battleLogSql);
            preparedStatement.setString(1, playerId);
            preparedStatement.setString(2, playerId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    BattleLog battleLog = ServiceFactory.instance().getJsonParser().fromJsonString(rs.getString("BattleLog"), BattleLog.class);

                    battleLogs.add(battleLog);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException("Error retrieving resultset for player battle logs");
            }


        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to get DB connection when retrieving playerbattlelogs");
        }

        return battleLogs;
    }

    @Override
    public BattleType getBattleType(String battleId) {
        String sql = "SELECT battleType FROM BattlesMaster WHERE battleId = ?";
        BattleType battleType = BattleType.Pvp;
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, battleId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    battleType = BattleType.valueOf(rs.getString("battleType"));
                }

            }

        } catch (SQLException ex) {
            throw new RuntimeException("Failed to get DB connection when retrieving battle type", ex);
        }
        return battleType;
    }


    @Override
    public BattleReplay pvpReplay(String battleId) {
        String sql = "SELECT BattleLog, ReplayData FRom PvpBattleData WHERE battleId = ?";
        BattleReplay battleReplay = new BattleReplay();
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, battleId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    battleReplay.battleLog = ServiceFactory.instance().getJsonParser().fromJsonString(rs.getString("BattleLog"), BattleEntry.class);
                    battleReplay.replayData = ServiceFactory.instance().getJsonParser().fromJsonString(rs.getString("ReplayData"), ReplayData.class);

                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to get DB connection when retrieving battle type", ex);
        }
        return battleReplay;
    }
    public War processWarEnd(String warId, String squadIdA, String squadIdB) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            checkAndProcessWarEnd(warId, squadIdA, squadIdB, connection);
            connection.commit();
            return this.loadWar(warId, connection);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void checkAndProcessWarEnd(String warId, String squadIdA, String squadIdB, Connection connection) throws Exception
    {
        final String squadsSql = "update War " +
                "set processedEndTime = ?, " +
                "squadAScore = (select sum(score) from WarParticipants w where w.warId = War.warId and w.squadId = War.squadIdA), " +
                "squadBScore = (select sum(score) from WarParticipants w where w.warId = War.warId and w.squadId = War.squadIdA) " +
                "where warId = ? and (processedEndTime is null or processedEndTime = 0)";

        long time = ServiceFactory.getSystemTimeSecondsFromEpoch();
        try (PreparedStatement stmt = connection.prepareStatement(squadsSql)) {
            stmt.setLong(1, time);
            stmt.setString(2, warId);
            int updated = stmt.executeUpdate();

            if (updated == 1) {
                clearWarParty(squadIdA, connection);
                clearWarParty(squadIdB, connection);
            }
        }
    }

    @Override
    public void resetWarPartyForParticipants(String warId) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            setWarPartySquadMembers(warId, connection);
            connection.commit();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void setWarPartySquadMembers(String warId, Connection connection) throws Exception
    {
        final String squadsSql = "update SquadMembers " +
                                 "set warParty = 1 " +
                                 "where exists (select 1 " +
                                                "from WarParticipants where SquadMembers.playerId = WarParticipants.playerId " +
                                                "and SquadMembers.guildId = WarParticipants.squadId and WarParticipants.warId = ?)";

        try (PreparedStatement stmt = connection.prepareStatement(squadsSql)) {
            stmt.setString(1, warId);
            int updated = stmt.executeUpdate();
        }
    }
}
