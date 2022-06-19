package swcnoops.server.commands;

import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseData;

public interface CommandAction<A extends CommandArguments> {
    String getAction();
    CommandResult execute(Object args) throws Exception;

    ResponseData createResponse(Command command, CommandResult commandResult);
}
