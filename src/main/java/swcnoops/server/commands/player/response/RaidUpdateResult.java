package swcnoops.server.commands.player.response;

import swcnoops.server.model.Raid;
import swcnoops.server.requests.AbstractCommandResult;

public class RaidUpdateResult extends AbstractCommandResult {
    public Raid raid;

    @Override
    public Object getResult() {
        return this.raid;
    }
}
