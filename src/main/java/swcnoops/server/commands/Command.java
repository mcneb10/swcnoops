package swcnoops.server.commands;

import swcnoops.server.requests.CommandResult;

public class Command {
    private String action;
    private Object args;
    private Long requestId;
    private Long time;
    private String token;
    private CommandAction commandAction;
    private CommandResult commandResult;

    public String getAction() {
        return action;
    }

    public Object getArgs() {
        return args;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getToken() {
        return token;
    }

    public void setCommandAction(CommandAction commandAction) {
        this.commandAction = commandAction;
    }

    public CommandAction getCommandAction() {
        return commandAction;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setResponse(CommandResult commandResult) {
        this.commandResult = commandResult;
    }

    public CommandResult getResponse() {
        return commandResult;
    }
}
