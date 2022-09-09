package swcnoops.server.datasource;

public class Player {
    final private String playerId;
    private String secret;
    private PlayerSettings playerSettings;
    // this is a flag to indicate if the player logged in with an existing account that does not exists in the DB
    // this triggers recovery process in the client
    private boolean missingSecret;

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

    public void setMissingSecret(boolean missingSecret) {
        this.missingSecret = missingSecret;
    }

    public boolean isMissingSecret() {
        return missingSecret;
    }
}
