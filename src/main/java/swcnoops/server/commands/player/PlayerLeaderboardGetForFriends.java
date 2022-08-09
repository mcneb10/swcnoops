package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.guild.response.LeaderboardResult;
import swcnoops.server.json.JsonParser;

// TODO
public class PlayerLeaderboardGetForFriends extends AbstractCommandAction<PlayerLeaderboardGetForFriends, LeaderboardResult> {
    private String friendIds;

    @Override
    protected LeaderboardResult execute(PlayerLeaderboardGetForFriends arguments, long time) throws Exception {
        return new LeaderboardResult();
    }

    @Override
    protected PlayerLeaderboardGetForFriends parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerLeaderboardGetForFriends.class);
    }

    @Override
    public String getAction() {
        return "player.leaderboard.getForFriends";
    }

    public String getFriendIds() {
        return friendIds;
    }
}
