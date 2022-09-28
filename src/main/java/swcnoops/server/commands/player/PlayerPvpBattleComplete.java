package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.player.response.PlayerPvpBattleCompleteCommandResult;
import swcnoops.server.game.PvpMatch;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.*;
import swcnoops.server.session.PlayerSession;

import java.util.HashMap;
import java.util.Map;

public class PlayerPvpBattleComplete extends PlayerBattleComplete<PlayerPvpBattleComplete, PlayerPvpBattleCompleteCommandResult> {
    @Override
    protected PlayerPvpBattleCompleteCommandResult execute(PlayerPvpBattleComplete arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        PvpMatch pvpMatch = ServiceFactory.instance().getSessionManager().getPlayerSession(arguments.getPlayerId())
                .getPvpSession().getCurrentPvPMatch();

        calculateMatchScoreAndPoints(pvpMatch, arguments.getStars(), arguments.getIsUserEnded());

        BattleReplay battleReplay;
        if (!pvpMatch.isDevBase()) {
            battleReplay = BattleReplay.map(arguments, playerSession, pvpMatch, time);
        } else {
            battleReplay = BattleReplay.map(arguments, playerSession, pvpMatch.getParticipantId(),
                    "DevBase", pvpMatch.getFactionType(), time);
        }

        mergeDamagedBuildings(pvpMatch, arguments.getDamagedBuildings());
        playerSession.pvpBattleComplete(battleReplay, pvpMatch, time);

        PlayerPvpBattleCompleteCommandResult response = mapResponse(battleReplay);
        return response;
    }

    /**
     * Joins the already damaged buildings with the damage from the latest attack
     * @param pvpMatch
     * @param damagedBuildings
     */
    private void mergeDamagedBuildings(PvpMatch pvpMatch, Map<String, Integer> damagedBuildings) {
        if (damagedBuildings != null) {
            if (pvpMatch.getDefenderDamagedBuildings() == null)
                pvpMatch.setDefenderDamagedBuildings(new HashMap<>());

            Map<String, Integer> alreadyDamaged = pvpMatch.getDefenderDamagedBuildings();
            for (Map.Entry<String,Integer> attackedBuilding : damagedBuildings.entrySet()) {
                Integer damage = alreadyDamaged.get(attackedBuilding.getKey());
                if (damage == null) {
                    damage = attackedBuilding.getValue();
                } else {
                    damage = Math.max(damage, attackedBuilding.getValue());
                }

                // no point taking 0 damage buildings
                if (damage != 0) {
                    alreadyDamaged.put(attackedBuilding.getKey(), damage);
                }
            }
        }
    }

    private void calculateMatchScoreAndPoints(PvpMatch pvpMatch, int stars, boolean userEnded) {
        // we prevent negative ratings as negative medals prevents players from joining a squad
        BattleParticipant attacker = pvpMatch.getAttacker();
        attacker.attackRatingDelta = getAttackerMedals(stars, pvpMatch);

        if (attacker.attackRating + attacker.attackRatingDelta < 0)
            attacker.attackRatingDelta = -attacker.attackRating;

        BattleParticipant defender = pvpMatch.getDefender();
        defender.defenseRatingDelta = getDefendersMedals(stars, pvpMatch);

        if (defender.defenseRating + defender.defenseRatingDelta < 0)
            defender.defenseRatingDelta = -defender.defenseRating;
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

    private PlayerPvpBattleCompleteCommandResult mapResponse(BattleReplay battleReplay) {
        PlayerPvpBattleCompleteCommandResult response = new PlayerPvpBattleCompleteCommandResult();
        response.attackerTournament.uid = ServiceFactory.createRandomUUID();
        response.battleVersion = battleReplay.battleLog.battleVersion;
        response.cmsVersion = battleReplay.battleLog.cmsVersion;
        response.stars = battleReplay.battleLog.stars;
        response.baseDamagePercent = battleReplay.battleLog.baseDamagePercent;
        response.battleId = battleReplay.battleLog.battleId;
        response.manifestVersion = battleReplay.battleLog.manifestVersion;

        response.attacker = battleReplay.battleLog.attacker;
        response.defender = battleReplay.battleLog.defender;

        response.attackDate = battleReplay.attackDate;
        response.planetId = battleReplay.battleLog.planetId;
        response.potentialMedalGain = battleReplay.battleLog.potentialMedalGain;
        response.attackerGuildTroopsExpended = battleReplay.battleLog.attackerGuildTroopsExpended;
        response.troopsExpended = battleReplay.battleLog.troopsExpended;

        response.looted = battleReplay.battleLog.looted;
        response.earned = battleReplay.battleLog.earned;

        response.revenged = battleReplay.battleLog.revenged;

        return response;
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
