package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.Player;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.model.*;
import swcnoops.server.session.creature.CreatureManager;
import swcnoops.server.session.creature.CreatureManagerFactory;
import swcnoops.server.session.inventory.TroopInventory;
import swcnoops.server.session.inventory.TroopInventoryFactory;
import swcnoops.server.session.research.OffenseLab;
import swcnoops.server.session.research.OffenseLabFactory;
import swcnoops.server.session.training.TrainingManager;
import swcnoops.server.session.training.TrainingManagerFactory;

import java.util.*;

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

    private GuildSession guildSession;

    static final private TrainingManagerFactory trainingManagerFactory = new TrainingManagerFactory();
    static final private CreatureManagerFactory creatureManagerFactory = new CreatureManagerFactory();
    static final private TroopInventoryFactory troopInventoryFactory = new TroopInventoryFactory();
    static final private OffenseLabFactory offenseLabFactory = new OffenseLabFactory();

    public PlayerSessionImpl(Player player, PlayerSettings playerSettings) {
        this.player = player;
        this.playerSettings = playerSettings;
        this.troopInventory = PlayerSessionImpl.troopInventoryFactory.createForPlayer(this);
        this.trainingManager = PlayerSessionImpl.trainingManagerFactory.createForPlayer(this);
        this.creatureManager = PlayerSessionImpl.creatureManagerFactory.createForPlayer(this);
        this.offenseLab = PlayerSessionImpl.offenseLabFactory.createForPlayer(this);
        this.donatedTroops = playerSettings.getDonatedTroops();
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
        this.trainingManager.cancelTrainTroops(buildingId, unitTypeId, quantity, time);
        this.processCompletedContracts(time);
        savePlayerSession();
    }

    @Override
    public void buyOutTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        // we move completed troops last because its possible they managed to buy it out
        // while we think it had completed and already moved to be a deployable
        this.trainingManager.buyOutTrainTroops(buildingId, unitTypeId, quantity, time);
        this.processCompletedContracts(time);
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
        if (this.offenseLab.processCompletedUpgrades(time))
            this.trainingManager.recalculateContracts(time);
        this.trainingManager.moveCompletedBuildUnits(time);
    }

    @Override
    public PlayerMap getBaseMap() {
        return playerSettings.getBaseMap();
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
        if (this.creatureManager.hasCreature() && this.creatureManager.getBuildingId().equals(buildingId)) {
            this.creatureManager.buyout(time);
            this.savePlayerSession();
        } else if (this.offenseLab.getBuildingId().equals(buildingId)) {
            this.offenseLab.buyout(time);
            this.trainingManager.recalculateContracts(time);
            this.savePlayerSession();
        }
    }

    @Override
    public void buildingCancel(String buildingId, String tag, long time) {
        this.processCompletedContracts(time);
        if (this.offenseLab.getBuildingId().equals(buildingId)) {
            this.offenseLab.cancel(time);
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
}
