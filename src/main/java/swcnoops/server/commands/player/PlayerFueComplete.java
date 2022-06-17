package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerFueCompleteResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.model.EpisodeProgressInfo;
import swcnoops.server.model.PlayerModel;

public class PlayerFueComplete extends AbstractCommandAction<PlayerFueComplete, PlayerFueCompleteResult> {
    @Override
    protected PlayerFueCompleteResult execute(PlayerFueComplete arguments) throws Exception {
        PlayerFueCompleteResult playerFueCompleteResult = new PlayerFueCompleteResult();
        playerFueCompleteResult.playerModel = new PlayerModel();
        playerFueCompleteResult.playerModel.episodeProgressInfo = new EpisodeProgressInfo();
        return playerFueCompleteResult;
    }

    @Override
    protected PlayerFueComplete parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerFueComplete.class);
    }

    @Override
    public String getAction() {
        return "player.fue.complete";
    }
}
