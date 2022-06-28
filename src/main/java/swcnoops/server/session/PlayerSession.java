package swcnoops.server.session;

import swcnoops.server.datasource.Player;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.model.DeploymentRecord;
import swcnoops.server.model.PlayerMap;
import swcnoops.server.session.creature.CreatureManager;
import swcnoops.server.session.inventory.TroopInventory;
import swcnoops.server.session.training.TrainingManager;

import java.util.List;
import java.util.Map;

public interface PlayerSession {
    Player getPlayer();

    String getPlayerId();

    TroopInventory getTroopInventory();

    void trainTroops(String constructor, String unitTypeId, int quantity, long time);

    void cancelTrainTroops(String constructor, String unitTypeId, int quantity, long time);

    void buyOutTrainTroops(String constructor, String unitTypeId, int quantity, long time);

    void removeDeployedTroops(Map<String, Integer> deployablesToRemove, long time);
    void removeDeployedTroops(List<DeploymentRecord> deployablesToRemove, long time);

    void playerBattleStart(long time);

    PlayerMap getBaseMap();

    TrainingManager getTrainingManager();
    PlayerSettings getPlayerSettings();

    void recaptureCreature(String instanceId, String creatureTroopUid, long time);

    CreatureManager getCreatureManager();

    void buildingBuyout(String instanceId, String tag, long time);

    void deployableUpgradeStart(String buildingId, String troopUid, long time);

    void playerLogin(long time);

    void buildingCancel(String buildingId, String tag, long time);

    void troopsRequest(boolean payToSkip, String message, long time);

    boolean isInGuild(String guildId);

    void setGuildSession(GuildSession guildSession);
}
