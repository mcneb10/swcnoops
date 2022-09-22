package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.player.response.PlayerPvpBattleCompleteCommandResult;
import swcnoops.server.game.PvpMatch;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;
import swcnoops.server.session.PlayerSession;

import java.util.Map;

public class PlayerPvpBattleComplete extends PlayerBattleComplete<PlayerPvpBattleComplete, PlayerPvpBattleCompleteCommandResult> {
    @Override
    protected PlayerPvpBattleCompleteCommandResult execute(PlayerPvpBattleComplete arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        PvpMatch pvpMatch = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId())
                .getPvpSession().getCurrentPvPMatch();

        calculateMatchScoreAndPoints(pvpMatch, arguments.getStars(), arguments.isUserEnded());

        BattleReplay battleReplay;
        if (!pvpMatch.isDevBase()) {
            battleReplay = BattleReplay.map(arguments, playerSession, pvpMatch, time);
        } else {
            battleReplay = BattleReplay.map(arguments, playerSession, pvpMatch.getParticipantId(),
                    "DevBase", pvpMatch.getFactionType(), time);
        }

        mergeDamagedBuildings(pvpMatch, arguments.getDamagedBuildings());
        playerSession.pvpBattleComplete(battleReplay, arguments.getAttackingUnitsKilled(), pvpMatch, time);

        PlayerPvpBattleCompleteCommandResult response = new PlayerPvpBattleCompleteCommandResult();
        setupResponse(arguments, response, pvpMatch);
        return response;
    }

    /**
     * Joins the already damaged buildings with the damage from the latest attack
     * @param pvpMatch
     * @param damagedBuildings
     */
    private void mergeDamagedBuildings(PvpMatch pvpMatch, Map<String, Integer> damagedBuildings) {
        if (pvpMatch.getDefenderDamagedBuildings() == null)
            pvpMatch.setDefenderDamagedBuildings(damagedBuildings);
        else {
            if (damagedBuildings != null) {
                Map<String, Integer> alreadyDamaged = pvpMatch.getDefenderDamagedBuildings();
                for (Map.Entry<String,Integer> attackedBuilding : damagedBuildings.entrySet()) {
                    Integer damage = alreadyDamaged.get(attackedBuilding.getKey());
                    if (damage == null) {
                        damage = attackedBuilding.getValue();
                    } else {
                        damage = Math.max(damage, attackedBuilding.getValue());
                    }

                    alreadyDamaged.put(attackedBuilding.getKey(), damage);
                }
            }
        }
    }

    private void calculateMatchScoreAndPoints(PvpMatch pvpMatch, int stars, boolean userEnded) {
        // if we want to prevent negative scores and points it should be done here and it will flow to the rest
        // of the code as everything works off the delta
        pvpMatch.getAttacker().attackRatingDelta = getAttackerMedals(stars, pvpMatch);

        pvpMatch.getDefender().defenseRatingDelta = getDefendersMedals(stars, pvpMatch);
    }

    private int getDefendersMedals(int stars, PvpMatch pvpMatch) {
        int defenderMedals = 0;
        switch (stars) {
            case 0:
                defenderMedals = pvpMatch.getPotentialScoreWin();
                break;
            case 1:
                defenderMedals = (int) (pvpMatch.getPotentialScoreLose() *
                        ServiceFactory.instance().getGameDataManager().getGameConstants().pvp_battle_one_star_victory) * -1;
                break;
            case 2:
                defenderMedals = (int) (pvpMatch.getPotentialScoreLose() *
                        ServiceFactory.instance().getGameDataManager().getGameConstants().pvp_battle_two_star_victory) * -1;
                break;
            case 3:
                defenderMedals = (int) (pvpMatch.getPotentialScoreLose() *
                        ServiceFactory.instance().getGameDataManager().getGameConstants().pvp_battle_three_star_victory) * -1;
                break;
        }

        return defenderMedals;
    }

    private int getAttackerMedals(int stars, PvpMatch pvpMatch) {
        int medalsDelta = 0;

        switch (stars) {
            case 0:
                medalsDelta = pvpMatch.getPotentialScoreLose() * -1;
                break;
            case 1:
                medalsDelta = (int) (pvpMatch.getPotentialScoreWin() *
                        ServiceFactory.instance().getGameDataManager().getGameConstants().pvp_battle_one_star_victory);
                break;
            case 2:
                medalsDelta = (int) (pvpMatch.getPotentialScoreWin() *
                        ServiceFactory.instance().getGameDataManager().getGameConstants().pvp_battle_two_star_victory);
                break;
            case 3:
                medalsDelta = (int) (pvpMatch.getPotentialScoreWin() *
                        ServiceFactory.instance().getGameDataManager().getGameConstants().pvp_battle_three_star_victory);
                break;
        }

        return medalsDelta;
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

    @Override
    protected PlayerPvpBattleComplete parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerPvpBattleComplete.class);
    }

    @Override
    public String getAction() {
        return "player.pvp.battle.complete";
    }
}
