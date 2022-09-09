package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.player.response.PlayerPvpBattleCompleteCommandResult;
import swcnoops.server.game.PvpMatch;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;
import swcnoops.server.session.CurrencyDelta;
import swcnoops.server.session.PlayerSession;

public class PlayerPvpBattleComplete extends PlayerBattleComplete<PlayerPvpBattleComplete, PlayerPvpBattleCompleteCommandResult> {
    private JsonStringIntegerMap troopsExpended = new JsonStringIntegerMap();
    private int attackerMedals = 0;
    private int defenderMedals = 0;

    @Override
    protected PlayerPvpBattleCompleteCommandResult execute(PlayerPvpBattleComplete arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());
        PlayerPvpBattleCompleteCommandResult response = new PlayerPvpBattleCompleteCommandResult();
        PvpMatch pvpMatch = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId()).getPvpSession().getMatch(arguments.getBattleId());

        setUpTroopsExpended(arguments);
        setUpMedals(arguments, pvpMatch);
        BattleLog battleLog = createNewBattleLog(arguments, response, pvpMatch);

        setupResponse(arguments, response, pvpMatch);

        playerSession.battleComplete(arguments.getBattleId(), arguments.getStars(), arguments.getAttackingUnitsKilled(), time);
        //TODO, maybe better inside the battleComplete method above
        updatePlayer(arguments, pvpMatch);
        ServiceFactory.instance().getPlayerDatasource().saveNewPvPBattle(arguments, pvpMatch, battleLog);


        if (!pvpMatch.isDevBase())
            processDefenderResult();//TODO, set medals/resources/sc of real defender following result of battle
        return response;
    }

    private void setUpMedals(PlayerPvpBattleComplete arguments, PvpMatch pvpMatch) {
        switch (arguments.getStars()) {
            case 0:
                attackerMedals = pvpMatch.getPotentialScoreLose() * -1;
                defenderMedals = pvpMatch.getPotentialScoreWin();
                break;
            case 1:
                attackerMedals = (int) (pvpMatch.getPotentialScoreWin() * Float.valueOf(ServiceFactory.instance().getGameDataManager().getGameConstants().pvp_battle_one_star_victory));
                defenderMedals = (int) (pvpMatch.getPotentialScoreLose() * Float.valueOf(ServiceFactory.instance().getGameDataManager().getGameConstants().pvp_battle_one_star_victory)) * -1;
                break;
            case 2:
                attackerMedals = (int) (pvpMatch.getPotentialScoreWin() * Float.valueOf(ServiceFactory.instance().getGameDataManager().getGameConstants().pvp_battle_two_star_victory));
                defenderMedals = (int) (pvpMatch.getPotentialScoreLose() * Float.valueOf(ServiceFactory.instance().getGameDataManager().getGameConstants().pvp_battle_two_star_victory)) * -1;
                break;
            case 3:
                attackerMedals = pvpMatch.getPotentialScoreWin();
                defenderMedals = pvpMatch.getPotentialScoreLose();
                break;

        }


    }


    private void setUpTroopsExpended(PlayerPvpBattleComplete arguments) {
        this.troopsExpended = new JsonStringIntegerMap();
        arguments.getReplayData().attackerDeploymentData.troop.forEach((a, b) -> {
            troopsExpended.put(a, b);
        });
        arguments.getReplayData().attackerDeploymentData.champion.forEach((a, b) -> {
            troopsExpended.put(a, b);
        });
        arguments.getReplayData().attackerDeploymentData.creature.forEach((a, b) -> {
            troopsExpended.put(a, 1);
        });
        arguments.getReplayData().attackerDeploymentData.hero.forEach((a, b) -> {
            troopsExpended.put(a, b);
        });
        arguments.getReplayData().attackerDeploymentData.specialAttack.forEach((a, b) -> {
            troopsExpended.put(a, b);
        });

        //TODO - need to go through battleActions in the case of a cancelled battle, else the game sends all troops in lo deployed as the result.
    }


    private BattleLog createNewBattleLog(PlayerPvpBattleComplete arguments, PlayerPvpBattleCompleteCommandResult response, PvpMatch pvpMatch) {
        BattleLog battleLog = new BattleLog();
        battleLog.battleId = arguments.getBattleId();
        battleLog.attacker = pvpMatch.getAttacker();
        battleLog.defender = pvpMatch.getDefender();
        battleLog.attackDate = pvpMatch.getBattleDate();

        Earned earned = new Earned();
        earned.credits = Math.max(arguments.getReplayData().battleAttributes.lootCreditsEarned, 0);
        earned.materials = Math.max(arguments.getReplayData().battleAttributes.lootMaterialsEarned, 0);
        earned.materials = Math.max(arguments.getReplayData().battleAttributes.lootContrabandEarned, 0);
        battleLog.earned = earned;

        Earned looted = new Earned();
        looted.credits = Math.max(arguments.getLoot().get(CurrencyType.credits), 0);
        looted.materials = Math.max(arguments.getLoot().get(CurrencyType.materials), 0);
        looted.contraband = Math.max(arguments.getLoot().get(CurrencyType.contraband), 0);
        battleLog.looted = looted;

        Earned maxLootable = new Earned();
        maxLootable.credits = Math.max(arguments.getReplayData().lootCreditsAvailable, 0);
        maxLootable.materials = Math.max(arguments.getReplayData().lootMaterialsAvailable, 0);
        maxLootable.contraband = Math.max(arguments.getReplayData().lootContrabandAvailable, 0);
        battleLog.maxLootable = maxLootable;
        battleLog.troopsExpended = this.troopsExpended;
        battleLog.attackerGuildTroopsExpended = arguments.getAttackerGuildTroopsSpent();
        battleLog.defenderGuildTroopsExpended = arguments.getDefenderGuildTroopsSpent();
        battleLog.numVisitors = arguments.getNumVisitors();
        battleLog.baseDamagePercent = arguments.getBaseDamagePercent();
        battleLog.stars = arguments.getStars();
        battleLog.manifestVersion = 2045l;
        battleLog.potentialMedalGain = pvpMatch.getPotentialScoreWin();
        battleLog.revenged = false;
        battleLog.cmsVersion = arguments.getCmsVersion();
        battleLog.server = false;
        battleLog.battleVersion = arguments.getBattleVersion();
        battleLog.attackerEquipment = pvpMatch.getAttackerEquipment();
        battleLog.attackerEquipment = pvpMatch.getDefenderEquipment();
        battleLog.planetId = arguments.getPlanetId();


        return battleLog;

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

        response.troopsExpended = troopsExpended;
        response.attackerGuildTroopsExpended = arguments.getAttackerGuildTroopsSpent();

        Earned looted = new Earned();
        looted.credits = arguments.getLoot().get(CurrencyType.credits);
        looted.materials = arguments.getLoot().get(CurrencyType.materials);
        looted.contraband = arguments.getLoot().get(CurrencyType.contraband);
        response.looted = looted;

    }


    private void updatePlayer(PlayerPvpBattleComplete arguments, PvpMatch pvpMatch) {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId());
        int creditsGained = arguments.getLoot().get(CurrencyType.credits);
        int materialsGained = arguments.getLoot().get(CurrencyType.materials);
        int contraGained = arguments.getLoot().get(CurrencyType.contraband);

        CurrencyDelta creditDelta = new CurrencyDelta(creditsGained, creditsGained, CurrencyType.credits, false);
        playerSession.processInventoryStorage(creditDelta);

        CurrencyDelta materialDelta = new CurrencyDelta(creditsGained, materialsGained, CurrencyType.materials, false);
        playerSession.processInventoryStorage(materialDelta);

        CurrencyDelta contraDelta = new CurrencyDelta(creditsGained, contraGained, CurrencyType.materials, false);
        playerSession.processInventoryStorage(contraDelta);

        playerSession.updateScalars(newAttackerScalars(playerSession, arguments, pvpMatch));

        playerSession.savePlayerSession();
    }

    private Scalars newAttackerScalars(PlayerSession playerSession, PlayerPvpBattleComplete arguments, PvpMatch pvpMatch) {

        Scalars scalars = playerSession.getPlayerSettings().getScalars();

        scalars.attacksStarted++;
        scalars.attacksCompleted = arguments.isUserEnded() ? scalars.attacksCompleted : scalars.attacksCompleted + 1;
        scalars.attacksWon = arguments.getStars() > 0 ? scalars.attacksWon + 1 : scalars.attacksWon;
        scalars.attacksLost = arguments.getStars() == 0 ? scalars.attacksLost + 1 : scalars.attacksLost;
        scalars.attackRating = scalars.attackRating + attackerMedals;

        pvpMatch.setAttackerDelta(attackerMedals);
        pvpMatch.setDefenderDelta(defenderMedals);


        return scalars;
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
