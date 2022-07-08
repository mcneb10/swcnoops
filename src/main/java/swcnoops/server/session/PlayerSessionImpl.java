package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.Player;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.game.BuildingData;
import swcnoops.server.game.BuildingType;
import swcnoops.server.game.ContractType;
import swcnoops.server.game.GameDataManager;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This represents the actions/commands that the game client can do for a player.
 * There should be no player state processing in the commands themselves, all those should be is mapping
 * to the response. Player State changes should be done in classes of package sessions.
 */
public class PlayerSessionImpl implements PlayerSession {
    final private Player player;
    final private PlayerSettings playerSettings;
    final private TrainingManager trainingManager;
    final private CreatureManager creatureManager;
    final private TroopInventory troopInventory;
    final private OffenseLab offenseLab;
    final private DonatedTroops donatedTroops;
    final private InventoryStorage inventoryStorage;
    final private PlayerMapItems playerMapItems;

    private GuildSession guildSession;
    private DroidManager droidManager;

    static final private TrainingManagerFactory trainingManagerFactory = new TrainingManagerFactory();
    static final private CreatureManagerFactory creatureManagerFactory = new CreatureManagerFactory();
    static final private TroopInventoryFactory troopInventoryFactory = new TroopInventoryFactory();
    static final private OffenseLabFactory offenseLabFactory = new OffenseLabFactory();

    public PlayerSessionImpl(Player player, PlayerSettings playerSettings) {
        this.player = player;
        this.playerSettings = playerSettings;

        // TODO - change this to use a method once refactor is done
        this.playerMapItems = createPlayersMap(playerSettings.baseMap);

        this.troopInventory = PlayerSessionImpl.troopInventoryFactory.createForPlayer(this);
        this.trainingManager = PlayerSessionImpl.trainingManagerFactory.createForPlayer(this);
        this.creatureManager = PlayerSessionImpl.creatureManagerFactory.createForPlayer(this);
        this.offenseLab = PlayerSessionImpl.offenseLabFactory.createForPlayer(this);
        this.donatedTroops = playerSettings.getDonatedTroops();
        this.inventoryStorage = playerSettings.getInventoryStorage();
        this.droidManager = new DroidManager(this);
        mapBuildingContracts(playerSettings);
    }

    private void mapBuildingContracts(PlayerSettings playerSettings) {
        for (BuildUnit buildUnit : playerSettings.getBuildContracts()) {
            if (isBuilding(buildUnit.getContractType()))
                this.droidManager.addBuildUnit(buildUnit);
        }
    }

    private boolean isBuilding(ContractType contractType) {
        if (contractType == ContractType.Build)
            return true;
        if (contractType == ContractType.Upgrade)
            return true;

        return false;
    }

