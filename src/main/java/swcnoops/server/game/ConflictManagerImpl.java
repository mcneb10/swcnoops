package swcnoops.server.game;

import swcnoops.server.ServiceFactory;
import swcnoops.server.datasource.TournamentLeaderBoard;
import swcnoops.server.datasource.TournamentStat;
import swcnoops.server.datasource.buffers.RingBuffer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConflictManagerImpl implements ConflictManager {
    private volatile List<TournamentData> conflicts = new ArrayList<>();
    private Map<String, TournamentData> planetConflicts = new ConcurrentHashMap<>();
    private TournamentTierData topTier;

    public void setup(List<TournamentData> validConflicts) {
        long now = ServiceFactory.getSystemTimeSecondsFromEpoch();

        if (validConflicts != null) {
            // sort them in order
            validConflicts.sort((a,b) -> Long.compare(a.getStartTime(), b.getStartTime()));
            this.conflicts = new ArrayList<>(validConflicts);
            for (TournamentData tournamentData : this.conflicts) {
                if (tournamentData.isActive(now)) {
                    this.planetConflicts.put(tournamentData.planetId, tournamentData);
                }
            }
        }

        List<String> planetKeys = new ArrayList<>(this.planetConflicts.keySet());
        for (String planetId : planetKeys) {
            TournamentData tournamentData = this.planetConflicts.get(planetId);
            if (tournamentData != null) {
                if (tournamentData.hasExpired(now)) {
                    this.planetConflicts.remove(planetId);
                }
            }
        }
    }

    @Override
    public TournamentData getConflict(String planetId) {
        return this.planetConflicts.get(planetId);
    }

    public void setTopTier(TournamentTierData topTier) {
        this.topTier = topTier;
    }

    public TournamentTierData getTopTier() {
        return topTier;
    }

    @Override
    public void calculatePercentile(TournamentLeaderBoard leaderBoard) {
        if (leaderBoard != null) {
            calculatePercentile(leaderBoard.getTop50(), leaderBoard.getLastTournamentStat());
            calculatePercentile(leaderBoard.getSurroundingMe(), leaderBoard.getLastTournamentStat());
        }
    }

    @Override
    public TournamentStat getTournamentStats(List<TournamentStat> tournaments, TournamentData tournamentData) {
        TournamentStat tournamentStat = null;
        if (tournaments != null && tournamentData != null) {
            for (TournamentStat stat : tournaments) {
                if (stat.uid.equals(tournamentData.getUid())) {
                    tournamentStat = stat;
                    break;
                }
            }
        }

        return tournamentStat;
    }

    private void calculatePercentile(RingBuffer top50, TournamentStat lastTournamentStat) {
        Iterator<TournamentStat> statIterator = top50.iterator();

        float maxRank = lastTournamentStat.rank;
        float maxTierRank = 100 / this.getTopTier().percentage;
        maxRank = Math.max(maxTierRank, maxRank);

        while (statIterator.hasNext()) {
            TournamentStat stat = statIterator.next();
            stat.percentile = 100 * (stat.rank / maxRank);
        }
    }
}
