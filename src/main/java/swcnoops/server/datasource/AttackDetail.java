package swcnoops.server.datasource;

public class AttackDetail {
    final private String battleId;
    final private long expirationDate;
    public AttackDetail(String battleId, long expirationDate) {
        this.battleId = battleId;
        this.expirationDate = expirationDate;
    }

    public String getBattleId() {
        return battleId;
    }

    public long getExpirationDate() {
        return expirationDate;
    }
}
