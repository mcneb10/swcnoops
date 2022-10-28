package swcnoops.server.model;

import java.util.List;

public class Tournament extends AbstractTimedEvent {
    public double percentile;
    public int rating;
    public int bestTier;
    public List<String> redeemedRewards;
    public TournamentRank finalRank;
}
