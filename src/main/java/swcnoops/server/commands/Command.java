package swcnoops.server.commands;

import swcnoops.server.requests.CommandResult;

public class Command {
    private String action;
    private Object args;
    private long requestId;
    private long time;
    private String token;

    private boolean attachGuildNotification = false;

    private CommandAction commandAction;
    private CommandResult commandResult;

    public String getAction() {
        return action;
    }

    public Object getArgs() {
        return args;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
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

    public void setAttachGuildNotification(boolean attachGuildNotification) {
        this.attachGuildNotification = attachGuildNotification;
    }

    public boolean getAttachGuildNotification() {
        return attachGuildNotification;
    }
}
