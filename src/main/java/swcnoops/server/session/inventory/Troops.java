package swcnoops.server.session.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import swcnoops.server.game.TroopData;

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

    public void initialiseMaps() {
        this.getTroops().forEach((a,b) -> this.getTroopRecords().put(a,b));
        this.getSpecialAttacks().forEach((a,b) -> this.getTroopRecords().put(a,b));
    }

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

    public void addTroop(TroopData troopData, long time) {
        TroopRecord troopRecord = new TroopRecord(troopData.getLevel(), time);

        if (troopData.isSpecialAttack())
            this.getSpecialAttacks().put(troopData.getUnitId(), troopRecord);
        else
            this.getTroops().put(troopData.getUnitId(), troopRecord);

        this.getTroopRecords().put(troopData.getUnitId(), troopRecord);
    }
}
