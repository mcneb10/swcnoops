package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.LeaderboardResult;
import swcnoops.server.json.JsonParser;

// TODO - need to complete, the response might not be correct
// this is called on the leaderboard to show player rankings
public class PlayerLeaderboardGetLeaders extends AbstractCommandAction<PlayerLeaderboardGetLeaders, LeaderboardResult> {
    private String planetUid;

    @Override
    protected LeaderboardResult execute(PlayerLeaderboardGetLeaders arguments, long time) throws Exception {
        return new LeaderboardResult();
    }

    @Override
    protected PlayerLeaderboardGetLeaders parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerLeaderboardGetLeaders.class);
    }

    @Override
    public String getAction() {
        return "player.leaderboard.getLeaders";
    }

    public String getPlanetUid() {
        return planetUid;
    }
}
