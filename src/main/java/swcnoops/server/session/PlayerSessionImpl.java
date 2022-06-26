package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.Player;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.model.*;
import swcnoops.server.session.creature.CreatureManager;
import swcnoops.server.session.creature.CreatureManagerFactory;
import swcnoops.server.session.inventory.TroopInventory;
import swcnoops.server.session.inventory.TroopInventoryFactory;
import swcnoops.server.session.training.TrainingManager;
import swcnoops.server.session.training.TrainingManagerFactory;

import java.util.*;

public class PlayerSessionImpl implements PlayerSession {
    final private Player player;
    final private PlayerSettings playerSettings;
    final private TrainingManager trainingManager;
    final private CreatureManager creatureManager;
    final private TroopInventory troopInventory;

    static final private TrainingManagerFactory trainingManagerFactory = new TrainingManagerFactory();
    static final private CreatureManagerFactory creatureManagerFactory = new CreatureManagerFactory();
    static final private TroopInventoryFactory troopInventoryFactory = new TroopInventoryFactory();

    public PlayerSessionImpl(Player player, PlayerSettings playerSettings) {
        this.player = player;
        this.playerSettings = playerSettings;
        this.troopInventory = PlayerSessionImpl.troopInventoryFactory.createForPlayer(this);
        this.trainingManager = PlayerSessionImpl.trainingManagerFactory.createForPlayer(this);
        this.creatureManager = PlayerSessionImpl.creatureManagerFactory.createForPlayer(this);
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
        this.trainingManager.trainTroops(buildingId, unitTypeId, quantity, startTime);
        savePlayerSession();
    }

    @Override
    public void cancelTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        this.trainingManager.cancelTrainTroops(buildingId, unitTypeId, quantity, time);
        savePlayerSession();
    }

    @Override
    public void buyOutTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
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
        this.troopInventory.processCompletedUpgrades(time);
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
    public void buildingBuyout(String instanceId, String tag, long time) {
        if (this.creatureManager.hasCreature() && this.creatureManager.getBuildingKey().equals(instanceId)) {
            this.creatureManager.creatureBuyout();
            this.savePlayerSession();
        }
    }

    @Override
    public CreatureManager getCreatureManager() {
        return creatureManager;
    }

    @Override
    public void deployableUpgradeStart(String buildingId, String troopUid, long time) {
        this.troopInventory.upgradeStart(buildingId, troopUid, time);
        this.savePlayerSession();
    }

    @Override
    public void playerLogin(long time) {
        this.processCompletedContracts(ServiceFactory.getSystemTimeSecondsFromEpoch());
        this.savePlayerSession();
    }
}
