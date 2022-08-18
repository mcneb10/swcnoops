package swcnoops.server.model;

public class WarNotificationData implements SquadNotificationData {
    private String warId;
    private String opponentId;
    private String opponentName;
    private boolean captured;
    private String buffBaseUid;
    private int stars;
    private int victoryPoints;
    private long attackExpirationDate;

    public WarNotificationData() {
    }

    public WarNotificationData(String warId) {
        this.warId = warId;
    }

    public String getWarId() {
        return warId;
    }

    public void setOpponentId(String opponentId) {
        this.opponentId = opponentId;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public String getOpponentId() {
        return opponentId;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public boolean isCaptured() {
        return captured;
    }

    public String getBuffBaseUid() {
        return buffBaseUid;
    }

    public int getStars() {
        return stars;
    }

    public int getVictoryPoints() {
        return victoryPoints;
    }

    public long getAttackExpirationDate() {
        return attackExpirationDate;
    }

    public void setAttackExpirationDate(long attackExpirationDate) {
        this.attackExpirationDate = attackExpirationDate;
    }
}
