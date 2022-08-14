package swcnoops.server.commands;

import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseData;

public interface CommandAction<R extends CommandResult> {
    String getAction();
    CommandResult execute(Object args, long time) throws Exception;

    ResponseData createResponse(Command command, R commandResult);

    boolean canAttachGuildNotifications();
}
