package swcnoops.server.commands.player.response;

import swcnoops.server.model.Tournament;
import swcnoops.server.model.TournamentRank;
import swcnoops.server.requests.AbstractCommandResult;

public class TournamentRankResult extends AbstractCommandResult {
    TournamentRank rank;
    Tournament tournament;
}
