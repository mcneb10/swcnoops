package swcnoops.server.game;

import org.junit.Test;
import swcnoops.server.Config;
import swcnoops.server.ServiceFactory;

import static org.junit.Assert.assertNotNull;

public class GameDataManagerImplTest {
    static {
        ServiceFactory.instance(new Config());
    }

    @Test
    public void test() {
        GameDataManagerImpl gameDataManager = new GameDataManagerImpl();
        gameDataManager.initOnStartup();
        TroopData troopData = gameDataManager.getTroopDataByUid("troopHeroATMP1");
        assertNotNull(troopData);
    }
}
