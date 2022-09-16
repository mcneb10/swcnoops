package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.player.response.PlayerPvpBattleCompleteCommandResult;
import swcnoops.server.game.PvpMatch;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;
import swcnoops.server.session.PlayerSession;

public class PlayerPvpBattleComplete extends PlayerBattleComplete<PlayerPvpBattleComplete, PlayerPvpBattleCompleteCommandResult> {
    @Override
    protected PlayerPvpBattleCompleteCommandResult execute(PlayerPvpBattleComplete arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        PvpMatch pvpMatch = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId())
                .getPvpSession().getMatch(arguments.getBattleId());

        BattleReplay battleReplay;
        if (!pvpMatch.isDevBase()) {
            PlayerSession defenderSession = ServiceFactory.instance().getSessionManager()
                    .getPlayerSession(pvpMatch.getParticipantId());
            battleReplay = BattleReplay.map(arguments, playerSession, defenderSession, time);
        } else {
            battleReplay = BattleReplay.map(arguments, playerSession, pvpMatch.getParticipantId(),
                    "DevBase", pvpMatch.getFactionType(), time);
        }

        playerSession.pvpBattleComplete(battleReplay,
                arguments.getAttackingUnitsKilled(), pvpMatch, time);

        if (!pvpMatch.isDevBase())
            processDefenderResult();//TODO, set medals/resources/sc of real defender following result of battle

        PlayerPvpBattleCompleteCommandResult response = new PlayerPvpBattleCompleteCommandResult();
        setupResponse(arguments, response, pvpMatch);
        return response;
    }

    private void setupResponse(PlayerPvpBattleComplete arguments, PlayerPvpBattleCompleteCommandResult response, PvpMatch pvpMatch) {
        response.attackerTournament.uid = ServiceFactory.createRandomUUID();
        response.battleVersion = arguments.getBattleVersion();
        response.cmsVersion = arguments.getCmsVersion();
        response.stars = arguments.getStars();
        response.baseDamagePercent = arguments.getBaseDamagePercent();
        response.battleId = arguments.getBattleId();
        response.manifestVersion = "02045";
        response.attacker = pvpMatch.getAttacker();
        response.defender = pvpMatch.getDefender();
        response.attackDate = pvpMatch.getBattleDate();
        response.planetId = arguments.getPlanetId();
        response.potentialMedalGain = pvpMatch.getPotentialScoreWin();

        response.attackerGuildTroopsExpended = arguments.getAttackerGuildTroopsSpent();

        Earned looted = new Earned();
        looted.credits = arguments.getLoot().get(CurrencyType.credits);
        looted.materials = arguments.getLoot().get(CurrencyType.materials);
        looted.contraband = arguments.getLoot().get(CurrencyType.contraband);
        response.looted = looted;
    }

    private void processDefenderResult() {
    }

    @Override
    protected PlayerPvpBattleComplete parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerPvpBattleComplete.class);
    }

    @Override
    public String getAction() {
        return "player.pvp.battle.complete";
    }
}
