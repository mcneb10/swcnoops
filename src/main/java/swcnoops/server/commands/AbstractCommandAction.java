package swcnoops.server.commands;

import swcnoops.server.ServiceFactory;
import swcnoops.server.json.JsonParser;
import swcnoops.server.requests.CommandMessages;
import swcnoops.server.requests.Messages;
import swcnoops.server.requests.CommandResult;
import swcnoops.server.requests.ResponseData;

abstract public class AbstractCommandAction<A extends CommandArguments, R extends CommandResult>
        implements CommandArguments, CommandAction<A>
{
    private String playerId;

    public String getPlayerId() {
        return playerId;
    }

    @Override
    public R execute(Object args, long time) throws Exception {
        JsonParser jsonParser = ServiceFactory.instance().getJsonParser();
        A parsedArgument = this.parseArgument(jsonParser, args);
        return this.execute(parsedArgument, time);
    }

    protected abstract R execute(A arguments, long time) throws Exception;

    protected abstract A parseArgument(JsonParser jsonParser, Object argumentObject);

    @Override
    public ResponseData createResponse(Command command, CommandResult commandResult) {
        ResponseData responseData = new ResponseData();
        responseData.requestId = command.getRequestId();

        if (commandResult != null) {
            responseData.result = commandResult.getResult();
            responseData.status = commandResult.getStatus();
        }

        responseData.messages = createMessage(command);
        return responseData;
    }

    protected Messages createMessage(Command command) {
        return new CommandMessages(command.getTime(), ServiceFactory.getSystemTimeSecondsFromEpoch(), ServiceFactory.createRandomUUID());
    }

    static final public <T extends CommandResult> T parseJsonFile(String filename, Class<T> clazz) {
        T response;

        try {
            response = ServiceFactory.instance().getJsonParser().toObjectFromResource(filename, clazz);
        } catch (Exception ex) {
            response = null;
        }

        return response;
    }
}
