package swcnoops.server.commands.guild;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.LeaderboardResult;
import swcnoops.server.json.JsonParser;

// TODO - need to finish off, this is to show the squad rankings
public class GuildLeaderboardGetLeaders extends AbstractCommandAction<GuildLeaderboardGetLeaders, LeaderboardResult> {
    @Override
    protected LeaderboardResult execute(GuildLeaderboardGetLeaders arguments, long time) throws Exception {
        // TODO - this should read in squads for top 50 and the rest
        return new LeaderboardResult();
    }

    @Override
    protected GuildLeaderboardGetLeaders parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GuildLeaderboardGetLeaders.class);
    }

    @Override
    public String getAction() {
        return "guild.leaderboard.getLeaders";
    }
}
