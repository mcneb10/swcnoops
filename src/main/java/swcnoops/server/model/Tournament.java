package swcnoops.server.model;

import java.util.List;

public class Tournament extends AbstractTimedEvent {
    public int rating;
    public List<String> redeemedRewards;
    public TournamentRank currentRank;
    public TournamentRank finalRank;
}
