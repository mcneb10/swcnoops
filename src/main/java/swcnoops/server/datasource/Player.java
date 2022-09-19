package swcnoops.server.datasource;

import org.mongojack.Id;

import java.util.Date;

public class Player {
    @Id
    private String playerId;

    private PlayerSecret playerSecret;

    private PlayerSettings playerSettings;
    private long keepAlive;
    private Date loginDate;

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

    public void setKeepAlive(long keepAlive) {
        this.keepAlive = keepAlive;
    }

    public long getKeepAlive() {
        return keepAlive;
    }

    public void setLoginDate(Date loginDate) {
        this.loginDate = loginDate;
    }

    public Date getLoginDate() {
        return loginDate;
    }
}