    private PlayerMapItems createPlayersMap(PlayerMap baseMap) {
        PlayerMapItems playerMapItems = new PlayerMapItems(baseMap);

        if (baseMap != null) {
            for (Building building : baseMap.buildings) {
                BuildingData buildingData = ServiceFactory.instance().getGameDataManager().getBuildingDataByUid(building.uid);
                if (buildingData != null) {
                    MoveableMapItem mapItem = new MoveableBuilding(building, buildingData);
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
    public MoveableMapItem getSquadBuilding() {
        return this.playerMapItems.getMapItemByType(BuildingType.squad);
    }

    @Override
    public MoveableMapItem getHeadQuarter() {
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
    public void trainTroops(String buildingId, String unitTypeId, int quantity, long startTime) {
        this.processCompletedContracts(startTime);
        this.trainingManager.trainTroops(buildingId, unitTypeId, quantity, startTime);
        savePlayerSession();
    }

    @Override
    public void cancelTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        this.processCompletedContracts(time);
        this.trainingManager.cancelTrainTroops(buildingId, unitTypeId, quantity, time);
        savePlayerSession();
    }

    @Override
    public void buyOutTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        this.processCompletedContracts(time);
        this.trainingManager.buyOutTrainTroops(buildingId, unitTypeId, quantity, time);
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
            this.trainingManager.removeDeployedTroops(deployablesToRemove);

            if (deployablesToRemove.stream().filter(a -> a.getAction().equals("SquadTroopPlaced")).count() > 0)
                this.getDonatedTroops().clear();

            this.savePlayerSession();
        }
    }

    /**
     * Before a battle we move all completed troops to their transport, as those are the troops going to war.
     * We do this as during the battle, deployment records are sent which we use to remove from deployables
     * @param time
     */
    @Override
    public void playerBattleStart(long time) {
        this.processCompletedContracts(time);
        this.savePlayerSession();
    }

    private void savePlayerSession() {
        ServiceFactory.instance().getPlayerDatasource().savePlayerSession(this);
    }

    private void processCompletedContracts(long time) {
        if (this.offenseLab != null && this.offenseLab.processCompletedUpgrades(time))
            this.trainingManager.recalculateContracts(time);
        this.trainingManager.moveCompletedBuildUnits(time);
        this.droidManager.moveCompletedBuildUnits(time);
    }

    @Override
    public TrainingManager getTrainingManager() {
        return trainingManager;
    }

    @Override
    public PlayerSettings getPlayerSettings() {
        return playerSettings;
    }

    @Override
    public void recaptureCreature(String instanceId, String creatureTroopUid, long time) {
        this.creatureManager.recaptureCreature(creatureTroopUid, time);
        this.savePlayerSession();
    }

    @Override
    public void buildingBuyout(String buildingId, String tag, long time) {
        this.processCompletedContracts(time);
        if (this.creatureManager.hasCreature() && this.creatureManager.getBuildingKey().equals(buildingId)) {
            this.creatureManager.buyout(time);
            this.savePlayerSession();
        } else if (this.offenseLab != null && this.offenseLab.getBuildingKey().equals(buildingId)) {
            if (offenseLab.isResearchingTroop()) {
                this.offenseLab.buyout(time);
                this.trainingManager.recalculateContracts(time);
            } else {
                this.droidManager.buyout(buildingId, time);
            }

            this.savePlayerSession();
        } else {
            this.droidManager.buyout(buildingId, time);
            this.savePlayerSession();
        }
    }

    @Override
    public void buildingCancel(String buildingId, String tag, long time) {
        this.processCompletedContracts(time);
        if (this.offenseLab.getBuildingKey().equals(buildingId)) {
            if (offenseLab.isResearchingTroop()) {
                this.offenseLab.cancel(time);
            } else {
                this.droidManager.cancel(buildingId);
            }
            this.savePlayerSession();
        } else {
            this.droidManager.cancel(buildingId);
            this.savePlayerSession();
        }
    }

    @Override
    public CreatureManager getCreatureManager() {
        return creatureManager;
    }

    @Override
    public void deployableUpgradeStart(String buildingId, String troopUid, long time) {
        this.processCompletedContracts(time);
        this.offenseLab.upgradeStart(buildingId, troopUid, time);
        this.savePlayerSession();
    }

    @Override
    public void playerLogin(long time) {
        this.processCompletedContracts(ServiceFactory.getSystemTimeSecondsFromEpoch());
        this.savePlayerSession();
    }

    @Override
    public void troopsRequest(boolean payToSkip, String message, long time) {
        // TODO - this will need to forward a message to the squad for requests
        // it will be something along the lines of putting a command onto a queue for the guild
        // every player request that can send messages in the response, checks that guild queue to create
        // that message type. For now we do nothing as not supporting multiple players and squads yet.
        this.getGuildSession().troopsRequest(this.getPlayerId(), message, time);
    }

    @Override
    public boolean isInGuild(String guildId) {
        if (this.guildSession != null) {
            GuildSession guild = this.guildSession;
            return guildId.equals(guild.getGuildId());
        }

        return false;
    }

    public GuildSession getGuildSession() {
        return guildSession;
    }

    @Override
    public void setGuildSession(GuildSession guildSession) {
        this.guildSession = guildSession;
    }

    @Override
    public DonatedTroops getDonatedTroops() {
        return donatedTroops;
    }

    @Override
    public void processDonatedTroops(Map<String, Integer> troopsDonated, String playerId) {
        troopsDonated.forEach((a,b) -> addDonatedTroop(a,b,playerId));
    }

    @Override
    public int getDonatedTroopsTotalUnits() {
        AtomicInteger totalUnits = new AtomicInteger(0);
        GameDataManager gameDataManager = ServiceFactory.instance().getGameDataManager();
        this.getDonatedTroops()
                .forEach((a,b) -> b.values().forEach(v -> totalUnits.addAndGet(gameDataManager.getTroopDataByUid(a).getSize() * v)));
        return totalUnits.get();
    }

    @Override
    public void pvpBattleComplete(Map<String, Integer> attackingUnitsKilled, long time) {
        processCreature(attackingUnitsKilled);
        Map<String,Integer> killedChampions = this.getTrainingManager().remapTroopUidToUnitId(attackingUnitsKilled);
        this.getTrainingManager().getDeployableChampion().removeDeployable(killedChampions);
        this.savePlayerSession();
    }

    @Override
    public void buildingMultimove(PositionMap positions, long time) {
        positions.forEach((a,b) -> buildingMultimove(a,b));
        this.savePlayerSession();
    }

    @Override
    public void buildingCollect(String buildingId, long time) {
        MoveableMapItem moveableMapItem = this.getMapItemByKey(buildingId);
        if (moveableMapItem != null) {
            moveableMapItem.collect(time);
            this.savePlayerSession();
        }
    }

    @Override
    public void buildingConstruct(String buildingUid, Position position, long time) {
        this.processCompletedContracts(time);
        MoveableMapItem moveableMapItem = this.playerMapItems.createMovableMapItem(buildingUid, position);

        // add the building to the map
        this.playerMapItems.constructNewBuilding(moveableMapItem);
        this.droidManager.constructBuildUnit(moveableMapItem, time);
        savePlayerSession();
    }

    @Override
    public void buildingUpgrade(String buildingId, long time) {
        this.processCompletedContracts(time);
        MoveableMapItem moveableMapItem = this.getMapItemByKey(buildingId);
        if (moveableMapItem != null) {
            this.droidManager.upgradeBuildUnit(moveableMapItem, time);
            this.savePlayerSession();
        }
    }

    @Override
    public void factionSet(FactionType faction, long time) {
        this.processCompletedContracts(time);
        FactionType oldFaction = this.playerSettings.getFaction();
        this.playerSettings.setFaction(faction);
        for (MoveableMapItem moveableMapItem : this.playerMapItems.getMapItems()) {
            BuildingData oldBuildingData = moveableMapItem.getBuildingData();
            if (moveableMapItem.getBuildingData().getFaction() == oldFaction) {
                BuildingData buildingData = ServiceFactory.instance().getGameDataManager()
                        .getBuildingData(oldBuildingData.getType(), faction, oldBuildingData.getLevel());

                moveableMapItem.changeBuildingData(buildingData);
            }
        }

        this.savePlayerSession();
    }

    @Override
    public MoveableMapItem getMapItemByKey(String key) {
        return this.playerMapItems.getMapItemByKey(key);
    }

    private void buildingMultimove(String key, Position newPosition) {
        MoveableMapItem moveableMapItem = this.getMapItemByKey(key);
        if (moveableMapItem != null)
            moveableMapItem.moveTo(newPosition);
    }

    private void processCreature(Map<String, Integer> attackingUnitsKilled) {
        if (this.getCreatureManager().hasCreature()) {
            if (attackingUnitsKilled.containsKey(this.getCreatureManager().getCreatureUid()))
                this.getCreatureManager().getCreature().setCreatureStatus(CreatureStatus.Dead);
        }
    }

    /**
     * TODO - Need to make this thread safe for squad support
     * @param troopUid
     * @param numberOf
     * @param fromPlayerId
     */
    private void addDonatedTroop(String troopUid, Integer numberOf, String fromPlayerId) {
        GuildDonatedTroops guildDonatedTroops = this.donatedTroops.get(troopUid);
        if (guildDonatedTroops == null) {
            guildDonatedTroops = new GuildDonatedTroops();
            this.donatedTroops.put(troopUid, guildDonatedTroops);
        }

        Integer troopsGivenByPlayer = guildDonatedTroops.get(fromPlayerId);
        if (troopsGivenByPlayer == null)
            troopsGivenByPlayer = new Integer(0);

        troopsGivenByPlayer = troopsGivenByPlayer + numberOf;
        guildDonatedTroops.put(fromPlayerId, troopsGivenByPlayer);
    }

    @Override
    public void rearm(List<String> buildingIds, long time) {
        this.processCompletedContracts(time);
        for (String buildingId : buildingIds) {
            MoveableMapItem moveableMapItem = this.getMapItemByKey(buildingId);
            if (moveableMapItem != null) {
                moveableMapItem.getBuilding().currentStorage = 1;
            }
        }

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
}
