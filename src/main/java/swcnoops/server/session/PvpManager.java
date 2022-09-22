package swcnoops.server.session;

import swcnoops.server.game.PvpMatch;

public interface PvpManager {
    PvpMatch getNextMatch();

    PvpMatch getCurrentPvPMatch();

    PlayerSession getPlayerSession();

    void pvpReleaseTarget();

    void playerLogin();
}
