package swcnoops.server.datasource;

public class PlayerSecret {
    private String playerId;
    private String secret;
    private String secondaryAccount;

    public PlayerSecret(String playerId, String secret, String secondaryAccount) {
        this.playerId = playerId;
        this.secret = secret;
        this.secondaryAccount = secondaryAccount;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getSecret() {
        return secret;
    }

    public String getSecondaryAccount() {
        return secondaryAccount;
    }
}
