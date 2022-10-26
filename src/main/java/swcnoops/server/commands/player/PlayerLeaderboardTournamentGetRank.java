package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.TournamentRankResult;
import swcnoops.server.datasource.TournamentStat;
import swcnoops.server.json.JsonParser;
import swcnoops.server.session.PlayerSession;

import java.util.List;

public class PlayerLeaderboardTournamentGetRank extends AbstractCommandAction<PlayerLeaderboardTournamentGetRank, TournamentRankResult> {
    private String planetId;

    @Override
    protected TournamentRankResult execute(PlayerLeaderboardTournamentGetRank arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .loginPlayerSession(arguments.getPlayerId());

        List<TournamentStat> tournamentStats = playerSession.getTournamentManager().getObjectForReading();

        // TODO - to finish, get rating, tier and rank
        TournamentRankResult tournamentRankResult = new TournamentRankResult();
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
