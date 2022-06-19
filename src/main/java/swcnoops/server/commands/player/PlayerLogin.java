package swcnoops.server.commands.player;

import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.Command;
import swcnoops.server.json.JsonParser;
import swcnoops.server.commands.player.response.PlayerLoginCommandResult;
import swcnoops.server.requests.LoginMessages;
import swcnoops.server.requests.Messages;
import swcnoops.server.session.PlayerSession;

public class PlayerLogin extends AbstractCommandAction<PlayerLogin, PlayerLoginCommandResult> {
    @Override
    final public String getAction() {
        return "player.login";
    }

    @Override
    protected PlayerLogin parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerLogin.class);
    }

    // TODO - need to fix this to log in properly for the player
    @Override
    protected PlayerLoginCommandResult execute(PlayerLogin arguments) throws Exception {
        PlayerLoginCommandResult response;
        try {
            response = loadPlayerDetails(arguments.getPlayerId());
            configureLoginForPlayer(response, arguments.getPlayerId());
        } catch (Exception ex) {
            // TODO
            response = new PlayerLoginCommandResult();
        }

        return response;
    }

    private PlayerLoginCommandResult loadPlayerDetails(String playerId) throws Exception {
        PlayerLoginCommandResult response = ServiceFactory.instance().getJsonParser()
                .toObjectFromResource(ServiceFactory.instance().getConfig().playerLoginTemplate, PlayerLoginCommandResult.class);
        return response;
    }

    // TODO - setup map and troops
    private void configureLoginForPlayer(PlayerLoginCommandResult playerLoginResponse, String playerId) {
        PlayerSession playerSession = ServiceFactory.instance().getSessionManager().getPlayerSession(playerId);

        playerLoginResponse.playerId = playerSession.getPlayerId();
        playerLoginResponse.name = playerSession.getPlayer().getName();

        // TODO
        // playerLoginResponse.playerModel.map = playerSession.getMap();

        // turn off conflicts
        playerLoginResponse.sharedPrefs.put("tv", null);
        // this disables login to google at start up
        playerLoginResponse.sharedPrefs.put("promptedForGoogleSignin", "1");
    }

    @Override
    protected Messages createMessage(Command command) {
        return new LoginMessages(command.getTime(), ServiceFactory.getSystemTimeSecondsFromEpoch(),
                ServiceFactory.createRandomUUID());
    }
}