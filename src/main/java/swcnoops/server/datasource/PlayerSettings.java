package swcnoops.server.datasource;

import swcnoops.server.model.PlayerMap;
import swcnoops.server.model.Upgrades;
import swcnoops.server.session.inventory.Troops;
import swcnoops.server.session.training.BuildUnits;

public class PlayerSettings {
    private final String playerId;
    private Upgrades upgrades;
    private String name;
    private String faction;
    private PlayerMap baseMap;
    private Deployables deployableTroops;
    private BuildUnits buildUnits;
    private CreatureSettings creatureSettings;
    private Troops troops;

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

    public Deployables getDeployableTroops() {
        if (this.deployableTroops == null)
            this.deployableTroops = new Deployables();

        return this.deployableTroops;
    }

    public void setDeployableTroops(Deployables deployableTroops) {
        this.deployableTroops = deployableTroops;
    }

    public BuildUnits getBuildContracts() {
        return this.buildUnits;
    }

    public void setBuildContracts(BuildUnits buildUnits) {
        this.buildUnits = buildUnits;
    }

    public void setCreatureSettings(CreatureSettings creatureSettings) {
        this.creatureSettings = creatureSettings;
    }

    public CreatureSettings getCreatureSettings() {
        return creatureSettings;
    }

    public Troops getTroops() {
        return troops;
    }

    public void setTroops(Troops troops) {
        this.troops = troops;
    }
}
