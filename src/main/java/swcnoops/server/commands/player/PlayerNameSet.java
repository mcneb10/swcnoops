package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseHelper;
import swcnoops.server.session.PlayerSession;

public class PlayerNameSet extends AbstractCommandAction<PlayerNameSet, CommandResult> {
    private String playerName;

    public String getPlayerName() {
        return playerName;
    }

    @Override
    protected CommandResult execute(PlayerNameSet arguments, long time) throws Exception {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager()
                .getPlayerSession(arguments.getPlayerId());

        ServiceFactory.instance().getPlayerDatasource()
                .savePlayerName(arguments.getPlayerId(), arguments.getPlayerName());

        playerSession.getPlayer().getPlayerSettings().setName(arguments.getPlayerName());

        return ResponseHelper.SUCCESS_COMMAND_RESULT;
    }

    @Override
    protected PlayerNameSet parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerNameSet.class);
    }

    @Override
    public String getAction() {
        return "player.name.set";
    }
}
