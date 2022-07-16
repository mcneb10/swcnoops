package swcnoops.server.commands.player.response;

import swcnoops.server.model.CrateData;
import swcnoops.server.requests.AbstractCommandResult;

public class PlayerStoreCrateBuyResult extends AbstractCommandResult {
    private CrateData crateData = new CrateData();

    @Override
    public Object getResult() {
        return getCrateData();
    }

    public CrateData getCrateData() {
        return crateData;
    }
}
