package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.TournamentRanksResult;
import swcnoops.server.datasource.TournamentStat;
import swcnoops.server.game.ConflictManager;
import swcnoops.server.game.TournamentData;
import swcnoops.server.json.JsonParser;
import swcnoops.server.session.PlayerSession;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This gets called on login, probably to get the initial tournament rank and score for player
 */
public class PlayerLeaderboardTournamentGetRanks extends AbstractCommandAction<PlayerLeaderboardTournamentGetRanks, TournamentRanksResult> {
    @Override
    protected TournamentRanksResult execute(PlayerLeaderboardTournamentGetRanks arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .loginPlayerSession(arguments.getPlayerId());

        List<TournamentStat> tournamentStats = playerSession.getTournamentManager().getObjectForReading();
        TournamentRanksResult tournamentRanksResult = new TournamentRanksResult();
        tournamentRanksResult.tournaments = new ConcurrentHashMap<>();

        if (tournamentStats != null) {
            ConflictManager conflictManager = ServiceFactory.instance().getGameDataManager().getConflictManager();
            for (TournamentStat tournamentStat : tournamentStats) {
                TournamentData tournamentData = conflictManager.getTournament(tournamentStat.uid);
                if (tournamentData.isActive(time)) {
                    TournamentStat stat = ServiceFactory.instance().getPlayerDatasource()
                            .getTournamentPlayerRank(tournamentStat.uid, arguments.getPlayerId());
                    if (stat != null) {
                        tournamentRanksResult.tournaments.put(stat.uid, stat);
                    }
                }
            }
        }

        return tournamentRanksResult;
    }

    @Override
    protected PlayerLeaderboardTournamentGetRanks parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerLeaderboardTournamentGetRanks.class);
    }

    @Override
    public String getAction() {
        return "player.leaderboard.tournament.getRanks";
    }
}
