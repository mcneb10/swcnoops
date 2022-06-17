package swcnoops.server.commands.player.response;

import swcnoops.server.requests.AbstractCommandResult;

public class PlayerDeviceRegisterResult extends AbstractCommandResult {
    public long credits;
    public long materials;
    public long contraband;
    public long crystals;
    public long reputation;
}
