package swcnoops.server.commands.player.response;

import swcnoops.server.model.PlayerIdentityInfo;
import swcnoops.server.requests.AbstractCommandResult;

import java.util.HashMap;
import java.util.Map;

public class PlayerAccountExternalRegisterResult extends AbstractCommandResult {
    public Map<String, PlayerIdentityInfo> identities = new HashMap<>();
    public String secret = "asdas";
    public long registrationTime;
    public String derivedExternalAccountId;
    public int registrationReward;
    private int returnCode;

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    @Override
    public Integer getStatus() {
        return Integer.valueOf(returnCode);
    }
}
