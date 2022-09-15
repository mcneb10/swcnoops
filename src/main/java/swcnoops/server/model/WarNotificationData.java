package swcnoops.server.model;

public class WarNotificationData extends SquadNotificationData {
    private String warId;
    private String opponentId;
    private String opponentName;
    private boolean captured;
    private String buffBaseUid;
    private int stars;
    private int victoryPoints;
    private long attackExpirationDate;

    public WarNotificationData() {
        super("WarNotification");
    }

    public WarNotificationData(String warId) {
        this();
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

    public void setStars(int stars) {
        this.stars = stars;
    }

    public void setVictoryPoints(int victoryPoints) {
        this.victoryPoints = victoryPoints;
    }
}
