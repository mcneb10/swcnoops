package swcnoops.server.datasource;

public class Player {
    final private String playerId;
    private String secret;
    private PlayerSettings playerSettings;

    public Player(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getSecret() {
        return secret;
    }

    public PlayerSettings getPlayerSettings() {
        return playerSettings;
    }

    public void setPlayerSettings(PlayerSettings playerSettings) {
        this.playerSettings = playerSettings;
    }
}
