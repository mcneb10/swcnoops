package swcnoops.server.session;

import swcnoops.server.game.PvpMatch;

import java.util.concurrent.ConcurrentHashMap;

public class PvpSessionImpl implements PvpManager {

    private ConcurrentHashMap<String, PvpMatch> pvpMatches = new ConcurrentHashMap<>();


    public PvpSessionImpl() {

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


}
