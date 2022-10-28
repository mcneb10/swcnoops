package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.TournamentRankResult;
import swcnoops.server.datasource.TournamentStat;
import swcnoops.server.game.ConflictManager;
import swcnoops.server.game.TournamentData;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.Tournament;

/**
 * This gets called on PvP find screen to update the conflict status at the bottom for that planet
 */
public class PlayerLeaderboardTournamentGetRank extends AbstractCommandAction<PlayerLeaderboardTournamentGetRank, TournamentRankResult> {
    private String planetId;

    @Override
    protected TournamentRankResult execute(PlayerLeaderboardTournamentGetRank arguments, long time) throws Exception {
        ConflictManager conflictManager = ServiceFactory.instance().getGameDataManager().getConflictManager();
        TournamentData tournamentData = conflictManager.getConflict(arguments.getPlanetId());

        // TODO - to finish, get rating, tier and rank
        TournamentRankResult tournamentRankResult = new TournamentRankResult();
        if (tournamentData != null && tournamentData.isActive(time)) {
            TournamentStat tournamentStat = ServiceFactory.instance().getPlayerDatasource()
                    .getTournamentPlayerRank(tournamentData.getUid(), arguments.getPlayerId());

            tournamentRankResult.tournament = new Tournament();
            tournamentRankResult.tournament.percentile = tournamentStat.percentile;
            tournamentRankResult.tournament.uid = tournamentStat.uid;
            tournamentRankResult.tournament.bestTier = 1;
            tournamentRankResult.tournament.rating = tournamentStat.value;
            tournamentRankResult.tournament.redeemedRewards = null;
            tournamentRankResult.tournament.collected = false;

            tournamentRankResult.percentile = tournamentStat.percentile;
            tournamentRankResult.tier = null;

            // TODO - when conflict finishes and its our final rank
//            tournamentRankResult.tournament.finalRank = new TournamentRank();
//            tournamentRankResult.tournament.finalRank.percentile = 2;
//            tournamentRankResult.tournament.finalRank.tier = null;
        }

        return tournamentRankResult;
    }

    @Override
    protected PlayerLeaderboardTournamentGetRank parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerLeaderboardTournamentGetRank.class);
    }

    @Override
    public String getAction() {
        return "player.leaderboard.tournament.getRank";
    }

    public String getPlanetId() {
        return planetId;
    }

    public void setPlanetId(String planetId) {
        this.planetId = planetId;
    }
}
