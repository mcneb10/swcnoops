package swcnoops.server.commands;

import swcnoops.server.Config;
import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.player.PlayerPvpBattleComplete;
import swcnoops.server.commands.player.response.PlayerPvpBattleCompleteCommandResult;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class PlayerPvpBattleCompleteTest {
    static {
        ServiceFactory.instance(new Config());
    }

    @Test
    public void test() throws Exception {
        PlayerPvpBattleComplete action = new PlayerPvpBattleComplete();
        PlayerPvpBattleCompleteCommandResult response = action.execute(null);
        assertNotNull(response);
    }
}
