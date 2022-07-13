package swcnoops.server.session;

import swcnoops.server.datasource.Player;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.model.*;
import swcnoops.server.session.map.MoveableMapItem;
import swcnoops.server.session.creature.CreatureManager;
import swcnoops.server.session.inventory.TroopInventory;
import swcnoops.server.session.training.TrainingManager;

import java.util.List;
import java.util.Map;

public interface PlayerSession {
    Player getPlayer();

    MoveableMapItem getHeadQuarter();
    MoveableMapItem getSquadBuilding();

    String getPlayerId();

    TroopInventory getTroopInventory();

    void trainTroops(String constructor, String unitTypeId, int quantity, long time);

    void cancelTrainTroops(String constructor, String unitTypeId, int quantity, long time);

    void buyOutTrainTroops(String constructor, String unitTypeId, int quantity, long time);

    void removeDeployedTroops(Map<String, Integer> deployablesToRemove, long time);
    void removeDeployedTroops(List<DeploymentRecord> deployablesToRemove, long time);

    String playerBattleStart(String missionUid, long time);

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

    GuildSession getGuildSession();

    DonatedTroops getDonatedTroops();

    void processDonatedTroops(Map<String, Integer> troopsDonated, String playerId);

    int getDonatedTroopsTotalUnits();

    void battleComplete(String battleId, int stars, Map<String, Integer> attackingUnitsKilled, long time);

    void buildingMultimove(PositionMap positions, long time);

    MoveableMapItem getMapItemByKey(String key);

    void buildingCollect(String buildingId, long time);

    void buildingClear(String instanceId, long time);

    void buildingConstruct(String buildingUid, Position position, long time);

    void buildingUpgrade(String buildingId, long time);

    void factionSet(FactionType faction, long time);

    DroidManager getDroidManager();

    void rearm(List<String> buildingIds, long time);

    PlayerMapItems getPlayerMapItems();

    void setCurrentQuest(String fueUid, long time);

    void activateMission(String missionUid, long time);

    void claimCampaign(String campaignUid, String missionUid, long time);

    void preferencesSet(Map<String,String> sharedPrefs);

    void pveCollect(String missionUid, String battleUid, long time);

    void missionsClaimMission(String missionUid, long time);

    void savePlayerSession();

    MoveableMapItem removeMapItemByKey(String instanceId);

    void buildingInstantUpgrade(String instanceId, long time);
    
    void storeBuy(String uid, int count, long time);

    void buildingUpgradeAll(String buildingUid, long time);
}
