package swcnoops.server.commands.guild.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import swcnoops.server.requests.AbstractCommandResult;
import swcnoops.server.requests.ResponseHelper;

public class BattleIdResult extends AbstractCommandResult {
    private String battleId;

    @JsonIgnore
    private Integer returnStatus;

    public BattleIdResult(Integer errorStatus) {
        this.returnStatus = errorStatus;
    }

    public BattleIdResult(String battleId) {
        this(ResponseHelper.RECEIPT_STATUS_COMPLETE);
        this.battleId = battleId;
    }

    public String getBattleId() {
        return battleId;
    }

    @JsonIgnore
    @Override
    public Integer getStatus() {
        return this.returnStatus;
    }
}
