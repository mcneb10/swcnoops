package swcnoops.server.requests;

import swcnoops.server.commands.Command;

import java.util.List;
import java.util.Optional;

public class Batch {
    private String authKey;
    private Boolean pickupMessages;
    private Long lastLoginTime;
    private List<Command> commands;

    public List<Command> getCommands() {
        return commands;
    }
}
