package swcnoops.server.datasource;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;
import org.mongojack.Aggregation;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonMongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import swcnoops.server.Config;
import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.guild.GuildHelper;
import swcnoops.server.commands.player.PlayerIdentitySwitch;
import swcnoops.server.game.PvpMatch;
import swcnoops.server.model.*;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.*;
import swcnoops.server.session.creature.CreatureManager;
import swcnoops.server.session.training.BuildUnits;
import swcnoops.server.session.training.DeployableQueue;
import swcnoops.server.session.training.TrainingManager;
import java.util.*;
import java.util.Date;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Indexes.compoundIndex;
import static com.mongodb.client.model.Indexes.descending;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.*;

public class PlayerDatasourceImpl implements PlayerDataSource {
    private static final Logger LOG = LoggerFactory.getLogger(PlayerDatasourceImpl.class);

    private MongoClient mongoClient;
    private JacksonMongoCollection<Player> playerCollection;
    private JacksonMongoCollection<SquadInfo> squadCollection;
    private JacksonMongoCollection<SquadNotification> squadNotificationCollection;
    private JacksonMongoCollection<DevBase> devBaseCollection;
    private JacksonMongoCollection<BattleReplay> battleReplayCollection;
    private JacksonMongoCollection<WarSignUp> warSignUpCollection;
    private JacksonMongoCollection<SquadWar> squadWarCollection;
    private JacksonMongoCollection<SquadMemberWarData> squadMemberWarDataCollection;
    private MongoDatabase database;

    public PlayerDatasourceImpl() {
    }

    @Override
    public void initOnStartup() {
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

        this.squadCollection = JacksonMongoCollection.builder()
                .build(this.mongoClient, "dev", "squad", SquadInfo.class, UuidRepresentation.STANDARD);

        this.squadCollection.createIndex(Indexes.ascending("squadMembers.playerId"), new IndexOptions().unique(true));
        this.squadCollection.createIndex(Indexes.ascending("faction"));
        this.squadCollection.createIndex(Indexes.text("name"));

        this.squadNotificationCollection = JacksonMongoCollection.builder()
                .build(this.mongoClient, "dev", "squadNotification", SquadNotification.class, UuidRepresentation.STANDARD);

        this.squadNotificationCollection.createIndex(compoundIndex(Indexes.ascending("guildId"),
                Indexes.ascending("playerId"), Indexes.ascending("type"), Indexes.descending("date")));

        this.devBaseCollection = JacksonMongoCollection.builder()
                .build(this.mongoClient, "dev", "devBase", DevBase.class, UuidRepresentation.STANDARD);

        this.devBaseCollection.createIndex(Indexes.text("fileName"));
        this.devBaseCollection.createIndex(Indexes.ascending("checksum"));
        this.devBaseCollection.createIndex(compoundIndex(Indexes.ascending("hq"), Indexes.ascending("xp")));

        this.battleReplayCollection = JacksonMongoCollection.builder()
                .build(this.mongoClient, "dev", "battleReplay", BattleReplay.class, UuidRepresentation.STANDARD);
        this.battleReplayCollection.createIndex(compoundIndex(Indexes.ascending("attackerId"),
                Indexes.descending("battleType"),
                Indexes.descending("attackDate")));
        this.battleReplayCollection.createIndex(compoundIndex(Indexes.ascending("defenderId"),
                Indexes.descending("battleType"),
                Indexes.descending("attackDate")));

        this.warSignUpCollection = JacksonMongoCollection.builder()
                .build(this.mongoClient, "dev", "warSignUp", WarSignUp.class, UuidRepresentation.STANDARD);
        this.warSignUpCollection.createIndex(Indexes.ascending("guildId"), new IndexOptions().unique(true));

        this.squadWarCollection = JacksonMongoCollection.builder()
                .build(this.mongoClient, "dev", "squadWar", SquadWar.class, UuidRepresentation.STANDARD);

        this.squadMemberWarDataCollection = JacksonMongoCollection.builder()
                .build(this.mongoClient, "dev", "squadMemberWarData", SquadMemberWarData.class, UuidRepresentation.STANDARD);

        this.squadMemberWarDataCollection.createIndex(compoundIndex(Indexes.ascending("id"),
                Indexes.ascending("guildId"),
                Indexes.ascending("warId")),
                new IndexOptions().unique(true));
    }

    @Override
    public Player loadPlayer(String playerId) {
        Player player = this.playerCollection.find(eq("_id", playerId)).first();
        SquadInfo squadInfo = this.squadCollection.find(eq("squadMembers.playerId", playerId))
                .projection(include("_id")).first();
        if (squadInfo != null)
            player.getPlayerSettings().setGuildId(squadInfo._id);
        else
            player.getPlayerSettings().setGuildId(null);
        return player;
    }

    @Override
    public void savePlayerName(PlayerSession playerSession, String playerName) {
        playerSession.getPlayer().setKeepAlive(ServiceFactory.getSystemTimeSecondsFromEpoch());
        Bson simpleUpdate = set("playerSettings.name", playerName);
        Bson simpleUpdateKeepAlive = set("keepAlive", playerSession.getPlayer().getKeepAlive());
        Bson combined = combine(simpleUpdate, simpleUpdateKeepAlive);
        UpdateResult result = this.playerCollection.updateOne(Filters.eq("_id", playerSession.getPlayerId()),
                combined);

        playerSession.getPlayer().getPlayerSettings().setName(playerName);
    }

    @Override
    public void savePlayerSession(PlayerSession playerSession) {
        try (ClientSession session = this.mongoClient.startSession()) {
            session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            savePlayerSettings(playerSession, session);
            session.commitTransaction();
        } catch (MongoCommandException e) {
            throw new RuntimeException("Failed to save player session " + playerSession.getPlayerId(), e);
        }
    }

    @Override
    public void savePlayerLogin(PlayerSession playerSession) {
        try (ClientSession session = this.mongoClient.startSession()) {
            session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            savePlayerSettings(playerSession, new Date(), session);
            session.commitTransaction();
        } catch (MongoCommandException e) {
            throw new RuntimeException("Failed to save player login " + playerSession.getPlayerId(), e);
        }
    }

    @Override
    public void savePlayerKeepAlive(PlayerSession playerSession) {
        try (ClientSession session = this.mongoClient.startSession()) {
            session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            savePlayerKeepAlive(session, playerSession);
            session.commitTransaction();
        } catch (MongoCommandException e) {
            throw new RuntimeException("Failed to save player keepAlive " + playerSession.getPlayerId(), e);
        }
    }

