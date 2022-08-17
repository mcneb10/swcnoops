package swcnoops.server.model;

import java.util.HashMap;
import java.util.Map;

public class Participant {
    public String id;
    public String name;
    /**
     * Number of attacks left
     */
    public int turns;
    /**
     * Number of stars left
     */
    public int victoryPoints;
    public int attacksWon;
    public int defensesWon;
    public int score;
    public int level;

    // TODO - dont know what this does yet
    public Map<String, Long> currentlyDefending = new HashMap<>();
}
