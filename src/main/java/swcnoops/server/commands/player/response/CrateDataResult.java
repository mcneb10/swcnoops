package swcnoops.server.commands.player.response;

import swcnoops.server.model.CrateData;
import swcnoops.server.requests.AbstractCommandResult;

public class CrateDataResult extends AbstractCommandResult {
    private CrateData crateData;

    @Override
    public Object getResult() {
        return this.getCrateData();
    }

    public CrateData getCrateData() {
        return crateData;
    }

    public void setCrateData(CrateData crateData) {
        this.crateData = crateData;
    }
}
