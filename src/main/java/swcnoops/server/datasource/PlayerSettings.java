package swcnoops.server.datasource;

import swcnoops.server.model.*;
import swcnoops.server.session.inventory.Troops;
import swcnoops.server.session.training.BuildUnits;

public class PlayerSettings {
    private Upgrades upgrades = new Upgrades();
    private String name;
    private FactionType faction;
    public PlayerMap baseMap;
    private Deployables deployableTroops = new Deployables();
    private BuildUnits buildContracts = new BuildUnits();
    private Creature creature;
    private Troops troops = new Troops();
    private DonatedTroops donatedTroops = new DonatedTroops();
    private InventoryStorage inventoryStorage;
    private String currentQuest;
    private PlayerCampaignMission playerCampaignMission;
    private PreferencesMap sharedPreferences;
    private String guildId;
    private UnlockedPlanets unlockedPlanets;
    private Scalars scalars = new Scalars();

    private long keepAlive;
    private int hqLevel;

    public PlayerSettings() {
    }

    // TODO - not used at the moment, will probably be used for samples, will have to rename if do
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

    public void setFaction(FactionType faction) {
        this.faction = faction;
    }

    public FactionType getFaction() {
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
        return this.buildContracts;
    }

    public void setBuildContracts(BuildUnits buildUnits) {
        this.buildContracts = buildUnits;
    }

    public void setCreature(Creature creature) {
        this.creature = creature;
    }

    public Creature getCreature() {
        return creature;
    }

    public Troops getTroops() {
        return troops;
    }

    public void setTroops(Troops troops) {
        if (troops != null)
            troops.initialiseMaps();

        this.troops = troops;
    }

    public DonatedTroops getDonatedTroops() {
        return donatedTroops;
    }

    public void setDonatedTroops(DonatedTroops donatedTroops) {
        this.donatedTroops = donatedTroops;
    }

    public InventoryStorage getInventoryStorage() {
        return inventoryStorage;
    }

    public void setInventoryStorage(InventoryStorage inventoryStorage) {
        this.inventoryStorage = inventoryStorage;
    }

    public void setCurrentQuest(String currentQuest) {
        this.currentQuest = currentQuest;
    }

    public String getCurrentQuest() {
        return currentQuest;
    }

    public void setPlayerCampaignMission(PlayerCampaignMission playerCampaignMission) {
        this.playerCampaignMission = playerCampaignMission;
    }

    public PlayerCampaignMission getPlayerCampaignMission() {
        return playerCampaignMission;
    }

    public PreferencesMap getSharedPreferences() {
        return sharedPreferences;
    }

    public void setSharedPreferences(PreferencesMap sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public String getGuildId() {
        return guildId;
    }

    public UnlockedPlanets getUnlockedPlanets() {
        return unlockedPlanets;
    }

    public void setUnlockedPlanets(UnlockedPlanets unlockedPlanets) {
        this.unlockedPlanets = unlockedPlanets;
    }

    public Scalars getScalars() {
        if (this.scalars == null)
            this.scalars = new Scalars(0, 0, 0, 0, 0, 0, 100, 0, 0, 0);

        return scalars;
    }

    public void setScalars(Scalars scalars) {
        this.scalars = scalars;
    }

    public long getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(long keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void setHqLevel(int hqLevel) {
        this.hqLevel = hqLevel;
    }

    public int getHqLevel() {
        return hqLevel;
    }
}
