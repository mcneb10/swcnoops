package swcnoops.server.game;

import swcnoops.server.datasource.Creature;
import swcnoops.server.model.*;
import swcnoops.server.session.inventory.Troops;

import java.util.Map;

public class PvpMatch {
    public int creditsCharged;
    private String battleId;
    private String playerId;
    private String participantId;
    private String name;
    private long battleDate;

    private FactionType factionType;
    private boolean isDevBase;
    private int level;
    private int defenderXp;

    private int potentialScoreWin;
    private int potentialScoreLose;
    private BattleParticipant attacker;
    private BattleParticipant defender;

    private JsonStringArrayList attackerEquipment;
    private JsonStringArrayList defenderEquipment;
    private int creditsGained;
    private int materialsGained;
    private int contraGained;
    private Map<String, Integer> defenderDamagedBuildings;
    private Scalars defendersScalars;
    private InventoryStorage defendersInventoryStorage;
    private String defendersName;
    private String defendersGuildId;
    private String defendersGuildName;
    private PlayerMap defendersBaseMap;

    private boolean revenge;
    private DonatedTroops defendersDonatedTroops;
    private Map<String, Integer> defendersDeployableTroopsChampion;
    private Creature defendersCreature;
    private Troops defendersTroops;

    public PvpMatch() {
    }

    public boolean isRevenge() {
        return revenge;
    }

    public void setRevenge(boolean revenge) {
        this.revenge = revenge;
    }

    public String getBattleId() {
        return battleId;
    }

    public void setBattleId(String battleId) {
        this.battleId = battleId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getBattleDate() {
        return battleDate;
    }

    public void setBattleDate(long battleDate) {
        this.battleDate = battleDate;
    }

    public FactionType getFactionType() {
        return factionType;
    }

    public void setFactionType(FactionType factionType) {
        this.factionType = factionType;
    }

    public boolean isDevBase() {
        return isDevBase;
    }

    public void setDevBase(boolean devBase) {
        isDevBase = devBase;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getDefenderXp() {
        return defenderXp;
    }

    public void setDefenderXp(int defenderXp) {
        this.defenderXp = defenderXp;
    }

    public BattleParticipant getAttacker() {
        return attacker;
    }

    public void setAttacker(BattleParticipant attacker) {
        this.attacker = attacker;
    }

    public BattleParticipant getDefender() {
        return defender;
    }

    public void setDefender(BattleParticipant defender) {
        this.defender = defender;
    }

    public int getPotentialScoreWin() {
        return potentialScoreWin;
    }

    public void setPotentialScoreWin(int potentialScoreWin) {
        this.potentialScoreWin = potentialScoreWin;
    }

    public int getPotentialScoreLose() {
        return potentialScoreLose;
    }

    public void setPotentialScoreLose(int potentialScoreLose) {
        this.potentialScoreLose = potentialScoreLose;
    }

    public void setAttackerDelta(int delta) {
        this.attacker.attackRatingDelta = delta;
    }

    public void setDefenderDelta(int delta) {
        this.defender.defenseRatingDelta = delta;
    }

    public JsonStringArrayList getAttackerEquipment() {
        return attackerEquipment;
    }

    public void setAttackerEquipment(JsonStringArrayList attackerEquipment) {
        this.attackerEquipment = attackerEquipment;
    }

    public JsonStringArrayList getDefenderEquipment() {
        return defenderEquipment;
    }

    public void setDefenderEquipment(JsonStringArrayList defenderEquipment) {
        this.defenderEquipment = defenderEquipment;
    }

    public void setCreditsGained(int creditsGained) {
        this.creditsGained = creditsGained;
    }

    public int getCreditsGained() {
        return creditsGained;
    }

    public void setMaterialsGained(int materialsGained) {
        this.materialsGained = materialsGained;
    }

    public int getMaterialsGained() {
        return materialsGained;
    }

    public void setContraGained(int contraGained) {
        this.contraGained = contraGained;
    }

    public int getContraGained() {
        return contraGained;
    }

    public void setDefenderDamagedBuildings(Map<String, Integer> defenderDamagedBuildings) {
        this.defenderDamagedBuildings = defenderDamagedBuildings;
    }

    public Map<String, Integer> getDefenderDamagedBuildings() {
        return defenderDamagedBuildings;
    }

    public void setDefendersScalars(Scalars defendersScalars) {
        this.defendersScalars = defendersScalars;
    }

    public Scalars getDefendersScalars() {
        return defendersScalars;
    }

    public void setDefendersInventoryStorage(InventoryStorage defendersInventoryStorage) {
        this.defendersInventoryStorage = defendersInventoryStorage;
    }

    public InventoryStorage getDefendersInventoryStorage() {
        return defendersInventoryStorage;
    }

    public String getDefendersName() {
        return defendersName;
    }

    public void setDefendersName(String defendersName) {
        this.defendersName = defendersName;
    }

    public String getDefendersGuildId() {
        return defendersGuildId;
    }

    public void setDefendersGuildId(String defendersGuildId) {
        this.defendersGuildId = defendersGuildId;
    }

    public String getDefendersGuildName() {
        return defendersGuildName;
    }

    public void setDefendersGuildName(String defendersGuildName) {
        this.defendersGuildName = defendersGuildName;
    }

    public PlayerMap getDefendersBaseMap() {
        return defendersBaseMap;
    }

    public void setDefendersBaseMap(PlayerMap defendersBaseMap) {
        this.defendersBaseMap = defendersBaseMap;
    }

    public DonatedTroops getDefendersDonatedTroops() {
        return defendersDonatedTroops;
    }

    public void setDefendersDonatedTroops(DonatedTroops defendersDonatedTroops) {
        this.defendersDonatedTroops = defendersDonatedTroops;
    }

    public void setDefendersDeployableTroopsChampion(Map<String, Integer> defendersDeployableTroopsChampion) {
        this.defendersDeployableTroopsChampion = defendersDeployableTroopsChampion;
    }

    public Map<String, Integer> getDefendersDeployableTroopsChampion() {
        return defendersDeployableTroopsChampion;
    }

    public void setDefendersCreature(Creature defendersCreature) {
        this.defendersCreature = defendersCreature;
    }

    public Creature getDefendersCreature() {
        return defendersCreature;
    }

    public void setDefendersTroops(Troops defendersTroops) {
        this.defendersTroops = defendersTroops;
    }

    public Troops getDefendersTroops() {
        return defendersTroops;
    }
}



