package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.game.PvpMatch;

import java.util.*;

public class PvpSessionImpl implements PvpManager {
    private Set<String> playersSeen = new HashSet<>();
    private Set<String> devBasesSeen = new HashSet<>();

    private PlayerSession playerSession;

    private PvpMatch currentPvPMatch;

    public PvpSessionImpl(PlayerSession playerSession) {
        this.playerSession = playerSession;

    }

    // Simple match for now to prioritise any random real player before looking at dev bases
    @Override
    public PvpMatch getNextMatch() {
        PvpMatch pvpMatch = ServiceFactory.instance().getPlayerDatasource().getPvPMatches(this, this.playersSeen);

        if (pvpMatch != null) {
            this.playersSeen.add(pvpMatch.getParticipantId());
        }

        if (pvpMatch == null) {
            pvpMatch = ServiceFactory.instance().getPlayerDatasource().getDevBaseMatches(this, this.devBasesSeen);

            if (pvpMatch != null) {
                this.devBasesSeen.add(pvpMatch.getParticipantId());
            } else {
                this.playersSeen.clear();
                this.devBasesSeen.clear();
            }
        }

        this.currentPvPMatch = pvpMatch;
        return pvpMatch;
    }

    @Override
    public PlayerSession getPlayerSession() {
        return this.playerSession;
    }

    @Override
    public PvpMatch getMatch() {
        return getCurrentPvPMatch();
    }

    public PvpMatch getCurrentPvPMatch() {
        return currentPvPMatch;
    }

    @Override
    public void pvpReleaseTarget() {
        ServiceFactory.instance().getPlayerDatasource().pvpReleaseTarget(this);
        this.currentPvPMatch = null;
    }

    @Override
    public void playerLogin() {
        // TODO - maybe reset the player Ids we have already seen for PvP
        this.playersSeen.clear();
        this.devBasesSeen.clear();
        this.currentPvPMatch = null;
    }
}
