package swcnoops.server.game;

import swcnoops.server.datasource.TournamentLeaderBoard;
import swcnoops.server.datasource.TournamentStat;

import java.util.List;

public interface ConflictManager {
    TournamentData getConflict(String planetId);
    TournamentTierData getTopTier();

    void calculatePercentile(TournamentLeaderBoard leaderBoard);

    TournamentStat getTournamentStats(List<TournamentStat> tournaments, TournamentData tournamentData);

    TournamentData getTournament(String uid);

    void calculatePercentile(TournamentStat foundPlayer, TournamentStat lastTournamentStat);
}
