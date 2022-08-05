package swcnoops.server.commands;

import org.junit.Ignore;
import swcnoops.server.Config;
import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.player.PlayerPvpBattleComplete;
import swcnoops.server.commands.player.response.PlayerPvpBattleCompleteCommandResult;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class PlayerPvpBattleCompleteTest {
    static {
        ServiceFactory.instance(new Config());
        ServiceFactory.instance().getPlayerDatasource().initOnStartup();
        ServiceFactory.instance().getGameDataManager().initOnStartup();
    }

    @Ignore
    @Test
    public void test() throws Exception {
        PlayerPvpBattleComplete action = new PlayerPvpBattleComplete();
        PlayerPvpBattleCompleteCommandResult response = action.execute(new PlayerPvpBattleComplete(), 1);
        assertNotNull(response);
    }
}
