package swcnoops.server.datasource;

public class PlayerSecret {
    private final boolean missingSecret;
    private String playerId;
    private String secret;
    private String secondaryAccount;

    public PlayerSecret(String playerId, String secret, String secondaryAccount, boolean missingSecret) {
        this.playerId = playerId;
        this.secret = secret;
        this.secondaryAccount = secondaryAccount;
        this.missingSecret = missingSecret;
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

    public boolean isMissingSecret() {
        return missingSecret;
    }
}
