package swcnoops.server.commands.player;

import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.AbstractCommandAction;
import swcnoops.server.commands.Command;
import swcnoops.server.json.JsonParser;
import swcnoops.server.commands.player.response.PlayerContentGetCommandResult;
import swcnoops.server.requests.EmptyMessage;
import swcnoops.server.requests.Messages;

public class PlayerContentGet extends AbstractCommandAction<PlayerContentGet, PlayerContentGetCommandResult> {

    @Override
    final public String getAction() {
        return "player.content.get";
    }

    @Override
    protected PlayerContentGet parseArgument(JsonParser jsonParser, Object argumentObject) {
        return jsonParser.fromJsonObject(argumentObject, PlayerContentGet.class);
    }

    @Override
    protected PlayerContentGetCommandResult execute(PlayerContentGet arguments, long time) throws Exception {
        PlayerContentGetCommandResult response =
                parseJsonFile(ServiceFactory.instance().getConfig().playerContentGetTemplate, PlayerContentGetCommandResult.class);
        response.cdnRoots.clear();
        response.cdnRoots.add("http://192.168.1.142:8080/swcFiles/");

        response.secureCdnRoots.clear();
        response.secureCdnRoots.add("http://192.168.1.142:8080/swcFiles/");
        return response;
    }

    @Override
    protected Messages createMessage(Command command, PlayerContentGetCommandResult commandResult) {
        return EmptyMessage.instance;
    }
}
