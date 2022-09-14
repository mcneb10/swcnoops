package swcnoops.server.datasource;

public class PlayerSecret {
    private String secret;
    private String secondaryAccount;
    private boolean missingSecret;

    public PlayerSecret() {
    }

    public PlayerSecret(String secret, String secondaryAccount, boolean missingSecret) {
        this.secret = secret;
        this.secondaryAccount = secondaryAccount;
        this.missingSecret = missingSecret;
    }

    public String getSecret() {
        return secret;
    }

    public String getSecondaryAccount() {
        return secondaryAccount;
    }

    public boolean getMissingSecret() {
        return missingSecret;
    }

    public void setMissingSecret(boolean missingSecret) {
        this.missingSecret = missingSecret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setSecondaryAccount(String secondaryAccount) {
        this.secondaryAccount = secondaryAccount;
    }
}
