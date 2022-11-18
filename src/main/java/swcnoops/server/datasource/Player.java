package swcnoops.server.datasource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.mongojack.Id;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {
    @Id
    private String playerId;

    private PlayerSecret playerSecret;

    private PlayerSettings playerSettings;
    private long keepAlive;
    private Date loginDate;
    private PvpAttack currentPvPAttack;
    private PvpAttack currentPvPDefence;
    private long loginTime;

    private Map<String, Long> receivedDonations = new HashMap<>();

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

    public PvpAttack getCurrentPvPAttack() {
        return currentPvPAttack;
    }

    public void setCurrentPvPAttack(PvpAttack currentPvPAttack) {
        this.currentPvPAttack = currentPvPAttack;
    }

    public PvpAttack getCurrentPvPDefence() {
        return currentPvPDefence;
    }

    public void setCurrentPvPDefence(PvpAttack currentPvPDefence) {
        this.currentPvPDefence = currentPvPDefence;
    }

    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }

    public long getLoginTime() {
        return loginTime;
    }

    public Map<String, Long> getReceivedDonations() {
        return receivedDonations;
    }
}
