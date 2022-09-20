package swcnoops.server.session;

import swcnoops.server.game.PvpMatch;

public interface PvpManager {
    PvpMatch getNextMatch();

    PvpMatch getMatch();

    PlayerSession getPlayerSession();

    void pvpReleaseTarget();

    void playerLogin();
}
