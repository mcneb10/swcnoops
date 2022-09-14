package swcnoops.server.datasource;

import org.mongojack.Id;

public class Player {
    @Id
    private String playerId;

    private PlayerSecret playerSecret;

    private PlayerSettings playerSettings;

    public Player() {
    }

    public Player(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public PlayerSettings getPlayerSettings() {
        return playerSettings;
    }

    public void setPlayerSettings(PlayerSettings playerSettings) {
        this.playerSettings = playerSettings;
    }

    public PlayerSecret getPlayerSecret() {
        return playerSecret;
    }

    public void setPlayerSecret(PlayerSecret playerSecret) {
        this.playerSecret = playerSecret;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
