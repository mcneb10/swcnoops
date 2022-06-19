package swcnoops.server.session;

import swcnoops.server.datasource.Player;

public class PlayerSessionImpl implements PlayerSession {
    final private Player player;

    public PlayerSessionImpl(Player player) {
        this.player = player;
    }

    @Override
    public String getPlayerId() {
        return this.player.getPlayerId();
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
