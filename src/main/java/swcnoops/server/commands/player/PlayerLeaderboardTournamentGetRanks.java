package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.TournamentRanksResult;
import swcnoops.server.datasource.TournamentStat;
import swcnoops.server.json.JsonParser;
import swcnoops.server.session.PlayerSession;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerLeaderboardTournamentGetRanks extends AbstractCommandAction<PlayerLeaderboardTournamentGetRanks, TournamentRanksResult> {
    @Override
    protected TournamentRanksResult execute(PlayerLeaderboardTournamentGetRanks arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .loginPlayerSession(arguments.getPlayerId());

        List<TournamentStat> tournamentStats = playerSession.getTournamentManager().getObjectForReading();

        TournamentRanksResult tournamentRanksResult = new TournamentRanksResult();
        tournamentRanksResult.tournamentRankResponse = new ConcurrentHashMap<>();

        if (tournamentStats != null) {
            for (TournamentStat tournamentStat : tournamentStats) {
                // TODO - work out rank, percentile and tier for this player
                tournamentRanksResult.tournamentRankResponse.put(tournamentStat.uid, tournamentStat);
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
