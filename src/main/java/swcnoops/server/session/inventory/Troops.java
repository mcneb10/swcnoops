package swcnoops.server.session.inventory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Troops {
    private HashMap<String,Integer> troops = new HashMap<>();
    private List<TroopUpgrade> upgrades = new LinkedList<>();
    private long time;

    public HashMap<String, Integer> getTroops() {
        return troops;
    }

    public List<TroopUpgrade> getUpgrades() {
        return upgrades;
    }

    public void setTroops(HashMap<String, Integer> troops) {
        this.troops = troops;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
