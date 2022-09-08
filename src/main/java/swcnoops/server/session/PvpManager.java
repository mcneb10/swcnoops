package swcnoops.server.session;

import swcnoops.server.game.PvpMatch;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public interface PvpManager {

    void addBattle(String battleId, PvpMatch pvpMatch);

    void removeBattle(String battleId);

    HashMap<String, PvpMatch> getBattles();

    PvpMatch getNextMatch();
}
