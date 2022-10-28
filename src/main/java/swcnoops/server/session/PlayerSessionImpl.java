package swcnoops.server.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.guild.TroopDonationResult;
import swcnoops.server.datasource.*;
import swcnoops.server.datasource.Player;
import swcnoops.server.game.*;
import swcnoops.server.model.*;
import swcnoops.server.session.map.*;
import swcnoops.server.session.creature.CreatureManager;
import swcnoops.server.session.creature.CreatureManagerFactory;
import swcnoops.server.session.creature.CreatureStatus;
import swcnoops.server.session.inventory.TroopInventory;
import swcnoops.server.session.inventory.TroopInventoryFactory;
import swcnoops.server.session.research.OffenseLab;
import swcnoops.server.session.research.OffenseLabFactory;
import swcnoops.server.session.training.BuildUnit;
import swcnoops.server.session.training.TrainingManager;
import swcnoops.server.session.training.TrainingManagerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * This represents the actions/commands that the game client can do for a player.
 * There should be no player state processing in the commands themselves, all those should be is mapping
 * to the response. Player State changes should be done in classes of package sessions.
 */
public class PlayerSessionImpl implements PlayerSession {
    private static final Logger LOG = LoggerFactory.getLogger(PlayerSessionImpl.class);

    private Player player;
    private TrainingManager trainingManager;
    private CreatureManager creatureManager;
    private TroopInventory troopInventory;
    private OffenseLab offenseLab;
    private DonatedTroops donatedTroops;
    private PlayerMapItems playerMapItems;
    private DroidManager droidManager;

    private PvpSessionImpl pvpSession = new PvpSessionImpl(this);

    static final private TrainingManagerFactory trainingManagerFactory = new TrainingManagerFactory();
    static final private CreatureManagerFactory creatureManagerFactory = new CreatureManagerFactory();
    static final private TroopInventoryFactory troopInventoryFactory = new TroopInventoryFactory();
    static final private OffenseLabFactory offenseLabFactory = new OffenseLabFactory();

    private NotificationSession notificationSession = new NotificationSession(this);

    private Lock donationLock = new ReentrantLock();
    private Lock notificationLock = new ReentrantLock();
    private DBCacheObject<InventoryStorage> inventoryManager = new DBCacheObjectImpl<InventoryStorage>() {
        @Override
        protected InventoryStorage loadDBObject() {
            return ServiceFactory.instance().getPlayerDatasource().loadPlayerSettings(player.getPlayerId(),
                            false, "playerSettings.inventoryStorage")
                    .getInventoryStorage();
        }
    };

    private DBCacheObjectSaving<PvpAttack> pvPAttack = new SaveOnlyDBCacheObject<>();
    private ReadOnlyDBCacheObject<PvpAttack> currentPvPDefending = new ReadOnlyDBCacheObject<PvpAttack>(true) {
        @Override
        protected PvpAttack loadDBObject() {
            return ServiceFactory.instance().getPlayerDatasource().loadPlayer(player.getPlayerId(),
                            false, "currentPvPDefence")
                    .getCurrentPvPDefence();
        }
    };

    private DBCacheObject<Scalars> scalarsManager = new DBCacheObjectImpl<Scalars>() {
        @Override
        protected Scalars loadDBObject() {
            return ServiceFactory.instance().getPlayerDatasource().loadPlayerSettings(player.getPlayerId(),
                            false, "playerSettings.scalars")
                    .getScalars();
        }
    };

    private DBCacheObjectRead<Map<String, Integer>> damagedBuildingManager = new ReadOnlyDBCacheObject<Map<String, Integer>>(true) {
        @Override
        protected Map<String, Integer> loadDBObject() {
            return ServiceFactory.instance().getPlayerDatasource().loadPlayerSettings(player.getPlayerId(),
                            false, "playerSettings.damagedBuildings").getDamagedBuildings();
        }
    };
    private long lastLoginTime;
    private DBCacheObject<List<TournamentStat>> tournamentsManager = new DBCacheObjectImpl<List<TournamentStat>>() {
        @Override
        protected List<TournamentStat> loadDBObject() {
            return ServiceFactory.instance().getPlayerDatasource().loadPlayerSettings(player.getPlayerId(),
                    false, "playerSettings.tournaments").getTournaments();
        }
    };

    public PlayerSessionImpl(Player player) {
        this.initialise(player);
    }

    @Override
    public void initialise(Player player) {
        this.player = player;
        this.playerMapItems = createPlayersMap(this.getPlayerSettings().getBaseMap());
        this.troopInventory = PlayerSessionImpl.troopInventoryFactory.createForPlayer(this);
        this.trainingManager = PlayerSessionImpl.trainingManagerFactory.createForPlayer(this);
        this.creatureManager = PlayerSessionImpl.creatureManagerFactory.createForPlayer(this);
        this.offenseLab = PlayerSessionImpl.offenseLabFactory.createForPlayer(this);
        this.donatedTroops = this.getPlayerSettings().getDonatedTroops();
        this.droidManager = new DroidManager(this);
        mapBuildingContracts(this.getPlayerSettings());

        // DB cache objects to allow smart saving to DB
        this.inventoryManager.initialise(this.player.getPlayerSettings().getInventoryStorage());
        this.scalarsManager.initialise(this.player.getPlayerSettings().getScalars());
        this.currentPvPDefending.initialise(this.player.getCurrentPvPDefence());
        this.damagedBuildingManager.initialise(this.player.getPlayerSettings().getDamagedBuildings());
        this.tournamentsManager.initialise(this.player.getPlayerSettings().getTournaments());
        this.lastLoginTime = player.getLoginTime();
    }

    @Override
    public long getLastLoginTime() {
        return lastLoginTime;
    }

    private void mapBuildingContracts(PlayerSettings playerSettings) {
        for (BuildUnit buildUnit : playerSettings.getBuildContracts()) {
            if (isDroidContract(buildUnit.getContractType()))
                this.droidManager.addBuildUnit(buildUnit);
        }
    }

    private boolean isDroidContract(ContractType contractType) {
        if (contractType == ContractType.Build)
            return true;
        if (contractType == ContractType.Upgrade)
            return true;
        if (contractType == ContractType.Clear)
            return true;

        return false;
    }

    private PlayerMapItems createPlayersMap(PlayerMap baseMap) {
        PlayerMapItems playerMapItems = new PlayerMapItems(baseMap);

        if (baseMap != null) {
            for (Building building : baseMap.buildings) {
                BuildingData buildingData = ServiceFactory.instance().getGameDataManager().getBuildingDataByUid(building.uid);
                if (buildingData != null) {
                    MapItem mapItem = PlayerMapItems.createMapItem(building, buildingData);
                    playerMapItems.add(mapItem.getBuildingKey(), mapItem);
                }
            }
        }

        return playerMapItems;
    }

    @Override
    public DroidManager getDroidManager() {
        return this.droidManager;
    }

    @Override
    public MapItem getSquadBuilding() {
        return this.playerMapItems.getMapItemByType(BuildingType.squad);
    }

    @Override
    public MapItem getHeadQuarter() {
        return this.playerMapItems.getMapItemByType(BuildingType.HQ);
    }

    @Override
    public String getPlayerId() {
        return this.player.getPlayerId();
    }

