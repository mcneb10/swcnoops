package swcnoops.server.datasource;

public class DefendingWarParticipant {
    private String playerId;
    private int victoryPoints;

    public DefendingWarParticipant(String playerId, int victoryPoints) {
        this.playerId = playerId;
        this.victoryPoints = victoryPoints;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public int getVictoryPoints() {
        return victoryPoints;
    }
}
