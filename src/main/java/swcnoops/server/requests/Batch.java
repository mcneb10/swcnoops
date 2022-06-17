package swcnoops.server.requests;

import swcnoops.server.commands.Command;

import java.util.List;
import java.util.Optional;

public class Batch {
    private String authKey;
    private Boolean pickupMessages;
    private Long lastLoginTime;
    private List<Command> commands;

    public Command getCommandByRequestId(long requestId)
    {
        Command command = null;
        if (commands != null) {
            Optional<Command> found = commands.stream().filter(c -> c.getRequestId() != null && c.getRequestId().equals(requestId)).findFirst();
            if (found.isPresent())
                command = found.get();
        }

        return command;
    }

    public List<Command> getCommands() {
        return commands;
    }
}
