package swcnoops.server.session;

import swcnoops.server.game.PvpMatch;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PvpSessionImpl implements PvpManager {
    private List<PvpMatch> pvpMatchList;
    private ConcurrentHashMap<String, PvpMatch> pvpMatches = new ConcurrentHashMap<>();

    private PlayerSession playerSession;

    int currentMatch = 0;

    public PvpSessionImpl(PlayerSession playerSession) {
        this.playerSession = playerSession;
    }

    @Override
    public ConcurrentHashMap<String, PvpMatch> getBattles() {
        return pvpMatches;
    }

    @Override
    public void addBattle(String battleId, PvpMatch pvpMatch) {
        this.pvpMatches.put(battleId, pvpMatch);
    }

    @Override
    public void removeBattle(String battleId) {
        this.pvpMatches.remove(battleId);
    }

    @Override
    public PvpMatch getNextMatch() {
        int matchIndex = currentMatch <= pvpMatchList.size() ? currentMatch : 0;
        currentMatch++;
        return pvpMatchList.get(matchIndex);
    }


}
