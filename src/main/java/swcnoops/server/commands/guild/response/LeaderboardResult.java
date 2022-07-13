package swcnoops.server.commands.guild.response;

import swcnoops.server.model.GuildBoardDetail;
import swcnoops.server.requests.AbstractCommandResult;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardResult extends AbstractCommandResult {
    private List<GuildBoardDetail> leaders;
    private List<GuildBoardDetail> surrounding;

    public LeaderboardResult() {
        this.leaders = new ArrayList<>();
        this.surrounding = new ArrayList<>();
    }

    public List<GuildBoardDetail> getLeaders() {
        return leaders;
    }

    public List<GuildBoardDetail> getSurrounding() {
        return surrounding;
    }
}
