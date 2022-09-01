package swcnoops.server.session;

import swcnoops.server.commands.guild.TroopDonationResult;
import swcnoops.server.datasource.Player;
import swcnoops.server.datasource.PlayerSettings;
import swcnoops.server.model.*;
import swcnoops.server.session.map.MapItem;
import swcnoops.server.session.creature.CreatureManager;
import swcnoops.server.session.inventory.TroopInventory;
import swcnoops.server.session.research.OffenseLab;
import swcnoops.server.session.training.TrainingManager;

import java.util.List;
import java.util.Map;

public interface PlayerSession {
    Player getPlayer();

    MapItem getHeadQuarter();
    MapItem getSquadBuilding();

    String getPlayerId();

    TroopInventory getTroopInventory();

    void trainTroops(String constructor, String unitTypeId, int quantity, int credits, int contraband, long time);

    void cancelTrainTroops(String constructor, String unitTypeId, int quantity, int credits, int materials, int contraband, long time);

    void buyOutTrainTroops(String constructor, String unitTypeId, int quantity, int crystals, long time);

    void removeDeployedTroops(Map<String, Integer> deployablesToRemove, long time);
    void removeDeployedTroops(List<DeploymentRecord> deployablesToRemove, long time);

    String playerBattleStart(String missionUid, long time);

    TrainingManager getTrainingManager();
    PlayerSettings getPlayerSettings();

    void recaptureCreature(String instanceId, String creatureTroopUid, long time);

    CreatureManager getCreatureManager();

    void buildingBuyout(String instanceId, String tag, int credits, int materials, int contraband, int crystals, long time);

    void deployableUpgradeStart(String buildingId, String troopUid, long time);

    void playerLogin(long time);

    void buildingCancel(String buildingId, String tag, int credits, int materials, int contraband, long time);

    SquadNotification troopsRequest(DonatedTroops donatedTroops, String warId, boolean payToSkip, String message, long time);

    boolean isInGuild(String guildId);

    void setGuildSession(GuildSession guildSession);

    GuildSession getGuildSession();

    DonatedTroops getDonatedTroops();

    boolean processDonatedTroops(Map<String, Integer> troopsDonated, String playerId, DonatedTroops troopsInSC);

    int getDonatedTroopsTotalUnits(DonatedTroops donatedTroops);

    void battleComplete(String battleId, int stars, Map<String, Integer> attackingUnitsKilled, long time);

    void buildingMultimove(PositionMap positions, long time);

    MapItem getMapItemByKey(String key);

    void buildingCollect(String buildingId, int credits, int materials, int contraband, int crystals, long time);

    void buildingClear(String instanceId, long time);

    void buildingConstruct(String buildingUid, String tag, Position position, int credits, int materials, int contraband, long time);

    void buildingUpgrade(String buildingId, String tag, int credits, int materials, int contraband, long time);

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

    void savePlayerSession(SquadNotification squadNotification);

    MapItem removeMapItemByKey(String instanceId);

    void buildingInstantUpgrade(String instanceId, String tag, int credits, int materials, int contraband, int crystals, long time);
    
    void storeBuy(String uid, int count, long time);

    void buildingUpgradeAll(String buildingUid, int credits, int materials, int contraband, int crystals, long time);

    void buildingSwap(String buildingId, String buildingUid, long time);

    FactionType getFaction();

    void setOffenseLab(OffenseLab offenseLab);

    void planetRelocate(String planet, boolean payWithHardCurrency, long time);

    TroopDonationResult troopsDonate(Map<String, Integer> troopsDonated, String requestId, String recipientId, boolean forWar, long time);

    boolean setLastNotificationSince(String guildId, long since);

    boolean hasNotificationsToSend();

    long getNotificationsSince();

    List<SquadNotification> getNotifications(long notificationsSince);

    void addSquadNotification(SquadNotification leaveNotification);

    void removeEjectedNotifications(List<SquadNotification> ejectedNotifications);

    SquadMemberWarData getSquadMemberWarData(long time);

    void warBaseSave(Map<String, Position> positions, long time);

    void levelUpBase(PlayerMap warMap);
}
