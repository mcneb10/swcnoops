package swcnoops.server.datasource;

import swcnoops.server.model.PlayerMap;
import swcnoops.server.model.SubStorage;
import swcnoops.server.model.Upgrades;
import swcnoops.server.session.BuildContract;
import swcnoops.server.session.BuildContracts;

import java.util.List;

public class PlayerSettings {
    private final String playerId;
    private Upgrades upgrades;
    private String name;
    private String faction;
    private PlayerMap baseMap;
    private SubStorage troopsOnTransport;
    private BuildContracts buildContracts;

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

    public void setTroopsOnTransport(SubStorage troopsOnTransport) {
        this.troopsOnTransport = troopsOnTransport;
    }

    public BuildContracts getBuildContracts() {
        return this.buildContracts;
    }

    public void setBuildContracts(BuildContracts buildContracts) {
        this.buildContracts = buildContracts;
    }
}
