package swcnoops.server.datasource;

import swcnoops.server.datasource.buffers.RingBuffer;

public class TournamentLeaderBoard {
    private RingBuffer top50;
    private RingBuffer surroundingMe;
    private TournamentStat lastTournamentStat;

    public TournamentLeaderBoard(RingBuffer top50, RingBuffer surroundingMe, TournamentStat lastTournamentStat) {
        this.top50 = top50;
        this.surroundingMe = surroundingMe;
        this.lastTournamentStat = lastTournamentStat;
    }

    public RingBuffer getTop50() {
        return top50;
    }

    public RingBuffer getSurroundingMe() {
        return surroundingMe;
    }

    public TournamentStat getLastTournamentStat() {
        return lastTournamentStat;
    }
}
