package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.Player;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.model.*;

import java.util.*;

public class PlayerSessionImpl implements PlayerSession {
    final private Player player;
    final private PlayerSettings playerSettings;
    final private ContractManager contractManager;

    static final private ContractManagerLoader contractManagerLoader = new ContractManagerLoader();

    public PlayerSessionImpl(Player player, PlayerSettings playerSettings) {
        this.player = player;
        this.playerSettings = playerSettings;
        this.contractManager = this.contractManagerLoader.createForPlayer(this.getPlayerSettings());
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
        this.contractManager.trainTroops(buildingId, unitTypeId, quantity, startTime);
    }

    @Override
    public void cancelTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        this.contractManager.cancelTrainTroops(buildingId, unitTypeId, quantity, time);
    }

    @Override
    public void buyOutTrainTroops(String buildingId, String unitTypeId, int quantity, long time) {
        this.contractManager.buyOutTrainTroops(buildingId, unitTypeId, quantity, time);
    }

    /**
     * Removing completed contracts during base building
     * @param deployablesToRemove
     * @param time
     */
    @Override
    public void removeDeployedTroops(Map<String, Integer> deployablesToRemove, long time) {
        if (deployablesToRemove != null) {
            this.contractManager.removeDeployedTroops(deployablesToRemove);
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
            this.contractManager.removeDeployedTroops(deployablesToRemove);
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
        this.contractManager.moveCompletedTroops(time);
        saveSession();
    }

    private void saveSession() {
        ServiceFactory.instance().getPlayerDatasource().savePlayerSession(this);
    }

    @Override
    public void onboardTransports(long time) {
        this.contractManager.moveCompletedTroops(time);
    }

    @Override
    public PlayerMap getBaseMap() {
        return playerSettings.getBaseMap();
    }

    @Override
    public ContractManager getContractManager() {
        return contractManager;
    }

    @Override
    public PlayerSettings getPlayerSettings() {
        return playerSettings;
    }
}
