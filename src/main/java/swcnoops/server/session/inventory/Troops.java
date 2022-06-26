package swcnoops.server.session.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Troops {
    final private HashMap<String,TroopRecord> troops = new HashMap<>();
    final private HashMap<String,TroopRecord> specialAttacks = new HashMap<>();
    @JsonIgnore
    final private HashMap<String,TroopRecord> troopRecords = new HashMap<>();
    final private List<TroopUpgrade> upgrades = new LinkedList<>();
    private long time;

    public HashMap<String, TroopRecord> getTroops() {
        return troops;
    }

    public HashMap<String, TroopRecord> getTroopRecords() {
        return troopRecords;
    }

    public HashMap<String, TroopRecord> getSpecialAttacks() {
        return specialAttacks;
    }

    public List<TroopUpgrade> getUpgrades() {
        return upgrades;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
