package swcnoops.server.commands.auth.preauth;

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
    protected GeneratePlayerCommandResult execute(GeneratePlayer arguments) throws Exception {
        // TODO - change generate our guid and secret
        GeneratePlayerCommandResult generatePlayerResponse = new GeneratePlayerCommandResult();
        generatePlayerResponse.playerId = "2c2d4aea-7f38-11e5-a29f-069096004f69";
        generatePlayerResponse.secret = "1118035f8f4160d5606e0c1a5e101ae5";
        return generatePlayerResponse;
    }

    @Override
    protected Messages createMessage(Command command) {
        return EmptyMessage.instance;
    }
}
