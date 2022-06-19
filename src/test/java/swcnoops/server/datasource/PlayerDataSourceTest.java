package swcnoops.server.datasource;

import org.junit.Test;
import swcnoops.server.Config;
import swcnoops.server.ServiceFactory;

import static org.junit.Assert.assertNotNull;

public class PlayerDataSourceTest {
    static {
        ServiceFactory.instance(new Config());
    }

    @Test
    public void loadPlayerTest() throws Exception {
        PlayerDataSource playerDataSource = ServiceFactory.instance().getPlayerDatasource();
        playerDataSource.initOnStartup();
        Player player = playerDataSource.loadPlayer("2c2d4aea-7f38-11e5-a29f-069096004f69");
        assertNotNull(player);
    }
}
