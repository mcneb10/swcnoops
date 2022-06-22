package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.player.response.PlayerEpisodesProgressGetCommandResult;
import swcnoops.server.json.JsonParser;

/**
 * Currently does not work so deprecated for now
 */
@Deprecated
public class PlayerEpisodesProgressGet extends AbstractCommandAction<PlayerEpisodesProgressGet, PlayerEpisodesProgressGetCommandResult> {
    @Override
    protected PlayerEpisodesProgressGetCommandResult execute(PlayerEpisodesProgressGet arguments, long time) throws Exception {
        PlayerEpisodesProgressGetCommandResult response =
                ServiceFactory.instance().getJsonParser()
                        .toObjectFromResource("templates/playerEpisodesProgressGet.json",
                                PlayerEpisodesProgressGetCommandResult.class);
        return response;
    }

    @Override
    protected PlayerEpisodesProgressGet parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerEpisodesProgressGet.class);
    }

    @Override
    public String getAction() {
        return "player.episodes.progress.get";
    }
}
