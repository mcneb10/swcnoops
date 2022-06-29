package swcnoops.server.model;

import java.util.List;
import java.util.Map;

public class ReplayData {
    //public CombatEncounter combatEncounter;
    public List<BattleAction> battleActions;
    public DeploymentData attackerDeploymentData;
    public DeploymentData defenderDeploymentData;
    public long lootCreditsAvailable;
    public long lootMaterialsAvailable;
    public long lootContrabandAvailable;
    public String battleType;
    public long battleLength;
    public long lowFPS;
    public long lowFPSTime;
    public String battleVersion;
    public String planetId;
    public String manifestVersion;
    //public BattleAttributes battleAttributes;
    public List<String> victoryConditions;
    public String failureCondition;
    //public DonatedTroops donatedTroops;
    //public DonatedTroopsAttacker donatedTroopsAttacker;
    public Map<String, Integer> champions;
    //public List<object> disabledBuildings;
    public long simSeedA;
    public long simSeedB;
    public double viewTimePreBattle;
    //public List<object> defenderEquipment;
    //public List<AttackerCreatureTrap> attackerCreatureTraps;
    //public List<DefenderCreatureTrap> defenderCreatureTraps;
}
