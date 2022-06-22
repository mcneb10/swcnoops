package swcnoops.server.datasource;

import swcnoops.server.model.PlayerMap;
import swcnoops.server.model.SubStorage;
import swcnoops.server.model.Upgrades;

public class PlayerSettings {
    private final String playerId;
    private Upgrades upgrades;
    private String name;
    private String faction;
    private PlayerMap baseMap;
    private SubStorage troopsOnTransport;

    public PlayerSettings(String playerId) {
        this.playerId = playerId;
    }

    public Upgrades getUpgrades() {
        return upgrades;
    }

    public void setUpgrades(Upgrades upgrades) {
        this.upgrades = upgrades;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    public String getFaction() {
        return faction;
    }

    public void setBaseMap(PlayerMap baseMap) {
        this.baseMap = baseMap;
    }

    public PlayerMap getBaseMap() {
        return baseMap;
    }

    public SubStorage getTroopsOnTransport() {
        if (this.troopsOnTransport == null)
            this.troopsOnTransport = new SubStorage();

        return this.troopsOnTransport;
    }
}