    @Override
    public TroopInventory getTroopInventory() {
        return troopInventory;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void trainTroops(String buildingId, String unitTypeId, int quantity, int credits, int contraband, long startTime) {
        this.processCompletedContracts(startTime);
        CurrencyDelta currencyDelta =
                this.trainingManager.trainTroops(buildingId, unitTypeId, quantity, credits, contraband, startTime);
        this.processInventoryStorage(currencyDelta);
        savePlayerSession();
    }

    @Override
    public void cancelTrainTroops(String buildingId, String unitTypeId, int quantity, int credits, int materials,
                                  int contraband, long time)
    {
        this.processCompletedContracts(time);
        CurrencyDelta currencyDelta = this.trainingManager.cancelTrainTroops(buildingId, unitTypeId, quantity,
                credits, materials, contraband, time);
        this.processInventoryStorage(currencyDelta);
        savePlayerSession();
    }

    @Override
    public void buyOutTrainTroops(String buildingId, String unitTypeId, int quantity, int crystals, long time) {
        this.processCompletedContracts(time);
        CurrencyDelta currencyDelta = this.trainingManager.buyOutTrainTroops(buildingId, unitTypeId, quantity, crystals, time);
        this.processInventoryStorage(currencyDelta);
        this.savePlayerSession();
    }

    /**
     * Removing completed contracts during base building
     * @param deployablesToRemove
     * @param time
     */
    @Override
    public void removeDeployedTroops(Map<String, Integer> deployablesToRemove, long time) {
        if (deployablesToRemove != null) {
            this.processCompletedContracts(time);
            this.trainingManager.removeDeployedTroops(deployablesToRemove);
            this.savePlayerSession();
        }
    }

    /**
     * This is removal during a battle to remove troops that were used.
     * Creatures if they stay alive during a battle seem to stay active
     * @param deployablesToRemove
     * @param time
     */
    @Override
    public void removeDeployedTroops(List<DeploymentRecord> deployablesToRemove, long time) {
        if (deployablesToRemove != null) {
            this.processCompletedContracts(time);
            this.trainingManager.removeDeployedTroops(deployablesToRemove);

            // TODO - this does not handle receiving donations while doing an attack
            // will have to rethink how to handle all the race conditions and overwriting.
            // probably the best way would be to to use the DB to do the removing on a unit level which means
            // knowing which troops were actually taken for the battle
            if (deployablesToRemove.stream().filter(a -> a.getAction().equals("SquadTroopPlaced")).count() > 0)
                this.getDonatedTroops().clear();

            this.savePlayerSession();
        }
    }

    /**
     * Before a battle we move all completed troops to their transport, as those are the troops going to war.
     * We do this as during the battle, deployment records are sent which we use to remove from deployables
     *
     * @param missionUid
     * @param time
     */
    @Override
    public String playerPveBattleStart(String missionUid, long time) {
        this.processCompletedContracts(time);
        String guid = ServiceFactory.createRandomUUID();

        if (missionUid != null) {
            PlayerCampaignMission playerCampaignMission = this.getPlayerSettings().getPlayerCampaignMission();
            Mission mission = playerCampaignMission.missions.get(missionUid);
            if (mission != null) {
                mission.lastBattleId = guid;
            }
        }

        this.savePlayerSession();
        return guid;
    }

    // TODO - rewrite this as this should be private, need a nicer way to save squad and player sessions together
    // thinking maybe save squad first then player, on load check that the player is still in squad
    @Override
    public void savePlayerSession() {
        ServiceFactory.instance().getPlayerDatasource().savePlayerSession(this);
    }

    @Override
    public void savePlayerKeepAlive() {
        ServiceFactory.instance().getPlayerDatasource().savePlayerKeepAlive(this);
    }

    @Override
    public void savePlayerName(String playerName) {
        ServiceFactory.instance().getPlayerDatasource().savePlayerName(this, playerName);
    }

    @Override
    public void recoverWithPlayerSettings(PlayerModel playerModel, Map<String, String> sharedPrefs) {
        ServiceFactory.instance().getPlayerDatasource().recoverWithPlayerSettings(this, playerModel, sharedPrefs);
    }

    private void processCompletedContracts(long time) {
        if (this.offenseLab != null && this.offenseLab.processCompletedUpgrades(time))
            this.trainingManager.recalculateContracts(time);
        this.trainingManager.moveCompletedBuildUnits(time);
        this.droidManager.processCompletedBuildUnits(time);
    }

    @Override
    public TrainingManager getTrainingManager() {
        return trainingManager;
    }

    @Override
    public PlayerSettings getPlayerSettings() {
        return player.getPlayerSettings();
    }

    @Override
    public void recaptureCreature(String instanceId, String creatureTroopUid, long time) {
        this.processCompletedContracts(time);
        TroopData troopData = ServiceFactory.instance().getGameDataManager().getTroopDataByUid(creatureTroopUid);
        this.creatureManager.recaptureCreature(troopData.getUnitId(), time);
        this.savePlayerSession();
    }

    @Override
    public void buildingBuyout(String buildingId, String tag, int credits, int materials, int contraband, int crystals, long time) {
        this.processCompletedContracts(time);

        CurrencyDelta currencyDelta;
        if (this.creatureManager.isRecapturing() && this.creatureManager.getBuildingKey().equals(buildingId)) {
            currencyDelta = this.creatureManager.buyout(crystals, time);
        } else if (this.offenseLab != null && offenseLab.isResearchingTroop()
                && this.offenseLab.getBuildingKey().equals(buildingId)) {
            currencyDelta = this.offenseLab.buyout(crystals, time);
            this.trainingManager.recalculateContracts(time);
        } else {
            currencyDelta = this.droidManager.buyout(buildingId, crystals, time);
        }

        this.processInventoryStorage(currencyDelta);
        this.savePlayerSession();
    }

    @Override
    public void buildingCancel(String buildingId, String tag, int credits, int materials, int contraband, long time) {
        this.processCompletedContracts(time);

        // for cancels on offenseLab, it could be cancelling the building or the research of troops
        CurrencyDelta currencyDelta;
        if (this.offenseLab != null && this.offenseLab.getBuildingKey().equals(buildingId)) {
            if (offenseLab.isResearchingTroop()) {
                currencyDelta = this.offenseLab.cancel(time, credits, materials, contraband);
            } else {
                currencyDelta = this.droidManager.cancel(buildingId, credits, materials, contraband, time);
            }
        } else {
            currencyDelta = this.droidManager.cancel(buildingId, credits, materials, contraband, time);
        }

        this.processInventoryStorage(currencyDelta);
        this.savePlayerSession();
    }

    @Override
    public CreatureManager getCreatureManager() {
        return creatureManager;
    }

    @Override
    public void deployableUpgradeStart(String buildingId, String troopUid, int credits, int materials, int contraband, long time) {
        this.processCompletedContracts(time);
        if (this.offenseLab != null) {
            CurrencyDelta currencyDelta = this.offenseLab.upgradeStart(buildingId, troopUid, credits, materials, contraband, time);
            this.processInventoryStorage(currencyDelta);
        }

        this.savePlayerSession();
    }

    @Override
    public void playerLogin(long time) {
        this.processCompletedContracts(ServiceFactory.getSystemTimeSecondsFromEpoch());
        this.notificationSession.playerLogin();
        removeEjectedNotifications();

        if (this.getFaction() != FactionType.smuggler)
            validateInventoryTotalCapacity(this);

        this.getPvpSession().playerLogin();
    }

    @Override
    public void savePlayerLogin(long time) {
        ServiceFactory.instance().getPlayerDatasource().savePlayerLogin(this);
    }

    private void validateInventoryTotalCapacity(PlayerSession playerSession) {
        InventoryStorage inventoryStorage = playerSession.getInventoryManager().getObjectForWriting();

        inventoryStorage.credits.capacity = CurrencyHelper.getTotalCapacity(playerSession, CurrencyType.credits);
        inventoryStorage.materials.capacity = CurrencyHelper.getTotalCapacity(playerSession, CurrencyType.materials);
        inventoryStorage.contraband.capacity = CurrencyHelper.getTotalCapacity(playerSession, CurrencyType.contraband);

        if (inventoryStorage.credits.amount > inventoryStorage.credits.capacity)
            inventoryStorage.credits.amount = inventoryStorage.credits.capacity;

        if (inventoryStorage.materials.amount > inventoryStorage.materials.capacity)
            inventoryStorage.materials.amount = inventoryStorage.materials.capacity;

        if (inventoryStorage.contraband.amount > inventoryStorage.contraband.capacity)
            inventoryStorage.contraband.amount = inventoryStorage.contraband.capacity;

        if (ServiceFactory.instance().getConfig().freeResources &&
                this.getPlayerSettings().getCurrentQuest() != null && this.getPlayerSettings().getCurrentQuest() .trim().isEmpty()
                && playerSession.getPlayerSettings().getName() != null && !playerSession.getPlayerSettings().getName().isEmpty()) {
            inventoryStorage.crystals.amount = 9999999;
        }
    }

    private void removeEjectedNotifications() {
        this.playerNotifications.removeIf(a -> a.getType() == SquadMsgType.ejected);
    }

    @Override
    public SquadNotification troopsRequest(DonatedTroops donatedTroops, String warId, boolean payToSkip, String message, long time) {
        this.processCompletedContracts(time);

        GuildSession guildSession = this.getGuildSession();

        // we need to check because they might request when they have been kicked out of the squad
        if (guildSession == null)
            return null;

        TroopRequestData troopRequestData = new TroopRequestData();
        troopRequestData.totalCapacity = this.getSquadBuilding().getBuildingData().getStorage();
        troopRequestData.troopDonationLimit = troopRequestData.totalCapacity;
        troopRequestData.amount = getSquadBuildingAvailableSpace(donatedTroops);
        troopRequestData.warId = warId;

        // this will save the notification for the guild for when the clients call GuildNotificationsGet they will
        // pick up the request. Will probably have to change so that notifications are sent on other requests
        SquadNotification squadNotification = guildSession.troopsRequest(this,
                troopRequestData, message, time);

        return squadNotification;
    }

    public int getSquadBuildingAvailableSpace(DonatedTroops donatedTroops) {
        return (this.getSquadBuilding().getBuildingData().getStorage() - this.getDonatedTroopsTotalUnits(donatedTroops));
    }

    @Override
    public TroopDonationResult troopsDonate(Map<String, Integer> troopsDonated, String requestId, String recipientId,
                                            boolean forWar, long time) {
        this.processCompletedContracts(time);
        GuildSession guildSession = this.getGuildSession();

        TroopDonationResult troopDonationResult = null;
        if (guildSession != null) {
            troopDonationResult = guildSession.troopDonation(troopsDonated,
                    requestId, this, recipientId, forWar, time);
        }

        return troopDonationResult;
    }

    @Override
    public Map<String, Integer> levelUpTroopsByUid(Map<String, Integer> donatedTroops) {
        Map<String, Integer> levelUpTroops = new HashMap<>();

        if (donatedTroops != null) {
            GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
            donatedTroops.forEach((donatedUid, numberOf) -> {
                TroopData troopData = gameDataManager.getTroopDataByUid(donatedUid);
                if (troopData != null) {
                    TroopData playersTroopData = this.getTroopInventory().getTroopByUnitId(troopData.getUnitId());
                    if (playersTroopData != null) {
                        if (playersTroopData.getLevel() > troopData.getLevel()) {
                            donatedUid = playersTroopData.getUid();
                        }
                    }
                }

                levelUpTroops.put(donatedUid, numberOf);
            });
        }

        return levelUpTroops;
    }

    @Override
    public boolean setLastNotificationSince(String guildId, long since) {
        return this.notificationSession.setLastNotification(guildId, since);
    }

    @Override
    public boolean hasNotificationsToSend() {
        return this.notificationSession.canSendNotifications();
    }

    @Override
    public long getNotificationsSince() {
        return this.notificationSession.getNotificationsSince();
    }

    private Queue<SquadNotification> playerNotifications = new ConcurrentLinkedQueue<>();

    @Override
    public List<SquadNotification> getNotifications(long since) {
        List<SquadNotification> notifications =
                this.playerNotifications.stream().filter(n -> n.getDate() > since).collect(Collectors.toList());
        return notifications;
    }

    @Override
    public void removeEjectedNotifications(List<SquadNotification> ejectedNotifications) {
        this.playerNotifications.removeAll(ejectedNotifications);
    }

    @Override
    public void addSquadNotification(SquadNotification squadNotification) {
        this.notificationLock.lock();
        try {
            switch (squadNotification.getType()) {
                case ejected:
                case joinRequestRejected:
                case joinRequestAccepted:
                    this.playerNotifications.clear();
                    break;
            }

            this.playerNotifications.add(squadNotification);
        } finally {
            this.notificationLock.unlock();
        }
    }

    @Override
    public boolean isInGuild(String guildId) {
        GuildSession guild = this.getGuildSession();
        if (guild != null) {
            return guildId.equals(guild.getGuildId());
        }

        return false;
    }

    public GuildSession getGuildSession() {
        String guildId = this.getPlayerSettings().getGuildId();
        if (guildId == null || guildId.isEmpty())
            return null;

        GuildSession guildSession = ServiceFactory.instance().getSessionManager()
                .getGuildSession(this, guildId);

        if (guildSession != null)
            guildSession.login(this);

        return guildSession;
    }

    @Override
    public void setGuildSession(GuildSession guildSession) {
        if (guildSession != null) {
            this.getPlayerSettings().setGuildId(guildSession.getGuildId());
            this.getPlayerSettings().setGuildName(guildSession.getGuildName());
        } else {
            this.getPlayerSettings().setGuildId(null);
            this.getPlayerSettings().setGuildName(null);
        }
    }

    @Override
    public DonatedTroops getDonatedTroops() {
        return donatedTroops;
    }

    /**
     * checks there is enough space, if everything fits then it will return true
     * else nothing gets added and returns false
     *
     * @param troopsDonated
     * @param playerId
     * @param troopsInSC
     * @return
     */
    @Override
    public boolean processDonatedTroops(Map<String, Integer> troopsDonated, String playerId, DonatedTroops troopsInSC) {
        boolean donated = false;
        this.donationLock.lock();
        try {
            int totalDonatedUnits = calculateTroopUnitsByUid(troopsDonated);
            if (totalDonatedUnits <= this.getSquadBuildingAvailableSpace(troopsInSC)) {
                troopsDonated.forEach((a, b) -> addDonatedTroop(troopsInSC, a, b, playerId));
                donated = true;
            }
        } finally {
            this.donationLock.unlock();
        }

        return donated;
    }

    private int calculateTroopUnitsByUid(Map<String, Integer> troopsDonated) {
        int units = 0;
        for (Map.Entry<String, Integer> donation : troopsDonated.entrySet()) {
            TroopData troopData =
                    ServiceFactory.instance().getGameDataManager().getTroopDataByUid(donation.getKey());
            units += troopData.getSize() * donation.getValue();
        }
        return units;
    }

    @Override
    public int getDonatedTroopsTotalUnits(DonatedTroops donatedTroops) {
        AtomicInteger totalUnits = new AtomicInteger(0);
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        if (donatedTroops != null) {
            donatedTroops.forEach((a, b) -> b.values()
                    .forEach(v -> totalUnits.addAndGet(gameDataManager.getTroopDataByUid(a).getSize() * v)));
        }
        return totalUnits.get();
    }

    @Override
    public void warBattleComplete(String battleId, int stars, Map<String, Integer> attackingUnitsKilled, long time) {
        processBattleComplete(attackingUnitsKilled, time);
        this.savePlayerSession();
    }

    private void processPvpBattleComplete(BattleReplay battleReplay, long time) {
        this.processBattleComplete(battleReplay.battleLog.attackingUnitsKilled, time);
    }

    private void processBattleComplete(Map<String, Integer> attackingUnitsKilled, long time) {
        this.processCompletedContracts(time);
        processCreature(attackingUnitsKilled);
        Map<String, Integer> champions = getUnitsKilledByTroopType(attackingUnitsKilled, TroopType.champion);
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        Map<String,Integer> killedChampions = gameDataManager.remapTroopUidToUnitId(champions);
        // TODO - change and simplify deployable troops to use a DBObject
        this.getTrainingManager().getDeployableChampion().removeDeployable(killedChampions);
    }

    @Override
    public void pveBattleComplete(String battleId, int stars, Map<String, Integer> attackingUnitsKilled, long time) {
        processBattleComplete(attackingUnitsKilled, time);
        PlayerCampaignMission playerCampaignMission = this.getPlayerSettings().getPlayerCampaignMission();
        playerCampaignMission.battleComplete(battleId, stars);
        this.savePlayerSession();
    }

    @Override
    public void pvpBattleComplete(BattleReplay battleReplay,
                                  PvpMatch pvpMatch, long time)
    {
        processPvpBattleComplete(battleReplay, time);
        calculateResourcesGained(battleReplay, pvpMatch);
        updatePlayerAfterPvPBattle(battleReplay, pvpMatch);
        updateDefenderAfterPvPBattle(battleReplay, pvpMatch);
        ServiceFactory.instance().getPlayerDatasource().savePvPBattleComplete(this, pvpMatch, battleReplay);
    }

    private void updateDefenderAfterPvPBattle(BattleReplay battleReplay, PvpMatch pvpMatch) {
        pvpMatch.getDefendersScalars().defensesWon += battleReplay.battleLog.stars == 0 ? 1 : 0;
        pvpMatch.getDefendersScalars().defensesLost += battleReplay.battleLog.stars == 0 ? 0 : 1;
        pvpMatch.getDefendersScalars().defenseRating += pvpMatch.getDefender().defenseRatingDelta;

        // update if it was for PvP
        if (pvpMatch.getTournamentData() != null) {
            ConflictManager conflictManager = ServiceFactory.instance().getGameDataManager().getConflictManager();
            TournamentStat tournamentStat = conflictManager.getTournamentStats(pvpMatch.getDefendersTournaments(),
                    pvpMatch.getTournamentData());

            // only if the player has attacked at least once to join conflict its defence will count
            if (tournamentStat != null) {
                tournamentStat.value += pvpMatch.getDefender().tournamentRatingDelta;
                tournamentStat.defensesWon = battleReplay.battleLog.stars == 0 ? tournamentStat.defensesWon + 1 : tournamentStat.defensesWon;
            }
        }

        // what they gain is what the defender lose
        if (pvpMatch.getDefendersInventoryStorage() != null) {
            pvpMatch.getDefendersInventoryStorage().credits.amount -= pvpMatch.getCreditsGained();
            pvpMatch.getDefendersInventoryStorage().materials.amount -= pvpMatch.getMaterialsGained();
            pvpMatch.getDefendersInventoryStorage().contraband.amount -= pvpMatch.getContraGained();
        }

        // creature
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        if (pvpMatch.getDefendersDeployableTroopsChampion() != null) {
            Map<String, Integer> championsKilled = getUnitsKilledByTroopType(battleReplay.battleLog.defendingUnitsKilled, TroopType.champion);
            Map<String,Integer> killedChampions = gameDataManager.remapTroopUidToUnitId(championsKilled);
            killedChampions.forEach((a, b) -> pvpMatch.getDefendersDeployableTroopsChampion().remove(a));
        }

        // triggered traps
        if (pvpMatch.getDefenderDamagedBuildings() != null && pvpMatch.getDefendersBaseMap() != null) {
            Map<String, Integer> damagedBuildings = pvpMatch.getDefenderDamagedBuildings();

            for (Building building : pvpMatch.getDefendersBaseMap().buildings) {
                if (damagedBuildings.containsKey(building.key)) {
                    BuildingData buildingData = gameDataManager.getBuildingDataByUid(building.uid);
                    if (buildingData.getType() == BuildingType.trap) {
                        building.currentStorage = 0;
                    }
                }
            }
        }

        // creature
        if (pvpMatch.getDefendersCreature() != null) {
            Map<String, Integer> creatureKilled = getUnitsKilledByTroopType(battleReplay.battleLog.defendingUnitsKilled, TroopType.creature);
            if (creatureKilled != null && creatureKilled.size() > 0) {
                pvpMatch.getDefendersCreature().setCreatureStatus(CreatureStatus.Dead);
            }
        }

        // SC troops killed
        if (pvpMatch.getDefendersDonatedTroops() != null && battleReplay.battleLog.defenderGuildTroopsExpended != null) {
            List<String> allKilledUnits = new ArrayList<>(pvpMatch.getDefendersDonatedTroops().size());
            for (Map.Entry<String, Integer> killedTroops : battleReplay.battleLog.defenderGuildTroopsExpended.entrySet()) {
                GuildDonatedTroops guildDonatedTroops = pvpMatch.getDefendersDonatedTroops().get(killedTroops.getKey());
                if (guildDonatedTroops != null) {
                    int killed = killedTroops.getValue();
                    int remaining = 0;
                    for (Map.Entry<String, Integer> donated : guildDonatedTroops.entrySet()) {
                        if (donated.getValue() > 0) {
                            if (donated.getValue() >= killed) {
                                donated.setValue(donated.getValue() - killed);
                                killed = 0;
                            } else if (killed > 0) {
                                killed = killed - donated.getValue();
                                donated.setValue(0);
                            }
                        }

                        remaining += donated.getValue();
                    }

                    if (remaining == 0) {
                        allKilledUnits.add(killedTroops.getKey());
                    }
                }
            }

            // clean up the donated if all dead
            for (String allKilledUid : allKilledUnits) {
                pvpMatch.getDefendersDonatedTroops().remove(allKilledUid);
            }
        }
    }

    private void calculateResourcesGained(BattleReplay battleReplay, PvpMatch pvpMatch) {
        int creditsGained = battleReplay.battleLog.earned.credits;
        int creditsAvailable = CurrencyHelper.calculateStorageAvailable(CurrencyType.credits, this);
        creditsGained = Math.min(creditsAvailable, creditsGained);
        pvpMatch.setCreditsGained(creditsGained);

        int materialsGained = battleReplay.battleLog.earned.materials;
        int materialsAvailable = CurrencyHelper.calculateStorageAvailable(CurrencyType.materials, this);
        materialsGained = Math.min(materialsAvailable, materialsGained);
        pvpMatch.setMaterialsGained(materialsGained);

        int contraGained = battleReplay.battleLog.earned.contraband;
        int contrabandAvailable = CurrencyHelper.calculateStorageAvailable(CurrencyType.contraband, this);
        contraGained = Math.min(contrabandAvailable, contraGained);
        pvpMatch.setContraGained(contraGained);
    }

    private void updatePlayerAfterPvPBattle(BattleReplay battleReplay, PvpMatch pvpMatch) {
        this.updatePlayerInventoryAfterPvP(pvpMatch);
        this.updateAttackScalars(battleReplay, pvpMatch);
        this.updateTournaments(battleReplay, pvpMatch);
    }

    private void updateTournaments(BattleReplay battleReplay, PvpMatch pvpMatch) {
        TournamentData tournamentData = pvpMatch.getTournamentData();
        if (tournamentData != null) {
            List<TournamentStat> tournamentStats = this.getTournamentsManager().getObjectForWriting();
            if (tournamentStats == null) {
                tournamentStats = new ArrayList<>();
                this.getTournamentsManager().setObjectForSaving(tournamentStats);
            }

            ConflictManager conflictManager = ServiceFactory.instance().getGameDataManager().getConflictManager();
            TournamentStat tournamentStat = conflictManager.getTournamentStats(tournamentStats, tournamentData);

            if (tournamentStat == null) {
                tournamentStat = new TournamentStat();
                tournamentStat.uid = tournamentData.getUid();
                tournamentStats.add(tournamentStat);
            }

            tournamentStat.value += pvpMatch.getAttacker().tournamentRatingDelta;
            tournamentStat.attacksWon = battleReplay.battleLog.stars > 0 ? tournamentStat.attacksWon + 1 : tournamentStat.attacksWon;
        }
    }

    // TODO - looks like in PvP will report looted over what is available for our storage
    private void updatePlayerInventoryAfterPvP(PvpMatch pvpMatch) {
        CurrencyDelta creditDelta = new CurrencyDelta(pvpMatch.getCreditsGained(),
                pvpMatch.getCreditsGained(), CurrencyType.credits, false);
        this.processInventoryStorage(creditDelta);

        CurrencyDelta materialDelta = new CurrencyDelta(pvpMatch.getMaterialsGained(),
                pvpMatch.getMaterialsGained(), CurrencyType.materials, false);
        this.processInventoryStorage(materialDelta);

        CurrencyDelta contraDelta = new CurrencyDelta(pvpMatch.getContraGained(), pvpMatch.getContraGained(),
                CurrencyType.contraband, false);
        this.processInventoryStorage(contraDelta);
    }

    private void updateAttackScalars(BattleReplay battleReplay, PvpMatch pvpMatch) {
        Scalars scalars = this.getScalarsManager().getObjectForWriting();
        scalars.attacksStarted++;
        scalars.attacksCompleted = battleReplay.battleLog.isUserEnded ? scalars.attacksCompleted : scalars.attacksCompleted + 1;
        scalars.attacksWon = battleReplay.battleLog.stars > 0 ? scalars.attacksWon + 1 : scalars.attacksWon;
        scalars.attacksLost = battleReplay.battleLog.stars == 0 ? scalars.attacksLost + 1 : scalars.attacksLost;
        scalars.attackRating = scalars.attackRating + pvpMatch.getAttacker().attackRatingDelta;
    }

    private Map<String, Integer> getUnitsKilledByTroopType(Map<String, Integer> attackingUnitsKilled, TroopType troopType) {
        Map<String, Integer> unitsOfType = new HashMap<>();
        if (attackingUnitsKilled != null) {
            GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
            for (Map.Entry<String, Integer> entry : attackingUnitsKilled.entrySet()) {
                TroopData troopData = gameDataManager.getTroopDataByUid(entry.getKey());
                if (troopData.getType() == troopType) {
                    unitsOfType.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return unitsOfType;
    }

    @Override
    public void buildingMultimove(PositionMap positions, long time) {
        this.processCompletedContracts(time);
        positions.forEach((a,b) -> buildingMultimove(a,b));
        this.savePlayerSession();
    }

    @Override
    public void buildingCollect(String buildingId, int credits, int materials, int contraband, long time) {
        this.processCompletedContracts(time);
        MapItem mapItem = this.getMapItemByKey(buildingId);
        if (mapItem != null) {
            CurrencyDelta currencyDelta = mapItem.collect(this, credits, materials, contraband, time, false);
            this.processInventoryStorage(currencyDelta);
            this.savePlayerSession();
        }
    }

    /**
     * Do a running deduction of what the server thinks the generator has.
     * For collectAll the client sends a list of generators in the order to collect.
     * It only provides the total collected and not each individual generator.
     */
    @Override
    public void buildingCollectAll(List<String> buildingIds, int credits, int materials, int contraband, long time) {
        this.processCompletedContracts(time);

        int givenCredits = CurrencyHelper.calculateGivenRefund(this, credits, materials, contraband, CurrencyType.credits);
        int givenMaterials = CurrencyHelper.calculateGivenRefund(this, credits, materials, contraband, CurrencyType.materials);
        int givenContraband = CurrencyHelper.calculateGivenRefund(this, credits, materials, contraband, CurrencyType.contraband);

        credits = givenCredits;
        materials = givenMaterials;
        contraband = givenContraband;

        int totalCredits = 0;
        int totalMaterials = 0;
        int totalContraband = 0;

        if (buildingIds != null) {
            for (String buildingId : buildingIds) {
                MapItem mapItem = this.getMapItemByKey(buildingId);
                if (mapItem != null) {
                    CurrencyDelta currencyDelta =
                            mapItem.collect(this, givenCredits, givenMaterials, givenContraband, time, true);
                    if (currencyDelta != null) {
                        this.processInventoryStorage(currencyDelta);

                        switch (currencyDelta.getCurrency()) {
                            case credits:
                                givenCredits -= currencyDelta.getExpectedDelta();
                                totalCredits += currencyDelta.getGivenDelta();
                                if (givenCredits < 0)
                                    givenCredits = 0;
                                break;
                            case materials:
                                givenMaterials += currencyDelta.getExpectedDelta();
                                totalMaterials += currencyDelta.getGivenDelta();
                                if (givenMaterials < 0)
                                    givenMaterials = 0;
                                break;
                            case contraband:
                                givenContraband += currencyDelta.getExpectedDelta();
                                totalContraband += currencyDelta.getGivenDelta();
                                if (givenContraband < 0)
                                    givenContraband = 0;
                                break;
                        }
                    }
                }
            }
        }

        if (totalCredits != credits)
            LOG.warn(this.getPlayerId() + " CollectAll had a miss match for credits " + credits + ","  + totalCredits);
        if (totalMaterials != materials)
            LOG.warn(this.getPlayerId() + " CollectAll had a miss match for materials " + materials + ","  + totalMaterials);
        if (totalContraband != contraband)
            LOG.warn(this.getPlayerId() + " CollectAll had a miss match for contraband " + contraband + ","  + totalContraband);
        this.savePlayerSession();
    }

    private void removeFromInventoryStorage(CurrencyDelta currencyDelta, PlayerSession playerSession) {
        if (currencyDelta != null && currencyDelta.getCurrency() != null) {
            // they dont match so we log for now
            if (currencyDelta.getGivenDelta() != currencyDelta.getExpectedDelta()) {
                LOG.warn("expected to remove " + currencyDelta.getCurrency() + " " +
                        currencyDelta.getExpectedDelta() + " but was given " +
                        currencyDelta.getGivenDelta() + " for player " + playerSession.getPlayerId());
            }

            InventoryStorage inventoryStorage = playerSession.getInventoryManager().getObjectForWriting();
            switch (currencyDelta.getCurrency()) {
                case credits:
                    inventoryStorage.credits.amount -= currencyDelta.getGivenDelta();
                    break;
                case materials:
                    inventoryStorage.materials.amount -= currencyDelta.getGivenDelta();
                    break;
                case contraband:
                    inventoryStorage.contraband.amount -= currencyDelta.getGivenDelta();
                    break;
                case crystals:
                    inventoryStorage.crystals.amount -= currencyDelta.getGivenDelta();
                    break;
            }
        }
    }

    private void addToInventoryStorage(CurrencyDelta currencyDelta, PlayerSession playerSession) {
        if (currencyDelta != null && currencyDelta.getCurrency() != null) {
            // they dont match so we log for now
            if (currencyDelta.getGivenDelta() != currencyDelta.getExpectedDelta()) {
                LOG.warn("expected to add " + currencyDelta.getCurrency() + " " +
                        currencyDelta.getExpectedDelta() + " but was given " +
                        currencyDelta.getGivenDelta() + " for player " + playerSession.getPlayerId());
            }

            InventoryStorage inventoryStorage = playerSession.getInventoryManager().getObjectForWriting();

            switch (currencyDelta.getCurrency()) {
                case credits:
                    inventoryStorage.credits.amount += currencyDelta.getGivenDelta();
                    break;
                case materials:
                    inventoryStorage.materials.amount += currencyDelta.getGivenDelta();
                    break;
                case contraband:
                    inventoryStorage.contraband.amount += currencyDelta.getGivenDelta();
                    break;
                case crystals:
                    inventoryStorage.crystals.amount += currencyDelta.getGivenDelta();
                    break;
            }
        }
    }

    /**
     * TODO - to get this working properly it needs to work as a contract where at the end of the contract
     * the crystals found are then added. For now we just add the number of crystals
     *
     * @param instanceId
     * @param credits
     * @param materials
     * @param contraband
     * @param crystals
     * @param time
     */
    @Override
    public void buildingClear(String instanceId, int credits, int materials, int contraband, int crystals, long time) {
        this.processCompletedContracts(time);
        MapItem mapItem = this.getMapItemByKey(instanceId);
        if (mapItem != null) {
            // create a contract to clear and process the cost
            CurrencyDelta currencyDelta = this.droidManager.clearMapItem(mapItem, credits, materials, contraband, time);
            this.processInventoryStorage(currencyDelta);
            this.savePlayerSession();
        }
    }

    @Override
    public void buildingConstruct(String buildingUid, String tag, Position position, int credits, int materials,
                                  int contraband, long time)
    {
        this.processCompletedContracts(time);
        MapItem mapItem = this.playerMapItems.createMapItem(buildingUid, tag, position);

        CurrencyType currencyType = CurrencyHelper.getCurrencyType(mapItem);
        if (currencyType == null)
            currencyType = CurrencyType.none;

        int expectedCost = CurrencyHelper.getConstructionCost(mapItem, currencyType);

        // add the building to the map
        this.playerMapItems.constructNewBuilding(mapItem);
        this.droidManager.constructBuildUnit(mapItem, tag, time, expectedCost);

        int givenTotal = CurrencyHelper.getGivenTotal(currencyType, credits, materials, contraband);
        int givenDelta = CurrencyHelper.calculateGivenConstructionCost(this, givenTotal, currencyType);
        CurrencyDelta currencyDelta = new CurrencyDelta(givenDelta, expectedCost, currencyType, true);
        this.processInventoryStorage(currencyDelta);
        savePlayerSession();
    }

    @Override
    public void processInventoryStorage(CurrencyDelta currencyDelta) {
        if (currencyDelta != null) {
            if (currencyDelta.getRemoveFromInventory())
                this.removeFromInventoryStorage(currencyDelta, this);
            else
                this.addToInventoryStorage(currencyDelta, this);
        }
    }

    @Override
    public void buildingUpgrade(String buildingId, String tag, int credits, int materials, int contraband, long time) {
        this.processCompletedContracts(time);
        MapItem mapItem = this.getMapItemByKey(buildingId);
        if (mapItem != null) {
            // if a resource we collect it first
            if (mapItem.getBuildingData().getType() == BuildingType.resource) {
                CurrencyDelta currencyDelta = mapItem.collect(this, credits, materials, contraband, time, false);
                this.processInventoryStorage(currencyDelta);
            }

            CurrencyDelta currencyDelta = this.droidManager.upgradeBuildUnit(mapItem, tag, credits,
                    materials, contraband, time);
            this.processInventoryStorage(currencyDelta);
            this.savePlayerSession();
        }
    }

    @Override
    public void buildingSwap(String buildingId, String buildingUid, int credits, int materials, int contraband, long time) {
        this.processCompletedContracts(time);
        MapItem mapItem = this.getMapItemByKey(buildingId);
        if (mapItem != null) {
            CurrencyDelta currencyDelta = this.droidManager.buildingSwap(mapItem, buildingUid, credits, materials,
                    contraband, time);
            this.processInventoryStorage(currencyDelta);
            this.savePlayerSession();
        }
    }

    @Override
    public void buildingUpgradeAll(String buildingUid, int credits, int materials, int contraband, int crystals, long time) {
        this.processCompletedContracts(time);
        List<MapItem> allMapItems = this.playerMapItems.getMapItemsByBuildingUid(buildingUid);
        for (MapItem mapItem : allMapItems) {
            this.droidManager.upgradeBuildUnit(mapItem, null, credits, materials, contraband, time);
            this.droidManager.buyout(mapItem.getBuildingKey(), crystals, time);
        }

        int givenDelta = CrystalHelper.calculateGivenCrystalDeltaToRemove(this, crystals);
        int costOfWall = allMapItems.get(0).getBuildingData().getMaterials();
        int expectedCost = CrystalHelper.crystalCostToUpgradeAllWalls(costOfWall, allMapItems.size());
        CurrencyDelta currencyDelta = new CurrencyDelta(givenDelta, expectedCost, CurrencyType.crystals, true);
        this.processInventoryStorage(currencyDelta);
        this.savePlayerSession();
    }

    @Override
    public void buildingInstantUpgrade(String buildingId, String tag, int credits, int materials, int contraband,
                                       int crystals, long time)
    {
        this.processCompletedContracts(time);
        MapItem mapItem = this.getMapItemByKey(buildingId);
        if (mapItem != null) {
            if (mapItem.getBuildingData().getType() == BuildingType.resource) {
                CurrencyDelta currencyDelta = mapItem.collect(this, credits, materials, contraband, time, false);
                this.processInventoryStorage(currencyDelta);
            }

            this.droidManager.upgradeBuildUnit(mapItem, tag, credits, materials, contraband, time);
            this.droidManager.buyout(buildingId, crystals, time);

            int givenDelta = CrystalHelper.calculateGivenCrystalDeltaToRemove(this, crystals);
            CurrencyType currencyType = CurrencyHelper.getCurrencyType(mapItem.getBuildingData());
            int upgradeCost = CurrencyHelper.getConstructionCost(mapItem.getBuildingData(), currencyType);
            int expectedCost = CrystalHelper.creditsCrystalCost(upgradeCost) +
                    CrystalHelper.secondsToCrystals(mapItem.getBuildingData().getTime(), mapItem.getBuildingData());
            CurrencyDelta currencyDelta = new CurrencyDelta(givenDelta, expectedCost, CurrencyType.crystals, true);
            this.processInventoryStorage(currencyDelta);
            this.savePlayerSession();
        }
    }

    @Override
    public void factionSet(FactionType faction, long time) {
        this.processCompletedContracts(time);

        // redo the map to the chosen faction to keep in sync
        FactionType oldFaction = this.getFaction();
        this.getPlayerSettings().setFaction(faction);
        for (MapItem mapItem : this.playerMapItems.getMapItems()) {
            BuildingData oldBuildingData = mapItem.getBuildingData();
            if (mapItem.getBuildingData().getFaction() == oldFaction) {
                BuildingData buildingData = ServiceFactory.instance().getGameDataManager()
                        .getFactionEquivalentOfBuilding(oldBuildingData, faction);
                mapItem.changeBuildingData(buildingData);
            }

            // if its a storage building we want to recalculate our inventory capacity
            if (mapItem instanceof StorageBuilding) {
                StorageBuilding storageBuilding = (StorageBuilding) mapItem;
                storageBuilding.factionSwitched(this);
            }
        }

        // add the first campaign and mission for this faction
        CampaignSet campaignSet = ServiceFactory.instance().getGameDataManager().getCampaignForFaction(faction);
        CampaignMissionSet campaignMissionSet = campaignSet.getCampaignMissionSet(1);
        PlayerCampaignMission playerCampaignMission = this.getPlayerSettings().getPlayerCampaignMission();
        CampaignMissionData campaignMissionData = campaignMissionSet.getMission(1);
        playerCampaignMission.addMission(campaignMissionData);
        this.savePlayerSession();
    }

    @Override
    public MapItem getMapItemByKey(String key) {
        return this.playerMapItems.getMapItemByKey(key);
    }

    @Override
    public MapItem removeMapItemByKey(String instanceId) {
        MapItem mapItem = this.getMapItemByKey(instanceId);
        if (mapItem != null) {
            this.playerMapItems.remove(mapItem);
        }

        return mapItem;
    }

    private void buildingMultimove(String key, Position newPosition) {
        MapItem mapItem = this.getMapItemByKey(key);
        if (mapItem != null)
            mapItem.moveTo(newPosition);
    }

    private void processCreature(Map<String, Integer> attackingUnitsKilled) {
        if (this.getCreatureManager().hasCreature() && attackingUnitsKilled != null) {
            String creatureUnitId = this.getCreatureManager().getCreatureUnitId();

            if (creatureUnitId != null && !creatureUnitId.isEmpty()) {
                TroopData troopData = this.troopInventory.getTroopByUnitId(creatureUnitId);
                if (troopData == null) {
                    LOG.error("Failed to lookup for player " + this.getPlayerId() + " for creature " + creatureUnitId);
                }

                if (troopData != null && attackingUnitsKilled.containsKey(troopData.getUid()))
                    this.getCreatureManager().getCreature().setCreatureStatus(CreatureStatus.Dead);
            }
        }
    }

    /**
     * TODO - Need to make this thread safe for squad support
     * @param troopUid
     * @param numberOf
     * @param fromPlayerId
     */
    private void addDonatedTroop(DonatedTroops donatedTroops, String troopUid, Integer numberOf, String fromPlayerId) {
        GuildDonatedTroops guildDonatedTroops = donatedTroops.get(troopUid);
        if (guildDonatedTroops == null) {
            guildDonatedTroops = new GuildDonatedTroops();
            donatedTroops.put(troopUid, guildDonatedTroops);
        }

        Integer troopsGivenByPlayer = guildDonatedTroops.get(fromPlayerId);
        if (troopsGivenByPlayer == null)
            troopsGivenByPlayer = new Integer(0);

        troopsGivenByPlayer = troopsGivenByPlayer + numberOf;
        guildDonatedTroops.put(fromPlayerId, troopsGivenByPlayer);
    }

    @Override
    public void rearm(List<String> buildingIds, int credits, int materials, int contraband, long time) {
        this.processCompletedContracts(time);
        int totalRearm = 0;
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        for (String buildingId : buildingIds) {
            MapItem mapItem = this.getMapItemByKey(buildingId);
            if (mapItem != null) {
                mapItem.getBuilding().currentStorage = 1;
                TrapData trapData = gameDataManager.getTrapDataByUid(mapItem.getBuildingData().getTrapId());
                totalRearm += trapData.getRearmMaterialsCost();
            }
        }

        int givenDelta = CurrencyHelper.calculateGivenConstructionCost(this, materials, CurrencyType.materials);
        CurrencyDelta currencyDelta = new CurrencyDelta(givenDelta, totalRearm, CurrencyType.materials, true);
        this.processInventoryStorage(currencyDelta);
        this.savePlayerSession();
    }

    @Override
    public PlayerMapItems getPlayerMapItems() {
        return this.playerMapItems;
    }

    @Override
    public void setCurrentQuest(String fueUid, long time) {
        this.getPlayerSettings().setCurrentQuest(fueUid);
        this.savePlayerSession();
    }

    @Override
    public void activateMission(String missionUid, long time) {
        PlayerCampaignMission playerCampaignMission = this.getPlayerSettings().getPlayerCampaignMission();
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        CampaignMissionData campaignMissionData = gameDataManager.getCampaignMissionData(missionUid);
        Mission mission = playerCampaignMission.addMission(campaignMissionData);

        if (mission != null)
            mission.status = MissionStatus.Active;

        this.savePlayerSession();
    }

    @Override
    public void claimCampaign(String campaignUid, String missionUid, long time) {
        PlayerCampaignMission playerCampaignMission = this.getPlayerSettings().getPlayerCampaignMission();
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        CampaignMissionData campaignMissionData = gameDataManager.getCampaignMissionData(missionUid);

        Mission mission = playerCampaignMission.missions.get(missionUid);
        if (mission != null)
            mission.status = MissionStatus.Claimed;

        // see if the campaign has been setup
        if (campaignMissionData != null) {
            Campaign campaign = playerCampaignMission.campaigns.get(campaignMissionData.getCampaignUid());
            if (campaign != null) {
                campaign.collected = true;
                campaign.completed = true;
            }
        }

        this.savePlayerSession();
    }

    @Override
    public void preferencesSet(Map<String,String> sharedPrefs) {
        Map<String,String> currentPrefs = this.getPlayerSettings().getSharedPreferences();
        currentPrefs.putAll(sharedPrefs);
        this.savePlayerSession();
    }

    @Override
    public void pveCollect(String missionUid, String battleUid, long time) {
        this.processCompletedContracts(time);
        PlayerCampaignMission playerCampaignMission = this.getPlayerSettings().getPlayerCampaignMission();
        playerCampaignMission.pveCollect(missionUid);
        this.savePlayerSession();
    }

    @Override
    public void missionsClaimMission(String missionUid, long time) {
        this.processCompletedContracts(time);
        PlayerCampaignMission playerCampaignMission = this.getPlayerSettings().getPlayerCampaignMission();
        playerCampaignMission.missionsClaimMission(missionUid);
        this.savePlayerSession();
    }

    @Override
    public void storeBuy(String uid, int count, int credits, int materials, int contraband, int crystals, long time) {
        this.processCompletedContracts(time);
        InventoryStorage inventoryStorage = this.getInventoryManager().getObjectForWriting();
        if (uid.equals("droids")) {
            inventoryStorage.droids.amount += count;
        }

        // TODO - do this properly, for now just going to accept what the client says
        inventoryStorage.credits.amount = credits;
        inventoryStorage.materials.amount = materials;
        inventoryStorage.contraband.amount = contraband;
        inventoryStorage.crystals.amount = crystals;

        this.savePlayerSession();
    }

    @Override
    public FactionType getFaction() {
        return this.getPlayerSettings().getFaction();
    }

    @Override
    public void setOffenseLab(OffenseLab offenseLab) {
        this.offenseLab = offenseLab;
    }

    @Override
    public void planetRelocate(String planet, boolean payWithHardCurrency, int crystals, long time) {
        this.processCompletedContracts(time);
        this.getPlayerSettings().getBaseMap().planet = planet;

        if (payWithHardCurrency) {
            int givenDelta = CrystalHelper.calculateGivenCrystalDeltaToRemove(this, crystals);
            if (givenDelta < 0) {
                LOG.warn("PlayerId " + this.getPlayerId() + " is relocating with a given delta that looks suspicious " + givenDelta);
            }

            CurrencyDelta currencyDelta = new CurrencyDelta(givenDelta, givenDelta,
                    CurrencyType.crystals, true);
            this.processInventoryStorage(currencyDelta);
        }

        this.savePlayerSession();
    }

    @Override
    public SquadMemberWarData getSquadMemberWarData(long time) {
        this.processCompletedContracts(time);
        GuildSession guildSession = this.getGuildSession();
        if (guildSession == null)
            return null;

        SquadMemberWarData squadMemberWarData = ServiceFactory.instance().getPlayerDatasource()
                .loadPlayerWarData(guildSession.getWarId(), this.getPlayerId());

        // put the base onto sullust
        if (squadMemberWarData != null) {
            squadMemberWarData.warMap.planet = "planet24";

            // TODO - level up buildings to what the player currently has although not sure if levelling up still should
            // happen once war starts???
            if (squadMemberWarData.id.equals(this.getPlayerId()))
                levelUpBase(squadMemberWarData.warMap);
        }

        return squadMemberWarData;
    }

    /**
     * This will modify the maps building to the same level as what the player currently has
     * @param warMap
     */
    @Override
    public void levelUpBase(PlayerMap warMap) {
        for (Building building : warMap.buildings) {
            MapItem mapItem = this.getPlayerMapItems().getMapItemByKey(building.key);
            if (mapItem != null)
                building.uid = mapItem.getBuildingUid();
        }
    }

    @Override
    public void warBaseSave(Map<String, Position> positions, String hqKey, long time) {
        this.processCompletedContracts(time);
        SquadMemberWarData squadMemberWarData = this.getSquadMemberWarData(time);

        Map<String, Building> buildingMap = new HashMap<>();
        squadMemberWarData.warMap.buildings.forEach(a -> buildingMap.put(a.key, a));

        for (Map.Entry<String, Position> buildingChange : positions.entrySet()) {
            Building building = buildingMap.get(buildingChange.getKey());
            if (building != null) {
                building.x = buildingChange.getValue().x;
                building.z = buildingChange.getValue().z;
            } else {
                Building newBuilding = new Building();
                newBuilding.key = buildingChange.getKey();
                newBuilding.x = buildingChange.getValue().x;
                newBuilding.z = buildingChange.getValue().z;
                MapItem mapItem = this.getPlayerMapItems().getMapItemByKey(newBuilding.key);
                newBuilding.uid = mapItem.getBuildingUid();
                squadMemberWarData.warMap.buildings.add(newBuilding);
            }
        }

        // TODO - probably should change when and where we work out the HQ level
        Building building = buildingMap.get(hqKey);
        int hqLevel = ServiceFactory.instance().getGameDataManager().getBuildingDataByUid(building.uid).getLevel();
        squadMemberWarData.level = hqLevel;
        ServiceFactory.instance().getPlayerDatasource().saveWarMap(this, squadMemberWarData);
    }

    @Override
    public PvpManager getPvpSession() {
        return pvpSession;
    }

    @Override
    public void pvpReleaseTarget() {
        getPvpSession().pvpReleaseTarget();
    }

    @Override
    public DBCacheObject<InventoryStorage> getInventoryManager() {
        return this.inventoryManager;
    }


    @Override
    public DBCacheObjectSaving<PvpAttack> getCurrentPvPAttack() {
        return this.pvPAttack;
    }

    @Override
    public ReadOnlyDBCacheObject<PvpAttack> getCurrentPvPDefence() {
        return currentPvPDefending;
    }

    @Override
    public void doneDBSave() {
        this.pvPAttack.doneDBSave();
        this.inventoryManager.doneDBSave();
        this.scalarsManager.doneDBSave();
        this.tournamentsManager.doneDBSave();
    }

    @Override
    public void playerPvPBattleStart(long time) {
        this.processCompletedContracts(time);
        ServiceFactory.instance().getPlayerDatasource().savePvPBattleStart(this);
    }

    @Override
    public DBCacheObject<Scalars> getScalarsManager() {
        return this.scalarsManager;
    }

    public DBCacheObject<List<TournamentStat>> getTournamentsManager() {
        return this.tournamentsManager;
    }

    @Override
    public DBCacheObjectRead<Map<String, Integer>> getDamagedBuildingManager() {
        return this.damagedBuildingManager;
    }

    @Override
    public DBCacheObject<List<TournamentStat>> getTournamentManager() {
        return this.tournamentsManager;
    }
}
