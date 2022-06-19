package swcnoops.server.session;

import swcnoops.server.datasource.Player;

public interface PlayerSession {
    Player getPlayer();

    String getPlayerId();
}