    private void savePlayerKeepAlive(ClientSession clientSession, PlayerSession playerSession) {
        playerSession.getPlayer().setKeepAlive(ServiceFactory.getSystemTimeSecondsFromEpoch());
        UpdateResult result = this.playerCollection.updateOne(clientSession, Filters.eq("_id", playerSession.getPlayerId()),
                set("keepAlive", playerSession.getPlayer().getKeepAlive()));
    }

    @Override
    public void recoverWithPlayerSettings(PlayerSession playerSession, PlayerModel playerModel, Map<String, String> sharedPrefs) {
        ServiceFactory.instance().getSessionManager().resetPlayerSettings(playerSession.getPlayerSettings());
        ServiceFactory.instance().getSessionManager().setFromModel(playerSession.getPlayerSettings(), playerModel);

        playerSession.getPlayer().setKeepAlive(ServiceFactory.getSystemTimeSecondsFromEpoch());
        playerSession.getPlayerSettings().getSharedPreferences().putAll(sharedPrefs);
        playerSession.getPlayer().getPlayerSecret().setMissingSecret(false);

        Bson simpleUpdate = set("playerSettings", playerSession.getPlayerSettings());
        Bson recoverUpdate = set("playerSecret.missingSecret", playerSession.getPlayer().getPlayerSecret().getMissingSecret());
        Bson keepAliveUpdate = set("keepAlive", playerSession.getPlayer().getKeepAlive());
        Bson combined = combine(recoverUpdate, simpleUpdate, keepAliveUpdate);
        UpdateResult result = this.playerCollection.updateOne(Filters.eq("_id", playerSession.getPlayerId()),
                combined);

        // reload and initialise
        Player player = this.loadPlayer(playerSession.getPlayerId());
        playerSession.initialise(player);
    }

    @Override
    public void joinSquad(GuildSession guildSession, PlayerSession playerSession, SquadNotification squadNotification) {
        try (ClientSession clientSession = this.mongoClient.startSession()) {
            clientSession.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            playerSession.setGuildSession(guildSession);
            savePlayerSettings(playerSession, clientSession);

            if (guildSession.canEdit()) {
                Member newMember = GuildHelper.createMember(playerSession);
                newMember.joinDate = ServiceFactory.getSystemTimeSecondsFromEpoch();
                // for some reason mongoJack did not like the push in a combine
                // we also use a combined query to only push if our playerId is not already there as the array index on
                // playerId only works across documents and not in the same document
                Bson combinedQuery = combine(eq("_id", guildSession.getGuildId()),
                        Filters.ne("squadMembers.playerId", newMember.playerId));
                UpdateResult result = this.squadCollection.updateOne(clientSession, combinedQuery,
                        DBUpdate.push("squadMembers", newMember).inc("members", 1));

                deleteJoinRequestNotifications(clientSession, squadNotification.getGuildId(), squadNotification.getPlayerId());
                setAndSaveGuildNotification(clientSession, guildSession, squadNotification);
            }

            clientSession.commitTransaction();
        } catch (MongoCommandException e) {
            throw new RuntimeException("Failed to join player to guild " + playerSession.getPlayerId(), e);
        }
    }

    @Override
    public void joinRequest(GuildSession guildSession, PlayerSession playerSession, SquadNotification squadNotification) {
        try (ClientSession clientSession = this.mongoClient.startSession()) {
            clientSession.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            savePlayerSettings(playerSession, clientSession);

            if (guildSession.canEdit()) {
                deleteJoinRequestNotifications(clientSession, squadNotification.getGuildId(), squadNotification.getPlayerId());
                setAndSaveGuildNotification(clientSession, guildSession, squadNotification);
            }
            clientSession.commitTransaction();
        } catch (MongoCommandException ex) {
            throw new RuntimeException("Failed to save player settings id=" + playerSession.getPlayerId(), ex);
        }
    }

