package swcnoops.server.commands.auth.preauth;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.Command;
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
        ServiceFactory.instance().getPlayerDatasource()
                .newPlayer(generatePlayerResponse.playerId, generatePlayerResponse.secret);
        return generatePlayerResponse;
    }

    @Override
    protected Messages createMessage(Command command, GeneratePlayerCommandResult commandResult) {
        return EmptyMessage.instance;
    }

    @Override
    public boolean canAttachGuildNotifications() {
        return false;
    }
}
