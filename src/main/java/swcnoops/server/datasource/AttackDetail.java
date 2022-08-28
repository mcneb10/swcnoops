package swcnoops.server.datasource;

import swcnoops.server.requests.ResponseHelper;

public class AttackDetail extends WarNotification {
    final private String battleId;
    final private long expirationDate;
    private int returnCode;

    public AttackDetail(String battleId, long expirationDate) {
        this.battleId = battleId;
        this.expirationDate = expirationDate;
        this.returnCode = ResponseHelper.RECEIPT_STATUS_COMPLETE;
    }

    public AttackDetail(int returnCode) {
        this(null,0);
        this.returnCode = returnCode;
    }

    public String getBattleId() {
        return battleId;
    }

    public long getExpirationDate() {
        return expirationDate;
    }

    public int getReturnCode() {
        return returnCode;
    }
}
