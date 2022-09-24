package swcnoops.server.session;

import swcnoops.server.commands.guild.TroopDonationResult;
import swcnoops.server.datasource.*;
import swcnoops.server.datasource.Player;
import swcnoops.server.game.PvpMatch;
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

    String playerPveBattleStart(String missionUid, long time);

    TrainingManager getTrainingManager();
    PlayerSettings getPlayerSettings();

    void recaptureCreature(String instanceId, String creatureTroopUid, long time);

    CreatureManager getCreatureManager();

    void buildingBuyout(String instanceId, String tag, int credits, int materials, int contraband, int crystals, long time);

    void deployableUpgradeStart(String buildingId, String troopUid, int credits, int materials, int contraband, long time);

    void playerLogin(long time);

    void buildingCancel(String buildingId, String tag, int credits, int materials, int contraband, long time);

    SquadNotification troopsRequest(DonatedTroops donatedTroops, String warId, boolean payToSkip, String message, long time);

    boolean isInGuild(String guildId);

    void setGuildSession(GuildSession guildSession);

    GuildSession getGuildSession();

    DonatedTroops getDonatedTroops();

    boolean processDonatedTroops(Map<String, Integer> troopsDonated, String playerId, DonatedTroops troopsInSC);

    int getDonatedTroopsTotalUnits(DonatedTroops donatedTroops);

    void warBattleComplete(String battleId, int stars, Map<String, Integer> attackingUnitsKilled, long time);

    void buildingMultimove(PositionMap positions, long time);

    MapItem getMapItemByKey(String key);

    void buildingCollect(String buildingId, int credits, int materials, int contraband, long time);

    void buildingClear(String instanceId, int credits, int materials, int contraband, int crystals, long time);

    void buildingConstruct(String buildingUid, String tag, Position position, int credits, int materials, int contraband, long time);

    void buildingUpgrade(String buildingId, String tag, int credits, int materials, int contraband, long time);

    void factionSet(FactionType faction, long time);

    DroidManager getDroidManager();

    void rearm(List<String> buildingIds, int credits, int materials, int contraband, long time);

    PlayerMapItems getPlayerMapItems();

    void setCurrentQuest(String fueUid, long time);

    void activateMission(String missionUid, long time);

    void claimCampaign(String campaignUid, String missionUid, long time);

    void preferencesSet(Map<String,String> sharedPrefs);

    void pveCollect(String missionUid, String battleUid, long time);

    void missionsClaimMission(String missionUid, long time);

    void savePlayerSession();

    MapItem removeMapItemByKey(String instanceId);

    void buildingInstantUpgrade(String instanceId, String tag, int credits, int materials, int contraband, int crystals, long time);

    void storeBuy(String uid, int count, int credits, int materials, int contraband, int crystals, long time);

    void buildingUpgradeAll(String buildingUid, int credits, int materials, int contraband, int crystals, long time);

    void buildingSwap(String buildingId, String buildingUid, int credits, int materials, int contraband, long time);

    FactionType getFaction();

    void setOffenseLab(OffenseLab offenseLab);

    void planetRelocate(String planet, boolean payWithHardCurrency, int crystals, long time);

    TroopDonationResult troopsDonate(Map<String, Integer> troopsDonated, String requestId, String recipientId, boolean forWar, long time);

    boolean setLastNotificationSince(String guildId, long since);

    boolean hasNotificationsToSend();

    long getNotificationsSince();

    List<SquadNotification> getNotifications(long notificationsSince);

    void addSquadNotification(SquadNotification leaveNotification);

    void removeEjectedNotifications(List<SquadNotification> ejectedNotifications);

    SquadMemberWarData getSquadMemberWarData(long time);

    void warBaseSave(Map<String, Position> positions, String buildingKey, long time);

    void levelUpBase(PlayerMap warMap);

    void processInventoryStorage(CurrencyDelta currencyDelta);

    void buildingCollectAll(List<String> buildingIds, int credits, int materials, int contraband, long time);

    PvpManager getPvpSession();

    void savePlayerKeepAlive();

    void savePlayerName(String playerName);

    void recoverWithPlayerSettings(PlayerModel playerModel, Map<String, String> sharedPrefs);

    void initialise(Player player);

    void pveBattleComplete(String battleId, int stars, Map<String, Integer> attackingUnitsKilled, long time);

    void pvpBattleComplete(BattleReplay battleReplay, Map<String, Integer> attackingUnitsKilled, PvpMatch pvpMatch, long time);

    void pvpReleaseTarget();

    DBCacheObject<InventoryStorage> getInventoryManager();

    DBCacheObjectSaving<PvpAttack> getCurrentPvPAttack();
    ReadOnlyDBCacheObject<PvpAttack> getCurrentPvPDefence();

    void doneDBSave();

    void playerPvPBattleStart(long time);

    DBCacheObject<Scalars> getScalarsManager();

    DBCacheObjectRead<Map<String,Integer>> getDamagedBuildingManager();

    void savePlayerLogin(long time);

    long getLastLoginTime();
}
