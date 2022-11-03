package swcnoops.server.commands.player.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import swcnoops.server.model.Raid;
import swcnoops.server.requests.AbstractCommandResult;

public class RaidDefenseCompleteResult extends AbstractCommandResult {
    private String awardedCrateUid;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Raid raidData;

    // TODO - to finish
    //private String crates;
    public String getAwardedCrateUid() {
        return awardedCrateUid;
    }

    public void setAwardedCrateUid(String awardedCrateUid) {
        this.awardedCrateUid = awardedCrateUid;
    }

    public Raid getRaidData() {
        return raidData;
    }

    public void setRaidData(Raid raidData) {
        this.raidData = raidData;
    }
}
