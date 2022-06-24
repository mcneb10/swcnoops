package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.Player;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.model.*;
import swcnoops.server.session.training.TrainingManager;
import swcnoops.server.session.training.TrainingManagerFactory;

import java.util.*;

public class PlayerSessionImpl implements PlayerSession {
    final private Player player;
    final private PlayerSettings playerSettings;
    final private TrainingManager trainingManager;

    static final private TrainingManagerFactory trainingManagerFactory = new TrainingManagerFactory();

    public PlayerSessionImpl(Player player, PlayerSettings playerSettings) {
        this.player = player;
        this.playerSettings = playerSettings;
        this.trainingManager = this.trainingManagerFactory.createForPlayer(this.getPlayerSettings());
    }

    @Override
    public String getPlayerId() {
        return this.player.getPlayerId();
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void trainTroops(String buildingId, String unitTypeId, int quantity, long startTime) {
        this.trainingManager.trainTroops(buildingId, unitTypeId, quantity, startTime);
        saveSession();
    }

    @Override
    public void cancelTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        this.trainingManager.cancelTrainTroops(buildingId, unitTypeId, quantity, time);
        saveSession();
    }

    @Override
    public void buyOutTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        this.trainingManager.buyOutTrainTroops(buildingId, unitTypeId, quantity, time);
        saveSession();
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
            saveSession();
        }
    }

    /**
     * This is removal during a battle to remove troops that were used.
     * TODO - CreatureDeployed
     * @param deployablesToRemove
     * @param time
     */
    @Override
    public void removeDeployedTroops(List<DeploymentRecord> deployablesToRemove, long time) {
        if (deployablesToRemove != null) {
            this.trainingManager.removeDeployedTroops(deployablesToRemove);
            saveSession();
        }
    }

    /**
     * Before a battle we move all completed troops to their transport, as those are the troops going to war.
     * We do this as during the battle deployment records are sent which we used to remove what are in the
     * transports.
     * @param time
     */
    @Override
    public void playerBattleStart(long time) {
        this.trainingManager.moveCompletedBuildUnits(time);
        saveSession();
    }

    private void saveSession() {
        ServiceFactory.instance().getPlayerDatasource().savePlayerSession(this);
    }

    @Override
    public void processCompletedContracts(long time) {
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
    public void captureCreature(String instanceId, String creatureTroopUid, long time) {
        // TODO -
    }
}