    @Override
    public void joinRejected(GuildSession guildSession, PlayerSession playerSession, SquadNotification squadNotification) {
        try (ClientSession clientSession = this.mongoClient.startSession()) {
            clientSession.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());

            if (guildSession.canEdit()) {
                deleteJoinRequestNotifications(clientSession, squadNotification.getGuildId(), squadNotification.getPlayerId());
                setAndSaveGuildNotification(clientSession, guildSession, squadNotification);
            }
            clientSession.commitTransaction();
        } catch (MongoCommandException ex) {
            throw new RuntimeException("Failed to save player settings id=" + playerSession.getPlayerId(), ex);
        }
    }

    private void deleteJoinRequestNotifications(ClientSession clientSession, String guildId, String playerId) {
        Bson combine = combine(eq("guildId", guildId), eq("playerId", playerId), eq("type", SquadMsgType.joinRequest));
        DeleteResult deleteResult = this.squadNotificationCollection.deleteMany(clientSession, combine);
    }

    @Override
    public void leaveSquad(GuildSession guildSession, PlayerSession playerSession, SquadNotification squadNotification) {
        try (ClientSession session = this.mongoClient.startSession()) {
            session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            savePlayerSettings(playerSession, session);
            if (guildSession.canEdit()) {
                Bson combine = combine(pull("squadMembers", eq("playerId",playerSession.getPlayerId())),
                        inc("members", -1));
                UpdateResult result = this.squadCollection.updateOne(session,
                        combine(eq("_id", guildSession.getGuildId()), eq("squadMembers.playerId", playerSession.getPlayerId())),
                        combine);

                deleteNotifications(session, squadNotification.getGuildId(), playerSession.getPlayerId());
                setAndSaveGuildNotification(session, guildSession, squadNotification);
            }

            session.commitTransaction();
        } catch (MongoCommandException e) {
            throw new RuntimeException("Failed to join player to guild " + playerSession.getPlayerId(), e);
        }
    }

    private void deleteNotifications(ClientSession clientSession, String guildId, String playerId) {
        Bson combine = combine(eq("guildId", guildId), eq("playerId", playerId));
        DeleteResult deleteResult = this.squadNotificationCollection.deleteMany(clientSession, combine);
    }

    @Override
    public void changeSquadRole(GuildSession guildSession, PlayerSession invokerSession, PlayerSession playerSession, SquadNotification squadNotification,
                                SquadRole squadRole) {
        try (ClientSession clientSession = this.mongoClient.startSession()) {
            clientSession.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            this.savePlayerKeepAlive(clientSession, invokerSession);
            if (guildSession.canEdit()) {
                updateSquadMember(clientSession, guildSession.getGuildId(), playerSession.getPlayerId(),
                        squadRole == SquadRole.Officer);
                setAndSaveGuildNotification(clientSession, guildSession, squadNotification);
            }
            clientSession.commitTransaction();
        } catch (MongoCommandException ex) {
            throw new RuntimeException("Failed to save squad member role id=" + playerSession.getPlayerId(), ex);
        }
    }

    private void updateSquadMember(ClientSession clientSession, String guildId, String playerId, boolean isOfficer) {
        UpdateResult result = this.squadCollection.updateOne(clientSession,
                combine(eq("_id", guildId), eq("squadMembers.playerId", playerId)),
                set("squadMembers.$.isOfficer", isOfficer));
    }

    private void saveDonationRecipient(PlayerSession playerSession, ClientSession session) {
        // TODO - redo these to do straight through amendments to the settings
        mapDonatedTroopsToPlayerSession(playerSession);
        playerSession.getPlayer().setKeepAlive(ServiceFactory.getSystemTimeSecondsFromEpoch());
        UpdateResult result = this.playerCollection.updateOne(session, Filters.eq("_id", playerSession.getPlayerId()),
                combine(set("playerSettings.donatedTroops", playerSession.getPlayer().getPlayerSettings().getDonatedTroops()),
                        set("keepAlive", playerSession.getPlayer().getKeepAlive())));
    }

    private void savePlayerSettings(PlayerSession playerSession, ClientSession session) {
        savePlayerSettings(playerSession, null, session);
    }

    // TODO - probably will need to change this to be smart and only update amended data
    private void savePlayerSettings(PlayerSession playerSession, Date loginDate, ClientSession session) {
        // TODO - redo these to do straight through amendments to the settings
        mapDeployablesToPlayerSettings(playerSession);
        playerSession.getPlayerSettings().setBuildContracts(mapContractsToPlayerSettings(playerSession));
        mapCreatureToPlayerSession(playerSession);
        mapDonatedTroopsToPlayerSession(playerSession);

        int oldHqLevel = playerSession.getPlayerSettings().getHqLevel();
        int oldXp = playerSession.getPlayerSettings().getScalars().xp;

        int hqLevel = playerSession.getHeadQuarter().getBuildingData().getLevel();
        int xp = ServiceFactory.getXpFromBuildings(playerSession.getPlayerMapItems().getBaseMap().buildings);
        playerSession.getPlayerSettings().setHqLevel(hqLevel);
        playerSession.getPlayerSettings().getScalars().xp = xp;

        // if there is a change then we need to tell the squad
        GuildSession guildSession = playerSession.getGuildSession();
        if (guildSession != null && (oldHqLevel != hqLevel || oldXp != xp)) {
            // TODO - do we need to send a squad notification otherwise how will the squad know
            // might be able to trick the client by sending a joinRequestRejected without data or something like that
            setSquadPlayerHQandXp(session, guildSession.getGuildId(), playerSession.getPlayerId(), hqLevel, xp);
            guildSession.getGuildSettings().membersUpdated();
        }

        playerSession.getPlayer().setKeepAlive(ServiceFactory.getSystemTimeSecondsFromEpoch());

        Bson combinedSet;
        if (loginDate != null) {
            playerSession.getPlayer().setLoginDate(loginDate);
            combinedSet = combine(set("playerSettings", playerSession.getPlayerSettings()),
                    set("loginDate", playerSession.getPlayer().getLoginDate()),
                    set("keepAlive", playerSession.getPlayer().getKeepAlive()));
        } else {
            combinedSet = combine(set("playerSettings", playerSession.getPlayerSettings()),
                    set("keepAlive", playerSession.getPlayer().getKeepAlive()));
        }

        UpdateResult result = this.playerCollection.updateOne(session, Filters.eq("_id", playerSession.getPlayerId()),
                combinedSet);
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
    public void saveTroopDonation(GuildSession guildSession, PlayerSession playerSession, PlayerSession recipientPlayerSession,
                                  SquadNotification squadNotification)
    {
        try (ClientSession clientSession = this.mongoClient.startSession()) {
            clientSession.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            savePlayerSettings(playerSession, clientSession);
            saveDonationRecipient(recipientPlayerSession, clientSession);
            setAndSaveGuildNotification(clientSession, guildSession, squadNotification);
            clientSession.commitTransaction();
        } catch (MongoCommandException ex) {
            throw new RuntimeException("Failed to save player settings id=" + playerSession.getPlayerId() +
                    " and id=" + recipientPlayerSession.getPlayerId(), ex);
        }
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
        Player player = new Player(playerId);
        player.setPlayerSecret(new PlayerSecret(secret, null, missingSecret));
        player.setPlayerSettings(new PlayerSettings());
        ServiceFactory.instance().getSessionManager().setFromModel(player.getPlayerSettings(), playerModel);
        if (sharedPrefs != null)
            player.getPlayerSettings().getSharedPreferences().putAll(sharedPrefs);
        player.getPlayerSettings().setName(name);

        try (ClientSession clientSession = this.mongoClient.startSession()) {
            clientSession.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            // creating a secondary account so we update the primary one
            if (playerId.endsWith("_1")) {
                String primaryAccount = PlayerIdentitySwitch.getPrimaryAccount(playerId);
                this.playerCollection.updateOne(clientSession, eq("_id", primaryAccount),
                        set("playerSecret.secondaryAccount", playerId));
            }
            this.playerCollection.insertOne(clientSession, player);
            clientSession.commitTransaction();
        }
    }

    @Override
    public void newGuild(PlayerSession playerSession, GuildSettings guildSettings) {
        createNewGuild(playerSession, guildSettings);
    }

    private void createNewGuild(PlayerSession playerSession, GuildSettings guildSettings) {
        SquadInfo squadInfo = new SquadInfo();
        squadInfo._id = guildSettings.getGuildId();
        squadInfo.name = guildSettings.getGuildName();
        squadInfo.openEnrollment = guildSettings.getOpenEnrollment();
        squadInfo.icon = guildSettings.getIcon();
        squadInfo.faction = guildSettings.getFaction();
        squadInfo.setDescription(guildSettings.getDescription());
        squadInfo.minScore = guildSettings.getMinScoreAtEnrollment();

        Member owner = GuildHelper.createMember(playerSession);
        owner.isOwner = true;
        owner.joinDate = ServiceFactory.getSystemTimeSecondsFromEpoch();
        squadInfo.getSquadMembers().add(owner);
        squadInfo.members = squadInfo.getSquadMembers().size();
        squadInfo.activeMemberCount = squadInfo.getSquadMembers().size();

        this.squadCollection.save(squadInfo);
    }

    @Override
    public GuildSettings loadGuildSettings(String guildId) {
        GuildSettingsImpl guildSettings = null;

        SquadInfo squadInfo = this.squadCollection.find(eq("_id", guildId)).first();

        if (squadInfo != null) {
            guildSettings = new GuildSettingsImpl(squadInfo._id);
            guildSettings.setName(squadInfo.name);
            guildSettings.setFaction(squadInfo.faction);
            guildSettings.setDescription(squadInfo.getDescription());
            guildSettings.setIcon(squadInfo.icon);
            guildSettings.setOpenEnrollment(squadInfo.openEnrollment);
            guildSettings.setMinScoreAtEnrollment(squadInfo.minScore);
            guildSettings.setWarSignUpTime(squadInfo.warSignUpTime);
            guildSettings.setWarId(squadInfo.warId);

            GuildMembers guildMembers = new GuildMembers(guildSettings.getGuildId(), squadInfo.getSquadMembers());
            guildSettings.setGuildMembers(guildMembers);
        }

        return guildSettings;
    }

    @Override
    public List<Member> loadSquadMembers(String guildId) {
        SquadInfo squadInfo = this.squadCollection.find(eq("_id", guildId)).projection(include("squadMembers")).first();
        if (squadInfo != null)
            return squadInfo.getSquadMembers();

        return new ArrayList<>();
    }

    @Override
    public List<WarHistory> loadWarHistory(String squadId) {
        // TODO - change to use aggregate to limit to the latest 20
        Bson squadInWar = or(eq("squadIdA", squadId), eq("squadIdB", squadId));
        FindIterable<SquadWar> squadWarIterable = this.squadWarCollection.find(and(squadInWar, gt("processedEndTime", 0)))
                .projection(include("warId", "squadIdA", "squadIdB", "processedEndTime", "squadAScore", "squadBScore",
                        "squadAWarSignUp.guildName", "squadAWarSignUp.icon", "squadBWarSignUp.guildName", "squadBWarSignUp.icon"));

        List<WarHistory> warHistories = new ArrayList<>();

        try (MongoCursor<SquadWar> cursor = squadWarIterable.cursor()) {
            while (cursor.hasNext()) {
                SquadWar squadWar = cursor.next();

                WarHistory warHistory = new WarHistory();
                warHistory.warId = squadWar.getWarId();
                warHistory.opponentGuildId = squadWar.getSquadIdB();
                warHistory.opponentName = squadWar.getSquadBWarSignUp().guildName;
                warHistory.opponentIcon = squadWar.getSquadBWarSignUp().icon;
                warHistory.opponentScore = squadWar.getSquadBScore();
                warHistory.endDate = squadWar.getProcessedEndTime();

                if (!warHistory.opponentGuildId.equals(squadId)) {
                    warHistory.score = squadWar.getSquadAScore();
                } else {
                    warHistory.opponentGuildId = squadWar.getSquadIdA();
                    warHistory.opponentName = squadWar.getSquadAWarSignUp().guildName;
                    warHistory.opponentIcon = squadWar.getSquadAWarSignUp().icon;
                    warHistory.opponentScore = squadWar.getSquadAScore();
                    warHistory.score = squadWar.getSquadBScore();
                }

                warHistories.add(warHistory);
            }
        }

        return warHistories;
    }


    @Override
    public void editGuild(String guildId, String description, String icon, Integer minScoreAtEnrollment,
                          boolean openEnrollment) {
        Bson setDescription = set("description", description);
        Bson setIcon = set("icon", icon);
        Bson setMinScoreAtEnrollment = set("minScore", minScoreAtEnrollment);
        Bson setOpenEnrollment = set("openEnrollment", openEnrollment);
        Bson combined = combine(setDescription, setIcon, setMinScoreAtEnrollment, setOpenEnrollment);
        UpdateResult result = this.squadCollection.updateOne(Filters.eq("_id", guildId),
                combined);
    }

    @Override
    public List<Squad> getGuildList(FactionType faction) {
        FindIterable<SquadInfo> squadInfos = this.squadCollection.find(eq("faction", faction))
                .projection(include("faction", "name", "description", "icon", "openEnrollment", "minScore", "members", "activeMemberCount"));

        List<Squad> squads = new ArrayList<>();
        if (squadInfos != null) {
            try (MongoCursor<SquadInfo> cursor = squadInfos.cursor()) {
                while (cursor.hasNext()) {
                    SquadInfo squadInfo = cursor.next();
                    squads.add(squadInfo);
                }
            }
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
        try (ClientSession clientSession = this.mongoClient.startSession()) {
            clientSession.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            setAndSaveGuildNotification(clientSession, guildSession, squadNotification);
            clientSession.commitTransaction();
        } catch (MongoCommandException e) {
            throw new RuntimeException("Failed to set and save notification for guild " + guildSession.getGuildId(), e);
        }
    }

    @Override
    public List<Squad> searchGuildByName(String searchTerm) {
        FindIterable<SquadInfo> squadInfos = this.squadCollection.find(regex("name", searchTerm, "i"))
                .projection(include("faction", "name", "description", "icon", "openEnrollment", "minScore", "members", "activeMemberCount"));

        List<Squad> squads = new ArrayList<>();
        if (squadInfos != null) {
            try (MongoCursor<SquadInfo> cursor = squadInfos.cursor()) {
                while (cursor.hasNext()) {
                    SquadInfo squadInfo = cursor.next();
                    squads.add(squadInfo);
                }
            }
        }

        return squads;
    }

    @Override
    public void saveWarSignUp(FactionType faction, GuildSession guildSession, List<String> participantIds,
                              boolean isSameFactionWarAllowed, SquadNotification squadNotification, long time) {
        try (ClientSession clientSession = this.mongoClient.startSession()) {
            clientSession.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            saveWarSignUp(clientSession, faction, guildSession, participantIds, isSameFactionWarAllowed, time);
            resetWarParty(clientSession, guildSession.getGuildId());
            setSquadWarParty(clientSession, guildSession.getGuildId(), participantIds, time);
            setAndSaveGuildNotification(clientSession, guildSession, squadNotification);
            clientSession.commitTransaction();
        } catch (MongoCommandException ex) {
            throw new RuntimeException("Failed to sign up for war " + guildSession.getGuildId(), ex);
        }
    }

    private void saveWarSignUp(ClientSession clientSession, FactionType faction, GuildSession guildSession, List<String> participantIds,
                               boolean isSameFactionWarAllowed, long time)
    {
        WarSignUp warSignUp = new WarSignUp();
        warSignUp.faction = faction;
        warSignUp.guildId = guildSession.getGuildId();
        warSignUp.guildName = guildSession.getGuildName();
        warSignUp.icon = guildSession.getGuildSettings().getIcon();
        warSignUp.participantIds = participantIds;
        warSignUp.isSameFactionWarAllowed = isSameFactionWarAllowed;
        warSignUp.time = time;
        warSignUp.signUpdate = new Date();
        this.warSignUpCollection.insertOne(clientSession, warSignUp);
    }

    private void setSquadPlayerHQandXp(ClientSession clientSession, String guildId, String playerId,
                                  int hqLevel, int xp)
    {
        UpdateResult result = this.squadCollection.updateOne(clientSession,
                and(eq("_id", guildId), Filters.eq("squadMembers.playerId", playerId)),
                combine(set("squadMembers.$.hqLevel", hqLevel), set("squadMembers.$.xp", xp)));
    }

    private void setSquadWarParty(ClientSession clientSession, String guildId, List<String> participantIds,
                                  long time)
    {
        UpdateResult result = this.squadCollection.updateOne(clientSession,
                and(eq("_id", guildId), Filters.in("squadMembers.playerId", participantIds)),
                combine(set("squadMembers.$.warParty", 1), set("warSignUpTime", time)));
    }

    private void resetWarParty(ClientSession clientSession, String guildId) {
        this.squadCollection.updateOne(clientSession, eq("_id", guildId),
                combine(set("squadMembers.$[].warParty", 0), unset("warSignUpTime"), unset("warId")));
    }

    private void clearWarParty(ClientSession clientSession, String warId, String guildId) {
        this.squadCollection.updateOne(clientSession, combine(eq("_id", guildId), eq("warId", warId)),
                combine(set("squadMembers.$[].warParty", 0), unset("warSignUpTime")));
    }

    @Override
    public void cancelWarSignUp(GuildSession guildSession, SquadNotification squadNotification) {
        try (ClientSession session = this.mongoClient.startSession()) {
            session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            deleteWarSignUp(session, guildSession.getGuildId());
            resetWarParty(session, guildSession.getGuildId());
            setAndSaveGuildNotification(session, guildSession, squadNotification);
            session.commitTransaction();
        }
    }

    private void deleteWarSignUp(ClientSession clientSession, String guildId) {
        this.warSignUpCollection.deleteOne(clientSession, eq("guildId", guildId));
    }

    @Override
    public String matchMake(String guildId) {
        String warId;
        try (ClientSession session = this.mongoClient.startSession()) {
            session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            warId = matchMake(session, guildId);
            session.commitTransaction();
        }

        return warId;
    }

    private String matchMake(ClientSession session, String guildId) {
        SquadWar squadWar = null;
        WarSignUp mySquad = this.warSignUpCollection.findOne(eq("guildId", guildId));
        if (mySquad != null) {
            List<Bson> aggregates = Arrays.asList(Aggregates.match(ne("guildId", guildId)),
                    Aggregates.sample(1));
            AggregateIterable<WarSignUp> otherSquadIterable = this.warSignUpCollection.aggregate(session, aggregates);
            if (otherSquadIterable.cursor().hasNext()) {
                WarSignUp opponent = otherSquadIterable.cursor().next();
                if (opponent != null) {
                    DeleteResult deleteResult = this.warSignUpCollection.deleteMany(session,
                            or(eq("guildId", guildId), eq("guildId", opponent.guildId)));

                    squadWar = this.createWar(session, mySquad, opponent);
                    this.setSquadWarId(session, mySquad.guildId, squadWar.getWarId());
                    this.setSquadWarId(session, opponent.guildId, squadWar.getWarId());
                    this.createWarParticipants(session, squadWar);
                }
            }
        }

        String warId = null;
        if (squadWar != null)
            warId = squadWar.getWarId();

        return warId;
    }

    private void setSquadWarId(ClientSession clientSession, String guildId, String warId) {
        UpdateResult result = this.squadCollection.updateOne(clientSession,
                eq("_id", guildId),
                set("warId", warId));
    }

    private void createWarParticipants(ClientSession session, SquadWar squadWar) {
        List<SquadMemberWarData> squadMembersA = createWarParticipants(squadWar.getWarId(), squadWar.getSquadAWarSignUp());
        List<SquadMemberWarData> squadMembersB = createWarParticipants(squadWar.getWarId(), squadWar.getSquadBWarSignUp());
        this.squadMemberWarDataCollection.insertMany(session, squadMembersA);
        this.squadMemberWarDataCollection.insertMany(session, squadMembersB);
    }

    private List<SquadMemberWarData> createWarParticipants(String warId, WarSignUp warSignUp) {
        List<SquadMemberWarData> squadMembers = new ArrayList<>();
        for (String playerId : warSignUp.participantIds) {
            if (!playerId.contains("BOT")) {
                Player player = this.playerCollection.find(eq("_id", playerId))
                        .projection(include("playerSettings.warMap", "playerSettings.baseMap",
                                "playerSettings.name", "playerSettings.hqLevel")).first();

                SquadMemberWarData squadMemberWarData = new SquadMemberWarData();
                squadMemberWarData.warId = warId;
                squadMemberWarData.id = playerId;
                squadMemberWarData.guildId = warSignUp.guildId;
                squadMemberWarData.turns = 3;
                squadMemberWarData.victoryPoints = 3;

                PlayerMap playerMap = player.getPlayerSettings().getWarMap();
                if (playerMap == null)
                    playerMap = player.getPlayerSettings().getBaseMap();

                squadMemberWarData.warMap = playerMap;
                squadMemberWarData.name = player.getPlayerSettings().getName();
                squadMemberWarData.level = player.getPlayerSettings().getHqLevel();
                squadMembers.add(squadMemberWarData);
            }
        }

        return squadMembers;
    }

    private SquadWar createWar(ClientSession session, WarSignUp mySquad, WarSignUp opponent) {
        long warMatchedTime = ServiceFactory.getSystemTimeSecondsFromEpoch();

        SquadWar squadWar = new SquadWar();
        squadWar.setWarId(ServiceFactory.createRandomUUID());
        squadWar.setSquadIdA(mySquad.guildId);
        squadWar.setSquadIdB(opponent.guildId);
        Config config = ServiceFactory.instance().getConfig();
        squadWar.warMatchedTime = warMatchedTime;
        squadWar.warMatchedDate = new Date();
        squadWar.setPrepGraceStartTime(warMatchedTime += config.warPlayerPreparationDuration);
        squadWar.setPrepEndTime(warMatchedTime += config.warServerPreparationDuration);
        squadWar.setActionGraceStartTime(warMatchedTime += config.warPlayDuration);
        squadWar.setActionEndTime(warMatchedTime += config.warResultDuration);
        squadWar.setCooldownEndTime(warMatchedTime + config.warCoolDownDuration);
        squadWar.setSquadAWarSignUp(mySquad);
        squadWar.setSquadBWarSignUp(opponent);

        this.squadWarCollection.insertOne(session, squadWar);
        return squadWar;
    }

    @Override
    public War getWar(String warId) {
        War war = this.squadWarCollection.findOne(eq("_id", warId));
        return war;
    }

    @Override
    public SquadMemberWarData loadPlayerWarData(String warId, String playerId) {
        SquadMemberWarData squadMemberWarData =
                this.squadMemberWarDataCollection.findOne(combine(eq("warId", warId),
                        eq("id", playerId)));

        return squadMemberWarData;
    }

    @Override
    public List<SquadMemberWarData> getWarParticipants(String guildId, String warId) {
        FindIterable<SquadMemberWarData> squadMemberWarDataFindIterable =
                this.squadMemberWarDataCollection.find(combine(eq("guildId", guildId), eq("warId", warId)));

        List<SquadMemberWarData> squadMemberWarDatums = new ArrayList<>();
        try (MongoCursor<SquadMemberWarData> cursor = squadMemberWarDataFindIterable.cursor()) {
            while (cursor.hasNext()) {
                squadMemberWarDatums.add(cursor.next());
            }
        }

        return squadMemberWarDatums;
    }

    @Override
    public void saveWarMap(PlayerSession playerSession, SquadMemberWarData squadMemberWarData) {
        try (ClientSession clientSession = this.mongoClient.startSession()) {
            clientSession.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            savePlayerSettings(playerSession, clientSession);
            SquadMemberWarData updatedData = this.squadMemberWarDataCollection.findOneAndUpdate(clientSession,
                    combine(eq("warId", squadMemberWarData.warId),
                            eq("id", squadMemberWarData.id)),
                    set("warMap", squadMemberWarData.warMap));
            clientSession.commitTransaction();
        }
    }

    @Override
    public void saveWarTroopDonation(GuildSession guildSession, PlayerSession playerSession, SquadMemberWarData squadMemberWarData,
                                     SquadNotification squadNotification) {
        try (ClientSession clientSession = this.mongoClient.startSession()) {
            clientSession.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            savePlayerSettings(playerSession, clientSession);
            saveWarTroopDonation(squadMemberWarData, clientSession);
            setAndSaveGuildNotification(clientSession, guildSession, squadNotification);
            clientSession.commitTransaction();
        }
    }

    private void saveWarTroopDonation(SquadMemberWarData squadMemberWarData, ClientSession clientSession) {
        SquadMemberWarData updatedData = this.squadMemberWarDataCollection.findOneAndUpdate(clientSession,
                combine(eq("warId", squadMemberWarData.warId),
                        eq("id", squadMemberWarData.id)),
                set("donatedTroops", squadMemberWarData.donatedTroops));
    }

    @Override
    public AttackDetail warAttackStart(WarSession warSession, String playerId, String opponentId,
                                       SquadNotification attackStartNotification, long time) {
        AttackDetail attackDetail;
        try (ClientSession session = this.mongoClient.startSession()) {
            session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            attackDetail = checkAndCreateWarBattleId(session, warSession.getWarId(), playerId, opponentId, time);
            if (attackDetail.getBattleId() != null) {
                WarNotificationData warNotificationData = (WarNotificationData) attackStartNotification.getData();
                warNotificationData.setAttackExpirationDate(attackDetail.getExpirationDate());
                setAndSaveWarNotification(session, attackDetail, warSession, attackStartNotification);
            }

            if (attackDetail.getReturnCode() == ResponseHelper.RECEIPT_STATUS_COMPLETE)
                session.commitTransaction();
            else
                session.abortTransaction();
        }

        return attackDetail;
    }

    @Override
    public AttackDetail warAttackComplete(WarSession warSession, PlayerSession playerSession,
                                          BattleReplay battleReplay,
                                          SquadNotification attackCompleteNotification,
                                          SquadNotification attackReplayNotification,
                                          DefendingWarParticipant defendingWarParticipant, long time) {
        AttackDetail attackDetail;
        try (ClientSession session = this.mongoClient.startSession()) {
            session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());

            // calculate how many stars earned in the attack
            int victoryPointsRemaining = defendingWarParticipant.getVictoryPoints();
            int victoryPointsEarned = (3 - victoryPointsRemaining) - battleReplay.battleLog.stars;
            if (victoryPointsEarned < 0)
                victoryPointsEarned = Math.abs(victoryPointsEarned);
            else
                victoryPointsEarned = 0;

            attackDetail = saveAndUpdateWarBattle(session, warSession.getWarId(), battleReplay, victoryPointsEarned);

            if (attackDetail.getReturnCode() == ResponseHelper.RECEIPT_STATUS_COMPLETE) {
                savePlayerSettings(playerSession, session);
                WarNotificationData warNotificationData = (WarNotificationData) attackCompleteNotification.getData();
                warNotificationData.setStars(battleReplay.battleLog.stars);
                warNotificationData.setVictoryPoints(victoryPointsEarned);
                setAndSaveWarNotification(session, attackDetail, warSession, attackCompleteNotification);
                setAndSaveWarNotification(session, attackDetail, warSession, attackReplayNotification);
                saveBattleReplay(session, battleReplay);
                session.commitTransaction();
            } else {
                session.abortTransaction();
            }
        }

        return attackDetail;
    }

    @Override
    public DefendingWarParticipant getDefendingWarParticipantByBattleId(String battleId) {
        SquadMemberWarData squadMemberWarData =
                this.squadMemberWarDataCollection.findOne(eq("defenseBattleId", battleId),
                        include("id", "victoryPoints"));

        if (squadMemberWarData == null)
            throw new RuntimeException("unable to find battleId in WarParticipants " + battleId);

        DefendingWarParticipant defendingWarParticipant =
                new DefendingWarParticipant(squadMemberWarData.id, squadMemberWarData.victoryPoints);

        return defendingWarParticipant;
    }

    private AttackDetail saveAndUpdateWarBattle(ClientSession clientSession, String warId,
                                                BattleReplay battleReplay,
                                                int victoryPointsEarned)
    {
        SquadMemberWarData defenderMemberWarData =
                this.squadMemberWarDataCollection.findOneAndUpdate(clientSession, combine(eq("warId", warId),
                                eq("defenseBattleId", battleReplay.getBattleId())),
                        combine(unset("defenseBattleId"),
                                unset("defenseExpirationDate"),
                                inc("victoryPoints", -1 * victoryPointsEarned)));

        boolean updated = defenderMemberWarData != null;

        if (updated) {
            SquadMemberWarData attackerMemberWarData =
                    this.squadMemberWarDataCollection.findOneAndUpdate(clientSession, combine(eq("warId", warId),
                                    eq("attackBattleId", battleReplay.getBattleId())),
                            combine(unset("attackBattleId"),
                                    unset("attackExpirationDate"),
                                    inc("score", victoryPointsEarned)));

            updated = attackerMemberWarData != null;
        }

        int response = ResponseHelper.RECEIPT_STATUS_COMPLETE;

        // TODO - not sure what to send back yet
        if (!updated)
            response = ResponseHelper.STATUS_CODE_NOT_MODIFIED;

        AttackDetail attackDetail = new AttackDetail(response);
        return attackDetail;
    }

    private void setAndSaveWarNotification(ClientSession clientSession, WarNotification warNotification, WarSession warSession,
                                           SquadNotification squadNotification) {
        synchronized (warSession) {
            GuildSession guildSessionA = warSession.getGuildASession();
            squadNotification.setDate(0);
            setAndSaveGuildNotification(clientSession, guildSessionA, squadNotification);
            warNotification.setGuildANotificationDate(squadNotification.getDate());
            GuildSession guildSessionB = warSession.getGuildBSession();
            squadNotification.setDate(0);
            setAndSaveGuildNotification(clientSession, guildSessionB, squadNotification);
            warNotification.setGuildBNotificationDate(squadNotification.getDate());
        }
    }

    private void setAndSaveGuildNotification(ClientSession clientSession, GuildSession guildSession, SquadNotification squadNotification) {
        synchronized (guildSession) {
            if (squadNotification.getDate() == 0)
                squadNotification.setDate(ServiceFactory.getSystemTimeSecondsFromEpoch());
            // have to reset the ID otherwise a shared notification saving to mongo will fail
            squadNotification.setId(ServiceFactory.createRandomUUID());
            squadNotification.setGuildId(guildSession.getGuildId());
            squadNotification.setGuildName(guildSession.getGuildName());
            saveNotification(clientSession, squadNotification);
        }
    }

    private void saveNotification(ClientSession clientSession, SquadNotification squadNotification) {
        this.squadNotificationCollection.insertOne(clientSession, squadNotification);
    }

    private AttackDetail checkAndCreateWarBattleId(ClientSession session, String warId, String playerId, String opponentId, long time) {
        AttackDetail attackDetail = null;

        try {
            String defenseBattleId = ServiceFactory.createRandomUUID();
            long defenseExpirationDate = ServiceFactory.getSystemTimeSecondsFromEpoch() +
                    ServiceFactory.instance().getConfig().attackDuration;

            // TODO - change this to findOneAndUpdate
            Bson defenseMatch = and(eq("warId", warId), eq("id", opponentId),
                    gt("victoryPoints", 0),
                    or(exists("defenseBattleId", false), lt("defenseExpirationDate", time - 10)));
            UpdateResult result = this.squadMemberWarDataCollection.updateOne(session, defenseMatch,
                    combine(set("defenseBattleId", defenseBattleId), set("defenseExpirationDate", defenseExpirationDate)));

            if (result.getMatchedCount() == 1) {
                attackDetail = new AttackDetail(defenseBattleId, defenseExpirationDate);
            }

            if (attackDetail != null) {
                Bson attackerMatch = and(eq("warId", warId), eq("id", playerId),
                        gt("turns", 0));
                UpdateResult updatePlayersTurns = this.squadMemberWarDataCollection.updateOne(session, attackerMatch,
                        combine(set("attackBattleId", defenseBattleId),
                                set("attackExpirationDate", defenseExpirationDate),
                                inc("turns", -1)));

                // if not changed then ran out of turns
                if (updatePlayersTurns.getMatchedCount() != 1) {
                    attackDetail = new AttackDetail(ResponseHelper.STATUS_CODE_GUILD_WAR_NOT_ENOUGH_TURNS);
                }
            } else {
                // no battle id generated means it is getting attacked by someone already
                SquadMemberWarData opponentData = this.squadMemberWarDataCollection.findOne(combine(eq("warId", warId),
                        eq("id", opponentId)), include("id", "victoryPoints", "defenseExpirationDate"));

                // base has been cleared
                if (opponentData.victoryPoints == 0) {
                    attackDetail = new AttackDetail(ResponseHelper.STATUS_CODE_GUILD_WAR_NOT_ENOUGH_VICTORY_POINTS);
                } else if (time > opponentData.defenseExpirationDate) {
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
        try (ClientSession session = this.mongoClient.startSession()) {
            session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            this.squadCollection.updateMany(eq("warId", war.getWarId()), combine(unset("warId"), unset("warSignUpTime")));
            session.commitTransaction();
        }
    }

    @Override
    public void saveWar(War war) {
        try (ClientSession session = this.mongoClient.startSession()) {
            session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            this.squadWarCollection.updateOne(session, eq("_id", war.getWarId()),
                    combine(set("prepGraceStartTime", war.getPrepGraceStartTime()),
                            set("prepEndTime", war.getPrepEndTime()),
                            set("actionGraceStartTime", war.getActionGraceStartTime()),
                            set("actionEndTime", war.getActionEndTime()),
                            set("cooldownEndTime", war.getCooldownEndTime()),
                            set("processedEndTime", war.getProcessedEndTime())));
            session.commitTransaction();
        }
    }

    @Override
    public Collection<SquadNotification> getSquadNotificationsSince(String guildId, String guildName, long since) {
        FindIterable<SquadNotification> squadNotifications =
                this.squadNotificationCollection.find(combine(eq("guildId", guildId), gt("date", since)));

        List<SquadNotification> notifications = new ArrayList<>();
        try (MongoCursor<SquadNotification> cursor = squadNotifications.cursor()) {
            while (cursor.hasNext()) {
                SquadNotification squadNotification = cursor.next();
                notifications.add(squadNotification);
            }
        }

        return notifications;
    }

    @Override
    public WarNotification warPrepared(WarSessionImpl warSession, String warId, SquadNotification warPreparedNotification) {
        WarNotification warNotification = new WarNotification();
        try (ClientSession session = this.mongoClient.startSession()) {
            session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            setAndSaveWarNotification(session, warNotification, warSession, warPreparedNotification);
            session.commitTransaction();
        }

        return warNotification;
    }

    @Override
    public HashMap<String, PvpMatch> getDevBaseMatches(PlayerSession playerSession) {
//        SELECT id, buildings, buildings, hqlevel, xp
//        FROM DevBases
//        WHERE (hqlevel >= ? -1 AND hqlevel <= ? +1)
//        or (xp >= ( ? * 0.9) AND xp <= (? * 1.10))
//        ORDER BY xp desc;

        int playerHq = playerSession.getHeadQuarter().getBuildingData().getLevel();
        int playerXp = playerSession.getPlayerSettings().getScalars().xp;
        Bson hqQuery = and(gte("hq", playerHq - 1), lte("hq", playerHq + 1));
        Bson xpQuery = and(gte("xp", playerXp * 0.9), lte("xp", playerXp * 1.10));

        List<Bson> aggregates = Arrays.asList(Aggregates.match(or(hqQuery, xpQuery)),
                Aggregates.project(include("xp", "hq")),
                Aggregates.sample(20));

        AggregateIterable<DevBase> devBasesIterable = this.devBaseCollection.aggregate(aggregates);

        HashMap<String, PvpMatch> pvpMatches = new HashMap<>();

        try (MongoCursor<DevBase> devBaseCursor = devBasesIterable.cursor()) {
            while (devBaseCursor.hasNext()) {
                DevBase devBase = devBaseCursor.next();
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
                pvpMatch.setParticipantId(devBase._id);
                pvpMatch.setDefenderXp(devBase.xp);
                pvpMatch.setBattleId(battleId);
                pvpMatch.setFactionType(playerSession.getFaction().equals(FactionType.empire) ? FactionType.rebel : FactionType.empire);
                pvpMatch.setDevBase(true);
                pvpMatch.setLevel(devBase.hq);
                pvpMatches.put(battleId, pvpMatch);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return pvpMatches;
    }

    @Override
    public Buildings getDevBaseMap(String id, FactionType faction) {
        Buildings buildings = this.devBaseCollection.findOne(eq("_id", id)).buildings;
        for (Building building : buildings) {
            building.uid = building.uid.replace(FactionType.neutral.name(), faction.name());
        }
        return buildings;
    }

    @Override
    public void saveNewPvPBattle(PlayerSession playerSession, BattleReplay battleReplay) {
        try (ClientSession session = this.mongoClient.startSession()) {
            session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            savePlayerSettings(playerSession, session);
            // TODO - to save defenders settings
            saveBattleReplay(session, battleReplay);
            session.commitTransaction();
        }
    }

    private void saveBattleReplay(ClientSession session, BattleReplay battleReplay) {
        this.battleReplayCollection.insertOne(session, battleReplay);
    }

    @Override
    public List<BattleLog> getPlayerBattleLogs(String playerId) {
        List<BattleLog> battleLogs = new ArrayList<>();

        Bson pvpOnPlayer = or(eq("attackerId", playerId), eq("defenderId", playerId));
        FindIterable<BattleReplay> battleReplaysIterable =
                this.battleReplayCollection.find(and(pvpOnPlayer, eq("battleType", BattleType.Pvp)))
                        .sort(descending("attackDate"))
                        .projection(include("battleLog", "attackerId", "defenderId", "attackDate"))
                .limit(30);

        try (MongoCursor<BattleReplay> cursor = battleReplaysIterable.cursor()) {
            while (cursor.hasNext()) {
                BattleReplay battleReplay = cursor.next();
                battleLogs.add(battleReplay.battleLog);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to get DB connection when retrieving playerbattlelogs", ex);
        }

        return battleLogs;
    }

    @Override
    public BattleReplay getBattleReplay(String battleId) {
        BattleReplay battleReplay = this.battleReplayCollection.findOne(eq("_id", battleId));
        return battleReplay;
    }

    public War processWarEnd(String warId, String squadIdA, String squadIdB) {
        try (ClientSession session = this.mongoClient.startSession()) {
            session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());

            SquadTotalScore squadAScore = null;
            SquadTotalScore squadBScore = null;

            // TODO - find out how to do this properly using mongo driver instead of mongojack
            Aggregation.Pipeline<?> pipeline = Aggregation.match(DBQuery.is("warId", warId)).group("guildId")
                    .set("totalScore", Aggregation.Group.sum("score"));
            AggregateIterable<SquadTotalScore> squadMemberWarData =
                    this.squadMemberWarDataCollection.aggregate(session, pipeline, SquadTotalScore.class);

            try (MongoCursor<SquadTotalScore> cursor = squadMemberWarData.cursor()) {
                while (cursor.hasNext()) {
                    SquadTotalScore squadTotalScore = cursor.next();
                    if (squadIdA.equals(squadTotalScore.guildId))
                        squadAScore = squadTotalScore;
                    else if (squadIdB.equals(squadTotalScore.guildId))
                        squadBScore = squadTotalScore;
                }
            }

            if (squadAScore == null || squadBScore == null)
                throw new RuntimeException("Failed to sum up squad war scores");

            long time = ServiceFactory.getSystemTimeSecondsFromEpoch();
            UpdateResult result = this.squadWarCollection.updateOne(session, combine(eq("_id", warId)),
                    combine(set("processedEndTime", time),
                            set("squadAScore", squadAScore.totalScore),
                            set("squadBScore", squadBScore.totalScore)));

            if (result.getMatchedCount() == 1) {
                clearWarParty(session, warId, squadIdA);
                clearWarParty(session, warId, squadIdB);
            }

            session.commitTransaction();
        }

        return this.getWar(warId);
    }

    @Override
    public void resetWarPartyForParticipants(String warId) {
        try (ClientSession session = this.mongoClient.startSession()) {
            session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build());
            SquadWar squadWar = this.squadWarCollection.findOne(eq("_id", warId));
            setSquadWarParty(session, squadWar.getSquadIdA(), squadWar.getSquadAWarSignUp().participantIds, squadWar.getSquadAWarSignUp().time);
            setSquadWarParty(session, squadWar.getSquadIdB(), squadWar.getSquadBWarSignUp().participantIds, squadWar.getSquadBWarSignUp().time);
            session.commitTransaction();
        }
    }

    @Override
    public void saveDevBase(DevBase devBase) {
        DevBase existingDevBase = this.devBaseCollection.findOne(eq("checksum", devBase.checksum));
        if (existingDevBase == null) {
            devBase._id = ServiceFactory.createRandomUUID();
            this.devBaseCollection.save(devBase);
        } else {
            LOG.debug("dev base with checksum already exists for file " + devBase.fileName);
        }
    }
}
