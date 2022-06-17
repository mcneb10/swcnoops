package swcnoops.server.commands;

import swcnoops.server.Config;
import swcnoops.server.ServiceFactory;
import swcnoops.server.commands.player.PlayerPvpGetNextTarget;
import swcnoops.server.commands.player.response.PlayerPvpGetNextTargetCommandResult;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class PlayerPvpGetNextTargetTest {
    static {
        ServiceFactory.instance(new Config());
    }

    @Test
    public void testNextLayout() {
        PlayerPvpGetNextTarget playerPvpGetNextTarget = new PlayerPvpGetNextTarget();
        PlayerPvpGetNextTargetCommandResult response = playerPvpGetNextTarget.execute(null);
        assertNotNull(response);
    }
}
