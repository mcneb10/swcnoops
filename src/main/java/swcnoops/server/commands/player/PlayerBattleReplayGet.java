package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerBattleReplayGetResult;
import swcnoops.server.datasource.WarBattle;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlayerBattleReplayGet extends AbstractCommandAction<PlayerBattleReplayGet, PlayerBattleReplayGetResult> {
    private String battleId;
    private String participantId;

    @Override
    protected PlayerBattleReplayGetResult execute(PlayerBattleReplayGet arguments, long time) throws Exception {
        WarBattle warBattle = ServiceFactory.instance().getPlayerDatasource().getWarBattle(arguments.getBattleId());

        BattleReplayResponse battleReplayResponse = map(warBattle);
        return new PlayerBattleReplayGetResult(battleReplayResponse);
    }

    private BattleReplayResponse map(WarBattle warBattle) {
        BattleReplayResponse battleReplayResponse = new BattleReplayResponse();
        battleReplayResponse.battleLog.battleVersion = warBattle.getBattleResponse().getBattleVersion();
        battleReplayResponse.battleLog.battleId = warBattle.getBattleId();
        battleReplayResponse.battleLog.attackDate = warBattle.getBattleCompleteTime();
        battleReplayResponse.battleLog.attacker = new Attacker();
        battleReplayResponse.battleLog.attacker.playerId = warBattle.getAttackerId();
        battleReplayResponse.battleLog.attacker.name = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(warBattle.getAttackerId()).getPlayerSettings().getName();

        battleReplayResponse.battleLog.defender = new Defender();
        battleReplayResponse.battleLog.defender.playerId = warBattle.getDefenderId();
        battleReplayResponse.battleLog.defender.name = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(warBattle.getDefenderId()).getPlayerSettings().getName();

        battleReplayResponse.battleLog.attackerEquipment = new ArrayList<>();
        battleReplayResponse.battleLog.baseDamagePercent = warBattle.getBattleResponse().getBaseDamagePercent();
        battleReplayResponse.battleLog.defenderEquipment = new ArrayList<>();
        battleReplayResponse.battleLog.stars = warBattle.getBattleResponse().getStars();
        battleReplayResponse.battleLog.attackerGuildTroopsExpended = warBattle.getBattleResponse().getAttackerGuildTroopsSpent();
        battleReplayResponse.battleLog.defenderGuildTroopsExpended = warBattle.getBattleResponse().getDefenderGuildTroopsSpent();
        battleReplayResponse.battleLog.cmsVersion = warBattle.getBattleResponse().getCmsVersion();
        battleReplayResponse.battleLog.defenderPotentialMedalGain = 0;
        battleReplayResponse.battleLog.earned = new Earned();
        battleReplayResponse.battleLog.looted = new Earned();
        battleReplayResponse.battleLog.troopsExpended = mapTroops(warBattle.getBattleResponse().getReplayData().attackerDeploymentData);
        battleReplayResponse.battleLog.maxLootable = new Earned();
        battleReplayResponse.battleLog.revenged = false;
        battleReplayResponse.battleLog.planetId = warBattle.getBattleResponse().getPlanetId();

        // TODO - not sure what this does
        battleReplayResponse.battleLog.server = false;

        battleReplayResponse.replayData = warBattle.getBattleResponse().getReplayData();

        battleReplayResponse.replayData.battleType = BattleType.Pvp;
        // we override stars to always mark it as failed otherwise in War Replay it will crash the
        // client when BattleType.PvpAttackSquadWar
        // battleReplayResponse.battleLog.stars = 0;
        return battleReplayResponse;
    }

    private Map<String, Integer> mapTroops(DeploymentData deploymentData) {
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

    @Override
    protected PlayerBattleReplayGet parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerBattleReplayGet.class);
    }

    @Override
    public String getAction() {
        return "player.battle.replay.get";
    }

    public String getBattleId() {
        return battleId;
    }

    public String getParticipantId() {
        return participantId;
    }
}
