package swcnoops.server.commands.player.response;

import swcnoops.server.requests.AbstractCommandResult;

public class PlayerIdentitySwitchResult extends AbstractCommandResult {
    private String playerId;

    public PlayerIdentitySwitchResult(String playerId) {
        this.playerId = playerId;
    }

    @Override
    public Object getResult() {
        return getPlayerId();
    }

    public String getPlayerId() {
        return playerId;
    }
}
