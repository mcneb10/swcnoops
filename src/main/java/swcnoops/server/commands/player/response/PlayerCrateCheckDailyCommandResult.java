package swcnoops.server.commands.player.response;

import swcnoops.server.model.Available;
import swcnoops.server.model.InProgress;
import swcnoops.server.requests.AbstractCommandResult;

public class PlayerCrateCheckDailyCommandResult extends AbstractCommandResult {
    public Available available;
    public InProgress inProgress;
    public long next;
    public long nextDailyCrateTime;
    public Object nextHolonetTime;
}
