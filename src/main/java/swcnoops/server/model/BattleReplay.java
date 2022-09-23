package swcnoops.server.model;

import org.mongojack.Id;
import swcnoops.server.commands.player.PlayerBattleComplete;
import swcnoops.server.game.PvpMatch;
import swcnoops.server.session.PlayerSession;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BattleReplay {
    @Id
    private String battleId;
    public BattleLog battleLog = new BattleLog();
    public ReplayData replayData;
    public String attackerId;
    public String defenderId;
    public BattleType battleType;
    public long attackDate;
    private Date date;

    public BattleReplay() {
    }

    public BattleReplay(String battleId) {
        this.battleId = battleId;
    }

    public String getBattleId() {
        return battleId;
    }

    public void setBattleId(String battleId) {
        this.battleId = battleId;
    }

    static public BattleReplay map(PlayerBattleComplete playerBattleComplete, PlayerSession attackerSession,
                                   PvpMatch pvpMatch, long time) {
        BattleReplay battleReplay = map(playerBattleComplete, attackerSession,
                pvpMatch.getDefender().playerId,
                pvpMatch.getDefender().name,
                pvpMatch.getDefender().faction, time);

        battleReplay.battleLog.revenged = pvpMatch.isRevenge();

        // we overwrite it with the real data
        battleReplay.battleLog.attacker = pvpMatch.getAttacker();
        battleReplay.battleLog.defender = pvpMatch.getDefender();
        return battleReplay;
    }

    static public BattleReplay map(PlayerBattleComplete playerBattleComplete, PlayerSession attackerSession,
                                             String defenderPlayerId, String defenderName, FactionType defenderFaction, long time)
    {
        BattleReplay battleReplay = new BattleReplay(playerBattleComplete.getBattleId());
        battleReplay.attackerId = attackerSession.getPlayerId();
        battleReplay.defenderId = defenderPlayerId;
        battleReplay.battleType = playerBattleComplete.getReplayData().battleType;
        battleReplay.attackDate = time;

        battleReplay.battleLog.battleVersion = playerBattleComplete.getBattleVersion();
        battleReplay.battleLog.battleId = playerBattleComplete.getBattleId();
        battleReplay.battleLog.attackDate = time;

        battleReplay.battleLog.attacker = new BattleParticipant();
        battleReplay.battleLog.attacker.playerId = attackerSession.getPlayerId();
        battleReplay.battleLog.attacker.name = attackerSession.getPlayerSettings().getName();
        battleReplay.battleLog.attacker.faction = attackerSession.getFaction();

        battleReplay.battleLog.defender = new BattleParticipant();
        battleReplay.battleLog.defender.playerId = defenderPlayerId;
        battleReplay.battleLog.defender.name = defenderName;
        battleReplay.battleLog.defender.faction = defenderFaction;

        battleReplay.battleLog.attackerEquipment = new JsonStringArrayList();
        battleReplay.battleLog.baseDamagePercent = playerBattleComplete.getBaseDamagePercent();
        battleReplay.battleLog.defenderEquipment = new JsonStringArrayList();
        battleReplay.battleLog.stars = playerBattleComplete.getStars();
        battleReplay.battleLog.attackerGuildTroopsExpended = playerBattleComplete.getAttackerGuildTroopsSpent();
        battleReplay.battleLog.defenderGuildTroopsExpended = playerBattleComplete.getDefenderGuildTroopsSpent();
        battleReplay.battleLog.cmsVersion = playerBattleComplete.getCmsVersion();
        battleReplay.battleLog.defenderPotentialMedalGain = 0;

        Earned earned = new Earned();
        earned.credits = Math.max(playerBattleComplete.getReplayData().battleAttributes.lootCreditsEarned, 0);
        earned.materials = Math.max(playerBattleComplete.getReplayData().battleAttributes.lootMaterialsEarned, 0);
        earned.contraband = Math.max(playerBattleComplete.getReplayData().battleAttributes.lootContrabandEarned, 0);
        battleReplay.battleLog.earned = earned;

        Earned looted = new Earned();
        Map<CurrencyType, Integer> lootMap = playerBattleComplete.getLoot();
        looted.credits = Math.max(lootMap.get(CurrencyType.credits), 0);
        looted.materials = Math.max(lootMap.get(CurrencyType.materials), 0);
        looted.contraband = Math.max(lootMap.get(CurrencyType.contraband), 0);
        battleReplay.battleLog.looted = looted;

        Earned maxLootable = new Earned();
        maxLootable.credits = Math.max(playerBattleComplete.getReplayData().lootCreditsAvailable, 0);
        maxLootable.materials = Math.max(playerBattleComplete.getReplayData().lootMaterialsAvailable, 0);
        maxLootable.contraband = Math.max(playerBattleComplete.getReplayData().lootContrabandAvailable, 0);
        battleReplay.battleLog.maxLootable = maxLootable;

        battleReplay.battleLog.troopsExpended = mapTroopsUsed(playerBattleComplete.getReplayData().battleActions);

        battleReplay.battleLog.planetId = playerBattleComplete.getReplayData().combatEncounter.map.planet;
        battleReplay.battleLog.isUserEnded = playerBattleComplete.getIsUserEnded();

        // TODO - not sure what this does
        battleReplay.battleLog.server = false;

        battleReplay.replayData = playerBattleComplete.getReplayData();
        return battleReplay;
    }

    private static Map<String, Integer> mapTroopsUsed(List<BattleAction> battleActions) {
        Map<String, Integer> troops = new HashMap<>();
        if (battleActions != null) {
            for (BattleAction battleAction : battleActions) {
                if (battleAction.actionId != null) {
                    BattleAction usedTroop = null;
                    switch (battleAction.actionId) {
                        case "TroopPlaced":
                        case "SpecialAttackDeployed":
                        case "CreatureDeployed":
                        case "HeroDeployed":
                        case "ChampionDeployed":
                        case "SquadTroopPlaced":
                            usedTroop = battleAction;
                            break;
                    }

                    if (usedTroop != null) {
                        String troopId = usedTroop.troopId;

                        if (troopId == null)
                            troopId = usedTroop.creatureId;

                        if (troopId == null)
                            troopId = usedTroop.specialAttackId;

                        if (troopId != null) {
                            Integer count = troops.get(troopId);
                            if (count == null)
                                count = Integer.valueOf(0);

                            troops.put(troopId, Integer.valueOf(count.intValue() + 1));
                        }
                    }
                }
            }
        }
        return troops;
    }

    static private Map<String, Integer> mapTroopsUsed(DeploymentData deploymentData) {
        Map<String, Integer> troops = new HashMap<>();
        if (deploymentData != null) {
            if (deploymentData.troop != null)
                troops.putAll(deploymentData.troop);

            if (deploymentData.champion != null)
                troops.putAll(deploymentData.champion);

            if (deploymentData.hero != null)
                troops.putAll(deploymentData.hero);

            if (deploymentData.specialAttack != null)
                troops.putAll(deploymentData.specialAttack);
        }
        return troops;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }
}
