package swcnoops.server.session;

import swcnoops.server.ServiceFactory;
import swcnoops.server.game.PvpMatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class PvpSessionImpl implements PvpManager {

    private HashMap<String, PvpMatch> pvpMatches;

    private PlayerSession playerSession;

    public PvpSessionImpl(PlayerSession playerSession) {
        this.playerSession = playerSession;

    }

    @Override
    public HashMap<String, PvpMatch> getBattles() {
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
        Random random = new Random();
        if (this.pvpMatches == null) {
            this.pvpMatches = ServiceFactory.instance().getPlayerDatasource().getDevBaseMatches(playerSession);
        }
        String[] battleId = pvpMatches.keySet().toArray(new String[0]);
        int matchIndex = random.nextInt(pvpMatches.size());
        return pvpMatches.get(battleId[matchIndex]);
    }

    @Override
    public PvpMatch getMatch(String battleId) {
        return this.pvpMatches.get(battleId);
    }
}
