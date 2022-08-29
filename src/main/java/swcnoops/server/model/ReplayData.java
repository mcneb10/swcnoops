package swcnoops.server.model;

import java.util.List;
import java.util.Map;

public class ReplayData {
    public CombatEncounter combatEncounter;
    public List<BattleAction> battleActions;
    public DeploymentData attackerDeploymentData;
    public DeploymentData defenderDeploymentData;
    public long lootCreditsAvailable;
    public long lootMaterialsAvailable;
    public long lootContrabandAvailable;
    public BattleType battleType;
    public long battleLength;
    public long lowFPS;
    public long lowFPSTime;
    public String battleVersion;
    public String planetId;
    public String manifestVersion;
    public BattleAttributes battleAttributes;
    public List<String> victoryConditions;
    public String failureCondition;
    public Map<String, Integer> donatedTroops;
    public Map<String, Integer> donatedTroopsAttacker;
    public Map<String, Integer> champions;
    public String defenseEncounterProfile;
    public String battleScript;
    public List<String> disabledBuildings;
    public long simSeedA;
    public long simSeedB;
    public double viewTimePreBattle;
    public List<String> attackerWarBuffs;
    public List<String> defenderWarBuffs;
    public List<String> attackerEquipment;
    public List<String> defenderEquipment;
    public List<CreatureTrapData> attackerCreatureTraps;
    public List<CreatureTrapData> defenderCreatureTraps;
}
