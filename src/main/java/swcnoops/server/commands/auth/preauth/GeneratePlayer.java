package swcnoops.server.commands.auth.preauth;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.Command;
import swcnoops.server.commands.player.response.PlayerLoginCommandResult;
import swcnoops.server.json.JsonParser;
import swcnoops.server.commands.auth.preauth.response.GeneratePlayerCommandResult;
import swcnoops.server.requests.EmptyMessage;
import swcnoops.server.requests.Messages;

public class GeneratePlayer extends AbstractCommandAction<GeneratePlayer, GeneratePlayerCommandResult> {
    @Override
    final public String getAction() {
        return "auth.preauth.generatePlayer";
    }

    @Override
    protected GeneratePlayer parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, GeneratePlayer.class);
    }

    @Override
    protected GeneratePlayerCommandResult execute(GeneratePlayer arguments, long time) throws Exception {
        GeneratePlayerCommandResult generatePlayerResponse = GeneratePlayerCommandResult.newInstance();
        PlayerLoginCommandResult template = loadPlayerTemplate();
        ServiceFactory.instance().getPlayerDatasource()
                .newPlayer(generatePlayerResponse.playerId, generatePlayerResponse.secret, template.playerModel, null, "new");
        return generatePlayerResponse;
    }

    private PlayerLoginCommandResult loadPlayerTemplate() throws Exception {
        PlayerLoginCommandResult response = ServiceFactory.instance().getJsonParser()
                .toObjectFromResource(ServiceFactory.instance().getConfig().playerLoginTemplate, PlayerLoginCommandResult.class);
        return response;
    }

    @Override
    protected Messages createMessage(Command command, GeneratePlayerCommandResult commandResult) {
        return EmptyMessage.instance;
    }

    @Override
    public boolean canAttachGuildNotifications() {
        return false;
    }

    @Override
    public boolean isAuthCommand() {
        return true;
    }
}
