package swcnoops.server.game;

import org.junit.Test;
import swcnoops.server.Config;
import swcnoops.server.ServiceFactory;

import java.sql.Timestamp;

import static org.junit.Assert.assertNotNull;

public class GameDataManagerImplTest {
    static {
        ServiceFactory.instance(new Config());
    }

    @Test
    public void test() {
        GameDataManager gameDataManager = new GameDataManagerImpl();
        gameDataManager.initOnStartup();
        TroopData troopData = gameDataManager.getTroopDataByUid("troopEmpireRageRancorCreature10");
        assertNotNull(troopData);
        BuildingData buildingData = gameDataManager.getBuildingDataByUid("empireTrapDropshipCreature10");
        assertNotNull(buildingData);
        troopData = gameDataManager.getTroopDataByUid("specialAttackEmpireCreatureDropship10");
        assertNotNull(troopData);
        TrapData trapData = gameDataManager.getTrapDataByUid(buildingData.getTrapId());
        assertNotNull(trapData);
        buildingData = gameDataManager.getBuildingDataByUid("rebelPlatformDroideka50");
        assertNotNull(buildingData);
        troopData = gameDataManager.getTroopDataByUid("troopChampionRebelDroideka50");
        assertNotNull(troopData);
        buildingData = gameDataManager.getBuildingDataByUid("rebelPlatformHeavyDroideka47");
        assertNotNull(buildingData);
        troopData = gameDataManager.getTroopDataByUid("specialAttackEmpireHauler11");
        assertNotNull(troopData);
        assertNotNull(troopData.getUnitId());
        troopData = gameDataManager.getTroopDataByUid("troopMercenaryEmpireRider1");
        assertNotNull(troopData);
        assertNotNull(troopData.getUnitId());
        troopData = gameDataManager.getTroopDataByUid("troopSniper1");
        assertNotNull(troopData);
        assertNotNull(troopData.getUnitId());
    }
}
