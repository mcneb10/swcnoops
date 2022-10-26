package swcnoops.server.commands.player.response;

import swcnoops.server.datasource.TournamentStat;
import swcnoops.server.requests.AbstractCommandResult;

import java.util.Map;

public class TournamentRanksResult extends AbstractCommandResult {
    public Map<String, TournamentStat> tournamentRankResponse;
}
