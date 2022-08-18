package swcnoops.server.requests;

import swcnoops.server.commands.Command;

import java.util.List;

public class Batch {
    private String authKey;
    private Boolean pickupMessages;
    private Long lastLoginTime;
    private List<Command> commands;

    public List<Command> getCommands() {
        return commands;
    }

    public String getAuthKey() {
        return authKey;
    }

    public Boolean getPickupMessages() {
        return pickupMessages;
    }

    public Long getLastLoginTime() {
        return lastLoginTime;
    }
}
