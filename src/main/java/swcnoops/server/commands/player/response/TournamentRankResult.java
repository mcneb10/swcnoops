package swcnoops.server.commands.player.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import swcnoops.server.model.Tournament;
import swcnoops.server.requests.AbstractCommandResult;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TournamentRankResult extends AbstractCommandResult {
    public Tournament tournament;
    public float percentile;
    public String tier;
}
