package swcnoops.server.model;

import swcnoops.server.commands.player.PlayerBattleComplete;
import swcnoops.server.session.PlayerSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BattleReplay {
    public BattleEntry battleLog = new BattleEntry();
    public ReplayData replayData;

    public BattleReplay() {
    }

    static public BattleReplay map(PlayerBattleComplete playerBattleComplete, PlayerSession attackerSession,
                                   PlayerSession defenderSession, long time) {
        return createAndMap(playerBattleComplete, attackerSession, defenderSession, time);
    }

    static private BattleReplay createAndMap(PlayerBattleComplete playerBattleComplete, PlayerSession attackerSession,
                                             PlayerSession defenderSession, long time) {
        BattleReplay battleReplay = new BattleReplay();
        battleReplay.battleLog.battleVersion = playerBattleComplete.getBattleVersion();
        battleReplay.battleLog.battleId = playerBattleComplete.getBattleId();
        battleReplay.battleLog.attackDate = time;
        battleReplay.battleLog.attacker = new BattleParticipant();
        battleReplay.battleLog.attacker.playerId = attackerSession.getPlayerId();
        battleReplay.battleLog.attacker.name = attackerSession.getPlayerSettings().getName();
        battleReplay.battleLog.attacker.faction = attackerSession.getFaction();

        battleReplay.battleLog.defender = new BattleParticipant();
        battleReplay.battleLog.defender.playerId = defenderSession.getPlayerId();
        battleReplay.battleLog.defender.name = defenderSession.getPlayerSettings().getName();
        battleReplay.battleLog.defender.faction = defenderSession.getFaction();

        battleReplay.battleLog.attackerEquipment = new ArrayList<>();
        battleReplay.battleLog.baseDamagePercent = playerBattleComplete.getBaseDamagePercent();
        battleReplay.battleLog.defenderEquipment = new ArrayList<>();
        battleReplay.battleLog.stars = playerBattleComplete.getStars();
        battleReplay.battleLog.attackerGuildTroopsExpended = playerBattleComplete.getAttackerGuildTroopsSpent();
        battleReplay.battleLog.defenderGuildTroopsExpended = playerBattleComplete.getDefenderGuildTroopsSpent();
        battleReplay.battleLog.cmsVersion = playerBattleComplete.getCmsVersion();
        battleReplay.battleLog.defenderPotentialMedalGain = 0;
        battleReplay.battleLog.earned = new Earned();
        battleReplay.battleLog.looted = new Earned();
        battleReplay.battleLog.maxLootable = new Earned();

        battleReplay.battleLog.troopsExpended = mapTroopsUsed(playerBattleComplete.getReplayData().battleActions);
        battleReplay.battleLog.revenged = false;
        battleReplay.battleLog.planetId = playerBattleComplete.getReplayData().combatEncounter.map.planet;

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
}
